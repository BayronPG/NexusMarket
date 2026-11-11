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
import com.nexusmarket.application.port.out.SellerRepository;
import com.nexusmarket.application.support.Authorization;
import com.nexusmarket.application.support.Validation;
import com.nexusmarket.domain.buyer.Buyer;
import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.domain.inventory.Inventory;
import com.nexusmarket.domain.order.Order;
import com.nexusmarket.domain.order.OrderItem;
import com.nexusmarket.domain.order.OrderStatus;
import com.nexusmarket.domain.seller.Seller;
import com.nexusmarket.domain.user.Role;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that manages the order lifecycle, its lines and the
 * per-line dispatch.
 *
 * <p>EXPLICIT source: the Buyer, the Seller and the Logistics Operator manage
 * orders (responsibilities matrix); an order follows the cycle Carrito,
 * Pendiente de Pago, Pagado, Despachado, Entregado/Finalizado without extra
 * transitions (RN-16); a finalized order cannot be modified (RN-13).</p>
 *
 * <p>INTERPRETATION: only the owner edits the cart (RG-03); only published
 * products with a positive price can be added; the quantity never exceeds the
 * available stock. The payment step deducts inventory inside the same
 * transaction and keeps a pessimistic lock on the stock rows. Shipping is
 * per line: the seller that owns the product dispatches its line and the
 * logistics operator confirms delivery; the global order status is derived.</p>
 *
 * <p>Traceability: OBJ-07, OBJ-08, OBJ-10, RN-13, RN-16, RG-03.</p>
 */
@Service
public class OrderService implements ManageOrdersUseCase {

    private final OrderRepository orderRepository;
    private final BuyerRepository buyerRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final SellerRepository sellerRepository;

    public OrderService(OrderRepository orderRepository,
                        BuyerRepository buyerRepository,
                        ProductRepository productRepository,
                        InventoryRepository inventoryRepository,
                        OrderItemRepository orderItemRepository,
                        SellerRepository sellerRepository) {
        this.orderRepository = orderRepository;
        this.buyerRepository = buyerRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderItemRepository = orderItemRepository;
        this.sellerRepository = sellerRepository;
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
        enrichSellerIds(order);
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
        enrichSellerIds(order);
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
        enrichSellerIds(order);
        return order;
    }

    @Override
    @Transactional
    public Order changeOrderStatus(ActorContext actor, Long orderId, OrderStatus next) {
        Authorization.requireAnyRole(actor, Role.BUYER);
        Validation.requireNotNull(next, "next status");
        if (next != OrderStatus.PENDING_PAYMENT && next != OrderStatus.PAID) {
            throw new AuthorizationException(
                    "Shipping and delivery are managed per line, not with the order status");
        }

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
        Order saved = orderRepository.save(order);
        enrichSellerIds(saved);
        return saved;
    }

    @Override
    @Transactional
    public Order shipItem(ActorContext actor, Long orderId, Long itemId) {
        Authorization.requireAnyRole(actor, Role.SELLER);
        Seller seller = sellerRepository.findByUserId(actor.userId())
                .orElseThrow(() -> new NotFoundException(
                        "Seller not found for the authenticated user"));

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        loadItems(order);
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessRuleException("Only a paid order can be shipped");
        }

        OrderItem item = order.findItem(itemId)
                .orElseThrow(() -> new NotFoundException("Order item not found: " + itemId));
        Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new NotFoundException(
                        "Product not found: " + item.getProductId()));
        if (!seller.getId().equals(product.getSellerId())) {
            throw new AuthorizationException("A seller can only ship its own products (RG-03)");
        }

        item.ship();
        orderItemRepository.save(item);
        order.refreshFulfillmentStatus();
        Order saved = orderRepository.save(order);
        enrichSellerIds(saved);
        return saved;
    }

    @Override
    @Transactional
    public Order deliverItem(ActorContext actor, Long orderId, Long itemId) {
        Authorization.requireAnyRole(actor, Role.LOGISTICS_OPERATOR);

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        loadItems(order);
        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.SHIPPED) {
            throw new BusinessRuleException("Only a paid or shipped order can be delivered");
        }

        OrderItem item = order.findItem(itemId)
                .orElseThrow(() -> new NotFoundException("Order item not found: " + itemId));
        item.deliver();
        orderItemRepository.save(item);
        order.refreshFulfillmentStatus();
        Order saved = orderRepository.save(order);
        enrichSellerIds(saved);
        return saved;
    }

    @Override
    @Transactional
    public Order findOrder(ActorContext actor, Long orderId) {
        Authorization.requireAnyRole(actor, Role.BUYER, Role.SELLER, Role.LOGISTICS_OPERATOR);
        Order order = requireOrder(orderId);
        loadItems(order);
        if (actor.role() == Role.BUYER) {
            requireOwner(actor, order);
        }
        enrichSellerIds(order);
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

    /**
     * Fills the derived seller of every line so the order response can show
     * which seller must dispatch each product, without persisting the link.
     */
    private void enrichSellerIds(Order order) {
        for (OrderItem item : order.getItems()) {
            productRepository.findById(item.getProductId())
                    .ifPresent(product -> item.setSellerId(product.getSellerId()));
        }
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
