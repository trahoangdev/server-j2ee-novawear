-- Thêm field banner_type để phân biệt loại banner (CAROUSEL hoặc PROMO)
ALTER TABLE banners 
ADD COLUMN banner_type VARCHAR(20) DEFAULT 'CAROUSEL';

-- Tạo index cho banner_type
CREATE INDEX idx_banner_type ON banners(banner_type);

-- Cập nhật banner mặc định là CAROUSEL
UPDATE banners SET banner_type = 'CAROUSEL' WHERE banner_type IS NULL;
