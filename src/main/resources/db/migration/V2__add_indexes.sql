-- V2: indices for the filters used by the paginated listing endpoints (Fase 2.9).
-- Only columns that already exist in V1; V1 is not modified.

CREATE INDEX idx_products_seller_id ON products (seller_id);
CREATE INDEX idx_products_status ON products (status);

CREATE INDEX idx_inventories_product_id ON inventories (product_id);
CREATE INDEX idx_inventories_warehouse_id ON inventories (warehouse_id);

CREATE INDEX idx_warehouses_seller_id ON warehouses (seller_id);

CREATE INDEX idx_refunds_order_id ON refunds (order_id);
