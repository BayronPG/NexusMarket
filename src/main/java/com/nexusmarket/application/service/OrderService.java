package com.nexusmarket.application.service;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.error.AuthorizationException;
import com.nexusmarket.application.error.BusinessRuleException;
import com.nexusmarket.application.error.NotFoundException;
import com.nexusmarket.application.error.ValidationException;
import com.nexusmarket.application.port.in.ManageOrdersUseCase;
import com.nexusmarket.application.port.out.BuyerRepository;
import com.nexusmarket.application.port.out.InventoryRepository;
import com.nexusmarket.application.port.out.OrderItemRepository;
import com.nexusmarket.application.port.out.OrderRepository;
import com.nexusmarket.application.port.out.ProductRepository;
import com.nexusmarket.application.support.Authorization;
import com.nexusmarket.application.support.Validation;
import com.nexusmarket.domain.buyer.Buyer;
import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.domain.inventory.Inventory;
import com.nexusmarket.domain.order.Order;
import com.nexusmarket.domain.order.OrderItem;
import com.nexusmarket.domain.order.OrderStatus;
import com.nexusmarket.domain.user.Role;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that manages the order lifecycle and its lines.
 *
 * <p>EXPLICIT source: the Buyer, the Seller and the Logistics Operator manage
 * orders (responsibilities matrix); an order follows the cycle Carrito,
 * Pendiente de Pago, Pagado, Despachado, Entregado/Finalizado without extra
 * transitions (RN-16); a finalized order cannot be modified (RN-13).</p>
 *
 * <p>INTERPRETATION: only the owner edits the cart (RG-03); only published
 * products with a positive price can be added; the quantity never exceeds the
 * available stock. The payment step deducts inventory inside the same
 * transaction and keeps a pessimistic lock on the stock rows.</p>
 *
 * <p>Traceability: OBJ-07, OBJ-08, RN-13, RN-16, RG-03.</p>
 */
@Service
public class OrderService implements ManageOrdersUseCase {

