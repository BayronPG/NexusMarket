-- ---------------------------------------------------------------------------
-- V5 - Per-line fulfillment, returns and partial refunds (sustentacion)
-- ---------------------------------------------------------------------------
-- V1-V4 no se modifican. Esta migracion agrega:
--   1. order_items.fulfillment_status: estado logistico por linea.
--   2. returns: devolucion de una linea, con condicion y ciclo de vida.
--   3. refunds: reembolso parcial originado por una devolucion (1 a 1).
--   4. inventory_movements: movimiento de devolucion sobre el inventario.
--
-- Compatible con MySQL 8 y H2 en MODE=MySQL.
-- ---------------------------------------------------------------------------

ALTER TABLE order_items
    ADD COLUMN fulfillment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

CREATE TABLE returns (
    id            BIGINT NOT NULL AUTO_INCREMENT,
    order_id      BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    buyer_id      BIGINT NOT NULL,
    quantity      INTEGER NOT NULL,
    reason        VARCHAR(500),
    item_condition VARCHAR(20) NOT NULL,
    status        VARCHAR(20) NOT NULL,
    created_at    DATETIME NOT NULL,
    received_at   DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_returns_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_returns_order_item FOREIGN KEY (order_item_id) REFERENCES order_items (id),
    CONSTRAINT fk_returns_buyer FOREIGN KEY (buyer_id) REFERENCES buyers (id)
);

CREATE INDEX idx_returns_order_id ON returns (order_id);
CREATE INDEX idx_returns_order_item_id ON returns (order_item_id);

ALTER TABLE refunds ADD COLUMN return_id BIGINT;
ALTER TABLE refunds ADD COLUMN order_item_id BIGINT;
ALTER TABLE refunds ADD COLUMN amount DECIMAL(12,2);
ALTER TABLE refunds ADD COLUMN status VARCHAR(20);
ALTER TABLE refunds ADD COLUMN created_at DATETIME;

ALTER TABLE refunds
    ADD CONSTRAINT uq_refunds_return UNIQUE (return_id);
ALTER TABLE refunds
    ADD CONSTRAINT fk_refunds_return FOREIGN KEY (return_id) REFERENCES returns (id);

-- idx_refunds_order_id already exists since V2; it is not created again.

CREATE TABLE inventory_movements (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    inventory_id BIGINT NOT NULL,
    type         VARCHAR(20) NOT NULL,
    quantity     INTEGER NOT NULL,
    occurred_at  DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_inventory_movements_inventory
        FOREIGN KEY (inventory_id) REFERENCES inventories (id)
);

CREATE INDEX idx_inventory_movements_inventory_id ON inventory_movements (inventory_id);
