-- 001_init.sql
-- Inicialización de esquema para Postgres acorde a las entidades JPA del proyecto
-- Generado para ejecutarse en /docker-entrypoint-initdb.d de la imagen oficial de Postgres

BEGIN;

-- Tabla de pedidos (OrderEntity)
-- En la entidad `OrderEntity.id` se usa String (UUID como texto), por eso se crea VARCHAR(36)
CREATE TABLE IF NOT EXISTS orders (
	id VARCHAR(36) PRIMARY KEY,
	created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Tabla de líneas de pedido (OrderItemEntity)
-- Corresponde a la clase OrderItemEntity: id (PK autogenerada), relación many-to-one con orders
CREATE TABLE IF NOT EXISTS order_items (
	id BIGSERIAL PRIMARY KEY,
	order_id VARCHAR(36) NOT NULL,
	product_id VARCHAR(255),
	quantity INTEGER NOT NULL CHECK (quantity > 0),
	unit_amount NUMERIC(19,2) NOT NULL CHECK (unit_amount >= 0),
	currency VARCHAR(3),
	created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
	CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Índices de ayuda
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items(product_id);

COMMIT;


