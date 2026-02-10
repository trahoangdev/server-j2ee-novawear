-- Giá khuyến mãi (null = không sale). Nếu set và < price thì hiển thị giảm giá.
ALTER TABLE products ADD COLUMN sale_price DECIMAL(12,2) NULL;
-- Nổi bật: hiển thị ở block "Sản phẩm nổi bật" trang chủ
ALTER TABLE products ADD COLUMN featured BOOLEAN NOT NULL DEFAULT FALSE;
-- Bán chạy: nhãn/hiển thị block bán chạy (admin đánh dấu thủ công)
ALTER TABLE products ADD COLUMN bestseller BOOLEAN NOT NULL DEFAULT FALSE;
