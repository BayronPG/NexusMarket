-- ---------------------------------------------------------------------------
-- V1 - Esquema inicial de NexusMarket
-- Fase 1.5 del docs/PLAN_API_WEB.md
-- ---------------------------------------------------------------------------
-- Equivalente al modelo JPA actual (Fase 1.2): las 8 entidades y las 2 tablas
-- auxiliares de los atributos de tipo lista (@ElementCollection).
--
-- SQL compatible a la vez con MySQL (base de datos real) y con H2 en
-- MODE=MySQL (perfil "test"). Por eso se usa AUTO_INCREMENT en lugar de
-- IDENTITY generico y no se declaran clausulas propias de un solo motor
-- (ENGINE, CHARSET, COLLATE).
--
-- Los vinculos entre agregados se guardan como columnas escalares (P-1): no se
-- declaran claves foraneas ni asociaciones JPA no respaldadas por el modelo.
-- ---------------------------------------------------------------------------

CREATE TABLE users (
    id                BIGINT NOT NULL AUTO_INCREMENT,
    full_name         VARCHAR(255),
    email             VARCHAR(255),
    identity_document VARCHAR(255),
    role              VARCHAR(255),
    status            VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE sellers (
    id      BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT,
    PRIMARY KEY (id)
);

CREATE TABLE buyers (
    id                BIGINT NOT NULL AUTO_INCREMENT,
    user_id           BIGINT,
    primary_address   VARCHAR(255),
    commercial_status VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE buyer_additional_addresses (
    buyer_id BIGINT NOT NULL,
    address  VARCHAR(255)
);

CREATE TABLE warehouses (
    id        BIGINT NOT NULL AUTO_INCREMENT,
    type      VARCHAR(255),
    seller_id BIGINT,
    PRIMARY KEY (id)
);

CREATE TABLE products (
    id        BIGINT NOT NULL AUTO_INCREMENT,
    seller_id BIGINT,
    type      VARCHAR(255),
    status    VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE product_variants (
    product_id BIGINT NOT NULL,
    variant    VARCHAR(255)
);

CREATE TABLE inventories (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    product_id       BIGINT,
    warehouse_id     BIGINT,
    quantity         INTEGER,
    damaged_quantity INTEGER,
    PRIMARY KEY (id)
);

CREATE TABLE orders (
    id       BIGINT NOT NULL AUTO_INCREMENT,
    buyer_id BIGINT,
    status   VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE refunds (
    id       BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT,
    PRIMARY KEY (id)
);
