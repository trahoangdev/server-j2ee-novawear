-- V18: Create viewed_products table for tracking user viewed products
CREATE TABLE IF NOT EXISTS viewed_products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    viewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_viewed_product_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_viewed_product_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT uk_viewed_user_product UNIQUE (user_id, product_id)
);

CREATE INDEX idx_viewed_user_id ON viewed_products(user_id);
CREATE INDEX idx_viewed_viewed_at ON viewed_products(viewed_at);
