-- Flyway migration: Create vouchers and user_vouchers tables
-- V10__add_vouchers_tables.sql

-- Bảng vouchers
CREATE TABLE vouchers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    discount_type VARCHAR(20) NOT NULL COMMENT 'PERCENT or FIXED',
    discount_value DECIMAL(12, 2) NOT NULL,
    min_order_value DECIMAL(12, 2),
    max_discount DECIMAL(12, 2),
    start_date TIMESTAMP NULL,
    end_date TIMESTAMP NULL,
    usage_limit INT,
    used_count INT NOT NULL DEFAULT 0,
    usage_limit_per_user INT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_voucher_code (code),
    INDEX idx_voucher_active (active),
    INDEX idx_voucher_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng user_vouchers (lịch sử sử dụng voucher)
CREATE TABLE user_vouchers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    voucher_id BIGINT NOT NULL,
    order_id BIGINT,
    used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_uv_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_uv_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE CASCADE,
    CONSTRAINT fk_uv_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,
    
    INDEX idx_uv_user (user_id),
    INDEX idx_uv_voucher (voucher_id),
    UNIQUE KEY uk_user_voucher_order (user_id, voucher_id, order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Thêm cột voucher vào bảng orders
ALTER TABLE orders ADD COLUMN voucher_id BIGINT NULL;
ALTER TABLE orders ADD COLUMN discount_amount DECIMAL(12, 2) NULL DEFAULT 0;
ALTER TABLE orders ADD CONSTRAINT fk_order_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE SET NULL;

-- Sample vouchers
INSERT INTO vouchers (code, description, discount_type, discount_value, min_order_value, max_discount, start_date, end_date, usage_limit, active) VALUES
('WELCOME10', 'Giảm 10% cho khách hàng mới', 'PERCENT', 10, 100000, 50000, NULL, NULL, NULL, TRUE),
('FREESHIP', 'Miễn phí vận chuyển 30K', 'FIXED', 30000, 200000, NULL, NULL, NULL, NULL, TRUE),
('SUMMER20', 'Giảm 20% mùa hè', 'PERCENT', 20, 300000, 100000, '2026-06-01 00:00:00', '2026-08-31 23:59:59', 1000, TRUE),
('VIP50', 'Giảm 50K cho VIP', 'FIXED', 50000, 500000, NULL, NULL, NULL, NULL, TRUE),
('FLASH30', 'Flash sale giảm 30%', 'PERCENT', 30, 200000, 150000, NULL, NULL, 100, TRUE);
