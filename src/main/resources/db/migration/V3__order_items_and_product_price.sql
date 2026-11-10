-- ---------------------------------------------------------------------------
-- V3 - Order items and product price (pedidos con items)
-- ---------------------------------------------------------------------------
-- V1 y V2 no se modifican. Esta migracion agrega:
--   1. products.price: precio vigente del producto. Se usa una instantanea
--      (order_items.unit_price) al agregar el producto al carrito.
--      DEFAULT 0.00 para las filas existentes; el carrito exige precio > 0.
--   2. order_items: lineas del pedido con cantidad y precio unitario.
--
-- Compatible con MySQL y H2 en MODE=MySQL.
-- ---------------------------------------------------------------------------

ALTER TABLE products ADD COLUMN price DECIMAL(12,2) NOT NULL DEFAULT 0.00;

CREATE TABLE order_items (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    order_id   BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity   INTEGER NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_order_items_order_product UNIQUE (order_id, product_id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_product_id ON order_items (product_id);
