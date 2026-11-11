package com.nexusmarket.domain.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Order of the NexusMarket platform.
 *
 * <p>EXPLICIT source: component "solicitudes de compra realizadas por
 * compradores"; formal commercial commitment with a defined lifecycle
 * (OBJ-08).</p>
 *
 * <p>INTERPRETATION: {@code buyerId} links the order to the buyer who made
 * it. The content of the order is modeled as lines with product, quantity and
 * a unit-price snapshot; the specification does not describe order lines.</p>
 *
 * <p>Business rules enforced here: only the defined lifecycle transitions
 * are allowed (RN-16) and a finalized order cannot be modified (RN-13).</p>
 *
 * <p>Traceability: OBJ-08, RN-13, RN-16.</p>
 */
public class Order {

    /** Unique identifier of the order. */
    private Long id;

    /** Buyer that made the order; INTERPRETATION. */
    private Long buyerId;

    /** Current lifecycle status. */
    private OrderStatus status;

    /** Lines of the order; only editable while the order is in CART. */
    private List<OrderItem> items = new ArrayList<>();

    /** Allowed transitions defined by the specification (RN-16). */
    private static final Map<OrderStatus, OrderStatus> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(OrderStatus.CART, OrderStatus.PENDING_PAYMENT);
        ALLOWED_TRANSITIONS.put(OrderStatus.PENDING_PAYMENT, OrderStatus.PAID);
        ALLOWED_TRANSITIONS.put(OrderStatus.PAID, OrderStatus.SHIPPED);
        ALLOWED_TRANSITIONS.put(OrderStatus.SHIPPED, OrderStatus.DELIVERED);
    }

    public Order() {
    }

    public Order(Long id, Long buyerId, OrderStatus status) {
        this.id = id;
        this.buyerId = buyerId;
        this.status = status;
    }

    public Order(Long id, Long buyerId, OrderStatus status, List<OrderItem> items) {
        this(id, buyerId, status);
        setItems(items);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    /**
     * Replaces the lines, used when the order is hydrated from persistence.
     *
     * @param items loaded lines; null means an empty order
     */
    public void setItems(List<OrderItem> items) {
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
    }

    /**
     * Adds a line, or increases the quantity when the product is already
     * present. Lines can only be modified while the order is in CART.
     *
     * @param item line to add
     * @return the stored line (the existing one when the product was repeated)
     * @throws IllegalStateException if the order is not in CART
     */
    public OrderItem addItem(OrderItem item) {
        requireCart();
        Optional<OrderItem> existing = findByProductId(item.getProductId());
        if (existing.isPresent()) {
            existing.get().increase(item.getQuantity());
            return existing.get();
        }
        item.setOrderId(id);
        items.add(item);
        return item;
    }

    /**
     * Removes a line.
     *
     * @param itemId identifier of the line
     * @return {@code true} when the line existed
     * @throws IllegalStateException if the order is not in CART
     */
    public boolean removeItem(Long itemId) {
        requireCart();
        return items.removeIf(item -> itemId != null && itemId.equals(item.getId()));
    }

    /** Finds a line by product. */
    public Optional<OrderItem> findByProductId(Long productId) {
        return items.stream()
                .filter(item -> productId != null && productId.equals(item.getProductId()))
                .findFirst();
    }

    /** Finds a line by its identifier. */
    public Optional<OrderItem> findItem(Long itemId) {
        return items.stream()
                .filter(item -> itemId != null && itemId.equals(item.getId()))
                .findFirst();
    }

    /** Whether the order has at least one line. */
    public boolean hasItems() {
        return !items.isEmpty();
    }

    /**
     * Total of the order: sum of the line subtotals.
     *
     * @return total with two decimals
     */
    public BigDecimal total() {
        return items.stream()
                .map(OrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(OrderItem.PRICE_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Changes the order status only through the allowed lifecycle (RN-16).
     *
     * @param next the next status
     * @throws IllegalStateException if the order is finalized (RN-13) or the
     *                               transition is not allowed (RN-16)
     */
    public void changeStatus(OrderStatus next) {
        if (status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("A finalized order cannot be modified (RN-13)");
        }
        OrderStatus allowedNext = ALLOWED_TRANSITIONS.get(status);
        if (allowedNext == null || allowedNext != next) {
            throw new IllegalStateException(
                    "Transition not allowed: " + status + " -> " + next + " (RN-16)");
        }
        this.status = next;
    }

    /**
     * Derives the global order status from the fulfillment of its lines.
     *
     * <p>SHIPPED once every line left the seller; DELIVERED once every line was
     * delivered (or reached a later state such as returned or refunded). The
     * order keeps a single status so the existing lifecycle remains valid, but
     * it never advances through this method after DELIVERED (RN-13).</p>
     *
     * @throws IllegalStateException if the derived transition is not allowed
     */
    public void refreshFulfillmentStatus() {
        if (items.isEmpty() || status == OrderStatus.DELIVERED) {
            return;
        }
        boolean allShipped = items.stream()
                .allMatch(item -> item.getStatus().isAtLeastShipped());
        if (!allShipped) {
            return;
        }
        boolean allDelivered = items.stream()
                .allMatch(item -> item.getStatus().isAtLeastDelivered());
        if (allDelivered) {
            if (status == OrderStatus.PAID) {
                changeStatus(OrderStatus.SHIPPED);
            }
            if (status == OrderStatus.SHIPPED) {
                changeStatus(OrderStatus.DELIVERED);
            }
        } else if (status == OrderStatus.PAID) {
            changeStatus(OrderStatus.SHIPPED);
        }
    }

    private void requireCart() {
        if (status != OrderStatus.CART) {
            throw new IllegalStateException("Items can only be modified while the order is in CART");
        }
    }
}
