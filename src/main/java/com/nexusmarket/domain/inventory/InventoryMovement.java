package com.nexusmarket.domain.inventory;

import java.time.LocalDateTime;

/**
 * Movement registered over an inventory record.
 *
 * <p>EXPLICIT source: movements are Ingreso, Reserva, Salida por venta,
 * Ajuste y Devolución.</p>
 *
 * <p>INTERPRETATION: whether movements are a historical entity or only
 * operations over the stock is PENDING; {@code occurredAt} is a reasonable
 * attribute for a movement record.</p>
 *
 * <p>Traceability: OBJ-06.</p>
 */
public class InventoryMovement {

    /** Unique identifier of the movement. */
    private Long id;

    /** Inventory record affected. */
    private Long inventoryId;

    /** Kind of movement; required. */
    private InventoryMovementType type;

    /** Quantity involved. */
    private int quantity;

    /** When the movement occurred; INTERPRETATION. */
    private LocalDateTime occurredAt;

    public InventoryMovement() {
    }

    public InventoryMovement(Long id, Long inventoryId, InventoryMovementType type, int quantity) {
        this.id = id;
        this.inventoryId = inventoryId;
        this.type = type;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public InventoryMovementType getType() {
        return type;
    }

    public void setType(InventoryMovementType type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}
