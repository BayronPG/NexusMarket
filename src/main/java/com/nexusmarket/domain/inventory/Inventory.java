package com.nexusmarket.domain.inventory;

/**
 * Inventory record of the NexusMarket platform.
 *
 * <p>EXPLICIT source: the inventory is distributed and must be linked to a
 * product and a warehouse. Stock can never be negative (RN-10).</p>
 *
 * <p>INTERPRETATION: {@code damagedQuantity} is derived from the critical
 * validation "no reservar inventario marcado como Dañado" (RN-12); how the
 * damaged state is represented is PENDING.</p>
 *
 * <p>Business rules enforced here: no negative stock (RN-10), no reserving
 * nonexistent stock (RN-11), no reserving damaged stock (RN-12).</p>
 *
 * <p>Traceability: OBJ-06, RN-09, RN-10, RN-11, RN-12.</p>
 */
public class Inventory {

    /** Unique identifier of the inventory record. */
    private Long id;

    /** Linked product; required (RN-09). */
    private Long productId;

    /** Linked warehouse; required (RN-09). */
    private Long warehouseId;

    /** Available stock; never negative (RN-10). */
    private int quantity;

    /** Stock marked as damaged; INTERPRETATION (RN-12). */
    private int damagedQuantity;

    public Inventory() {
    }

    public Inventory(Long id, Long productId, Long warehouseId, int quantity) {
        this.id = id;
        this.productId = productId;
        this.warehouseId = warehouseId;
        setQuantity(quantity);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets the stock; rejects negative values (RN-10).
     *
     * @param quantity new stock value
     * @throws IllegalArgumentException if the value is negative
     */
    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock can never be negative (RN-10)");
        }
        this.quantity = quantity;
    }

    public int getDamagedQuantity() {
        return damagedQuantity;
    }

    public void setDamagedQuantity(int damagedQuantity) {
        if (damagedQuantity < 0) {
            throw new IllegalArgumentException("Damaged quantity can never be negative");
        }
        this.damagedQuantity = damagedQuantity;
    }

    /**
     * Reserves stock for an order.
     *
     * <p>Validates that the requested quantity exists and is not damaged
     * (RN-11, RN-12).</p>
     *
     * @param amount quantity to reserve
     * @throws IllegalArgumentException if amount is not positive or exceeds
     *                                  the available (non-damaged) stock
     */
    public void reserve(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Reservation amount must be positive");
        }
        int available = quantity - damagedQuantity;
        if (available < amount) {
            throw new IllegalArgumentException(
                    "Cannot reserve nonexistent or damaged stock (RN-11, RN-12)");
        }
        quantity -= amount;
    }
}
