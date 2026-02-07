-- Flyway migration: Add gender column to products
-- V11__add_gender_to_products.sql

-- Thêm cột gender
ALTER TABLE products ADD COLUMN gender VARCHAR(20) DEFAULT 'UNISEX';

-- Tạo index cho gender
CREATE INDEX idx_product_gender ON products(gender);

-- Cập nhật một số sản phẩm mẫu
UPDATE products SET gender = 'MALE' WHERE name LIKE '%nam%' OR name LIKE '%Nam%';
UPDATE products SET gender = 'FEMALE' WHERE name LIKE '%nữ%' OR name LIKE '%Nữ%' OR name LIKE '%váy%' OR name LIKE '%Váy%';