    private final OrderRepository orderRepository;
    private final BuyerRepository buyerRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository,
                        BuyerRepository buyerRepository,
                        ProductRepository productRepository,
                        InventoryRepository inventoryRepository,
                        OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.buyerRepository = buyerRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    @Transactional
    public Order createOrder(ActorContext actor) {
        Authorization.requireAnyRole(actor, Role.BUYER);
        Buyer buyer = requireBuyerOfActor(actor);
        Order order = new Order(null, buyer.getId(), OrderStatus.CART);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order addItem(ActorContext actor, Long orderId, Long productId, int quantity) {
        Authorization.requireAnyRole(actor, Role.BUYER);
        requirePositive(quantity);
        Validation.requireNotNull(productId, "productId");

        Order order = requireOwnedCart(actor, orderId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

        if (product.getStatus() != ProductStatus.PUBLISHED) {
            throw new BusinessRuleException("Only published products can be added to a cart");
        }
        if (product.getPrice() == null || product.getPrice().signum() <= 0) {
            throw new BusinessRuleException("The product has no valid price");
        }

        int resultingQuantity = order.findByProductId(productId)
                .map(item -> item.getQuantity() + quantity)
                .orElse(quantity);
        requireAvailableStock(productId, resultingQuantity);

        // The unit price is the current catalog price: a snapshot for the line.
        OrderItem item = order.addItem(new OrderItem(
                null, order.getId(), productId, quantity, product.getPrice()));
        if (item.getId() == null) {
            OrderItem saved = orderItemRepository.save(item);
            order.setItems(order.getItems().stream()
                    .map(current -> current == item ? saved : current)
                    .toList());
        } else {
            orderItemRepository.save(item);
        }
        return order;
    }

    @Override
    @Transactional
    public Order updateItem(ActorContext actor, Long orderId, Long itemId, int quantity) {
        Authorization.requireAnyRole(actor, Role.BUYER);
        requirePositive(quantity);

        Order order = requireOwnedCart(actor, orderId);
        OrderItem item = order.findItem(itemId)
                .orElseThrow(() -> new NotFoundException("Order item not found: " + itemId));

        requireAvailableStock(item.getProductId(), quantity);
        item.setQuantity(quantity);
        orderItemRepository.save(item);
        return order;
    }

    @Override
    @Transactional
    public Order removeItem(ActorContext actor, Long orderId, Long itemId) {
        Authorization.requireAnyRole(actor, Role.BUYER);

        Order order = requireOwnedCart(actor, orderId);
        boolean removed = order.removeItem(itemId);
        if (!removed) {
            throw new NotFoundException("Order item not found: " + itemId);
        }
        orderItemRepository.deleteById(itemId);
        return order;
    }

    @Override
    @Transactional
    public Order changeOrderStatus(ActorContext actor, Long orderId, OrderStatus next) {
        Authorization.requireAnyRole(actor, Role.BUYER, Role.SELLER, Role.LOGISTICS_OPERATOR);
        Validation.requireNotNull(next, "next status");

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        loadItems(order);

        if (actor.role() == Role.BUYER) {
            requireOwner(actor, order);
        }

        // A retried payment must not deduct the stock twice.
        if (order.getStatus() == OrderStatus.PAID && next == OrderStatus.PAID) {
            return order;
        }
        if ((next == OrderStatus.PENDING_PAYMENT || next == OrderStatus.PAID) && !order.hasItems()) {
            throw new BusinessRuleException("An order without items cannot be paid");
        }

        order.changeStatus(next);
        if (next == OrderStatus.PAID) {
            deductInventory(order);
        }
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order findOrder(ActorContext actor, Long orderId) {
        Authorization.requireAnyRole(actor, Role.BUYER, Role.SELLER, Role.LOGISTICS_OPERATOR);
        Order order = requireOrder(orderId);
        loadItems(order);
        return order;
    }

    /**
     * Deducts the stock of every line across the warehouses, from the first
     * locked record to the last. The caller runs inside a transaction, so the
     * status change and the deduction commit together.
     */
    private void deductInventory(Order order) {
        for (OrderItem item : order.getItems()) {
            List<Inventory> stocks = inventoryRepository.lockByProductId(item.getProductId());
            int available = stocks.stream()
                    .mapToInt(stock -> stock.getQuantity() - stock.getDamagedQuantity())
                    .sum();
            if (available < item.getQuantity()) {
                throw new BusinessRuleException(
                        "Insufficient inventory for product " + item.getProductId());
            }
            int pending = item.getQuantity();
            for (Inventory stock : stocks) {
                int usable = stock.getQuantity() - stock.getDamagedQuantity();
                int take = Math.min(usable, pending);
                if (take > 0) {
                    stock.setQuantity(stock.getQuantity() - take);
                    inventoryRepository.save(stock);
                    pending -= take;
                }
                if (pending == 0) {
                    break;
                }
            }
        }
    }

    private void requireAvailableStock(Long productId, int required) {
        int available = inventoryRepository.findByProductId(productId).stream()
                .mapToInt(stock -> stock.getQuantity() - stock.getDamagedQuantity())
                .sum();
        if (available < required) {
            throw new BusinessRuleException(
                    "Insufficient inventory for product " + productId);
        }
    }

    private Order requireOwnedCart(ActorContext actor, Long orderId) {
        Order order = requireOrder(orderId);
        loadItems(order);
        requireOwner(actor, order);
        if (order.getStatus() != OrderStatus.CART) {
            throw new BusinessRuleException(
                    "Items can only be modified while the order is in CART");
        }
        return order;
    }

    private void requireOwner(ActorContext actor, Order order) {
        Buyer buyer = requireBuyerOfActor(actor);
        if (!buyer.getId().equals(order.getBuyerId())) {
            throw new AuthorizationException("A buyer can only manage its own orders (RG-03)");
        }
    }

    private Order requireOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
    }

    private void loadItems(Order order) {
        order.setItems(orderItemRepository.findByOrderId(order.getId()));
    }

    private void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("quantity must be a positive integer");
        }
    }

    private Buyer requireBuyerOfActor(ActorContext actor) {
        return buyerRepository.findByUserId(actor.userId())
                .orElseThrow(() -> new NotFoundException("Buyer not found for the authenticated user"));
    }
}
