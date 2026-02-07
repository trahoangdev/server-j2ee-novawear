ALTER TABLE orders ADD COLUMN order_code VARCHAR(100);

-- Initialize existing orders with padded IDs
UPDATE orders SET order_code = LPAD(CAST(id AS CHAR(50)), 6, '0');

-- Add not null constraint (MySQL syntax)
ALTER TABLE orders MODIFY order_code VARCHAR(100) NOT NULL;

-- Add unique constraint
ALTER TABLE orders ADD CONSTRAINT uk_orders_order_code UNIQUE (order_code);
