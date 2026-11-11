package com.nexusmarket.application.service;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.error.AuthorizationException;
import com.nexusmarket.application.error.BusinessRuleException;
import com.nexusmarket.application.error.NotFoundException;
import com.nexusmarket.application.error.ValidationException;
import com.nexusmarket.application.port.in.ManageReturnsUseCase;
import com.nexusmarket.application.port.out.BuyerRepository;
import com.nexusmarket.application.port.out.InventoryMovementRepository;
import com.nexusmarket.application.port.out.InventoryRepository;
import com.nexusmarket.application.port.out.OrderItemRepository;
import com.nexusmarket.application.port.out.OrderRepository;
import com.nexusmarket.application.port.out.ProductRepository;
import com.nexusmarket.application.port.out.RefundRepository;
import com.nexusmarket.application.port.out.ReturnRepository;
import com.nexusmarket.application.port.out.WarehouseRepository;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.application.support.Authorization;
import com.nexusmarket.application.support.Validation;
import com.nexusmarket.domain.buyer.Buyer;
import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.domain.inventory.Inventory;
import com.nexusmarket.domain.inventory.InventoryMovement;
import com.nexusmarket.domain.inventory.InventoryMovementType;
import com.nexusmarket.domain.order.Order;
import com.nexusmarket.domain.order.OrderItem;
import com.nexusmarket.domain.order.OrderItemStatus;
import com.nexusmarket.domain.returns.Refund;
import com.nexusmarket.domain.returns.RefundStatus;
import com.nexusmarket.domain.returns.Return;
import com.nexusmarket.domain.returns.ReturnCondition;
import com.nexusmarket.domain.returns.ReturnStatus;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.domain.warehouse.Warehouse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that manages returns and generates their partial
 * refunds.
 *
 * <p>Increment "envío y devolución por ítem" (2026-11-10). The buyer requests
 * the return of a delivered line, the administrator approves it and the
 * logistics operator receives it. Reception restores the stock in one
 * transaction, records the devolution movement and creates exactly one refund
 * with the historical unit price.</p>
 *
 * <p>Traceability: OBJ-11, RG-03, RN-12.</p>
 */
@Service
public class ReturnService implements ManageReturnsUseCase {

    private static final int MAX_WAREHOUSES = 100;

    private final ReturnRepository returnRepository;
    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BuyerRepository buyerRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public ReturnService(ReturnRepository returnRepository,
                         RefundRepository refundRepository,
                         OrderRepository orderRepository,
                         OrderItemRepository orderItemRepository,
                         BuyerRepository buyerRepository,
                         ProductRepository productRepository,
                         WarehouseRepository warehouseRepository,
                         InventoryRepository inventoryRepository,
                         InventoryMovementRepository inventoryMovementRepository) {
        this.returnRepository = returnRepository;
        this.refundRepository = refundRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.buyerRepository = buyerRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    @Override
    @Transactional
    public Return requestReturn(ActorContext actor, RequestReturnCommand command) {
        Authorization.requireAnyRole(actor, Role.BUYER);
        Validation.requireNotNull(command, "command");
        Long orderId = Validation.requireNotNull(command.orderId(), "orderId");
        Long orderItemId = Validation.requireNotNull(command.orderItemId(), "orderItemId");
        Validation.requireNotNull(command.condition(), "condition");
        String reason = Validation.requireText(command.reason(), "reason");
        if (command.quantity() <= 0) {
            throw new ValidationException("quantity must be a positive integer");
        }

        Buyer buyer = requireBuyerOfActor(actor);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        if (!buyer.getId().equals(order.getBuyerId())) {
            throw new AuthorizationException("A buyer can only return its own orders (RG-03)");
        }

        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new NotFoundException("Order item not found: " + orderItemId));
        if (!order.getId().equals(item.getOrderId())) {
            throw new NotFoundException("Order item not found in the order: " + orderItemId);
        }
        if (item.getStatus() != OrderItemStatus.DELIVERED) {
            throw new BusinessRuleException("Only a delivered item can be returned");
        }

        int alreadyReturned = returnRepository.findByOrderItemId(orderItemId).stream()
                .filter(current -> current.getStatus() != ReturnStatus.REJECTED)
                .mapToInt(Return::getQuantity)
                .sum();
        if (alreadyReturned + command.quantity() > item.getQuantity()) {
            throw new ValidationException(
                    "The returned quantity exceeds the purchased quantity");
        }

        Return returnRecord = new Return(null, order.getId(), item.getId(), buyer.getId(),
                command.quantity(), reason, command.condition(), ReturnStatus.REQUESTED,
                LocalDateTime.now(), null);
        Return saved = returnRepository.save(returnRecord);
        item.requestReturn();
        orderItemRepository.save(item);
        return saved;
    }

    @Override
    @Transactional
    public Return approveReturn(ActorContext actor, Long returnId) {
        Authorization.requireAnyRole(actor, Role.ADMINISTRATOR);
        Return returnRecord = requireReturn(returnId);
        returnRecord.approve();
        return returnRepository.save(returnRecord);
    }

