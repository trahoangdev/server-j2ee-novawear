-- Nhãn "Mới" / Hàng mới về (hiển thị badge trên thẻ sản phẩm)
ALTER TABLE products ADD COLUMN is_new BOOLEAN NOT NULL DEFAULT FALSE;
