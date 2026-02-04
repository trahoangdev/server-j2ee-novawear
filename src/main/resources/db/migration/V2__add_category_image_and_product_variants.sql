-- Thêm cột hình ảnh cho danh mục (quản lý danh mục)
ALTER TABLE categories ADD COLUMN image_url VARCHAR(500) NULL;

-- Thêm cột biến thể size/color cho sản phẩm (quản lý sản phẩm)
ALTER TABLE products ADD COLUMN sizes VARCHAR(500) NULL;
ALTER TABLE products ADD COLUMN colors VARCHAR(2000) NULL;