    @Override
    @Transactional
    public ReturnReceipt receiveReturn(ActorContext actor, Long returnId) {
        Authorization.requireAnyRole(actor, Role.LOGISTICS_OPERATOR);
        Return returnRecord = returnRepository.findByIdForUpdate(returnId)
                .orElseThrow(() -> new NotFoundException("Return not found: " + returnId));

        if (returnRecord.isReceived()) {
            Refund existing = refundRepository.findByReturnId(returnId)
                    .orElseThrow(() -> new BusinessRuleException(
                            "The return is received but has no refund"));
            return new ReturnReceipt(returnRecord, existing);
        }
        if (returnRecord.getStatus() != ReturnStatus.APPROVED) {
            throw new BusinessRuleException("Only an approved return can be received");
        }

        OrderItem item = orderItemRepository.findById(returnRecord.getOrderItemId())
                .orElseThrow(() -> new NotFoundException(
                        "Order item not found: " + returnRecord.getOrderItemId()));
        Inventory inventory = lockInventoryFor(returnRecord, item);
        if (returnRecord.getCondition() == ReturnCondition.RESELLABLE) {
            inventory.setQuantity(inventory.getQuantity() + returnRecord.getQuantity());
        } else {
            inventory.setDamagedQuantity(
                    inventory.getDamagedQuantity() + returnRecord.getQuantity());
        }
        inventoryRepository.save(inventory);

        InventoryMovement movement = new InventoryMovement(null, inventory.getId(),
                InventoryMovementType.RETURN, returnRecord.getQuantity());
        movement.setOccurredAt(LocalDateTime.now());
        inventoryMovementRepository.save(movement);

        returnRecord.receive();
        returnRepository.save(returnRecord);

        item.receiveReturn();
        item.refund();
        orderItemRepository.save(item);

        BigDecimal amount = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(returnRecord.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        Refund refund = new Refund(null, returnRecord.getId(), returnRecord.getOrderId(),
                returnRecord.getOrderItemId(), amount, RefundStatus.REGISTERED,
                LocalDateTime.now());
        Refund savedRefund = refundRepository.save(refund);
        return new ReturnReceipt(returnRecord, savedRefund);
    }

    @Override
    public Return findReturn(ActorContext actor, Long returnId) {
        Authorization.requireAnyRole(actor, Role.BUYER, Role.ADMINISTRATOR);
        Return returnRecord = requireReturn(returnId);
        requireReadAccess(actor, returnRecord.getBuyerId());
        refundRepository.findByReturnId(returnId)
                .ifPresent(refund -> returnRecord.setRefundId(refund.getId()));
        return returnRecord;
    }

    @Override
    public PageResult<Return> findReturnsByOrder(ActorContext actor, Long orderId, PageQuery query) {
        Authorization.requireAnyRole(actor, Role.BUYER, Role.ADMINISTRATOR);
        Validation.requireNotNull(orderId, "orderId");
        if (actor.role() == Role.BUYER) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
            requireReadAccess(actor, order.getBuyerId());
        }
        PageResult<Return> page = returnRepository.findByOrderId(orderId, query);
        for (Return current : page.content()) {
            refundRepository.findByReturnId(current.getId())
                    .ifPresent(refund -> current.setRefundId(refund.getId()));
        }
        return page;
    }

    /**
     * Locks the stock of the returned product and chooses the warehouse: the
     * seller's warehouse that already holds stock for the product, otherwise
     * the first stock record of that product.
     */
    private Inventory lockInventoryFor(Return returnRecord, OrderItem item) {
        List<Inventory> stocks = inventoryRepository.lockByProductId(item.getProductId());
        if (stocks.isEmpty()) {
            throw new BusinessRuleException(
                    "No inventory record exists for product " + item.getProductId());
        }
        Product product = productRepository.findById(item.getProductId()).orElse(null);
        if (product != null && product.getSellerId() != null) {
            PageResult<Warehouse> warehouses = warehouseRepository.findBySellerId(
                    product.getSellerId(), new PageQuery(0, MAX_WAREHOUSES, "id,asc"));
            for (Warehouse warehouse : warehouses.content()) {
                for (Inventory stock : stocks) {
                    if (warehouse.getId().equals(stock.getWarehouseId())) {
                        return stock;
                    }
                }
            }
        }
        return stocks.get(0);
    }

    private void requireReadAccess(ActorContext actor, Long buyerId) {
        if (actor.role() == Role.ADMINISTRATOR) {
            return;
        }
        Buyer buyer = requireBuyerOfActor(actor);
        if (!buyer.getId().equals(buyerId)) {
            throw new AuthorizationException("A buyer can only read its own returns (RG-03)");
        }
    }

    private Return requireReturn(Long returnId) {
        return returnRepository.findById(returnId)
                .orElseThrow(() -> new NotFoundException("Return not found: " + returnId));
    }

    private Buyer requireBuyerOfActor(ActorContext actor) {
        return buyerRepository.findByUserId(actor.userId())
                .orElseThrow(() -> new NotFoundException(
                        "Buyer not found for the authenticated user"));
    }
}
