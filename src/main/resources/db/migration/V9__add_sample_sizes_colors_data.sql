-- Add sample sizes and colors data to existing products
-- This migration populates the sizes and colors JSON fields for all products

-- Áo Blazer Dáng Rộng Premium (id=10) - Áo
UPDATE products SET 
    sizes = '["S","M","L","XL"]',
    colors = '[{"name":"Đen","hex":"#2D2D2D"},{"name":"Be","hex":"#D4C4A8"},{"name":"Xám","hex":"#6B7280"}]'
WHERE id = 10;

-- Váy Midi Hoa Nhí Vintage (id=11) - Váy
UPDATE products SET 
    sizes = '["S","M","L"]',
    colors = '[{"name":"Hồng Pastel","hex":"#FFB6C1"},{"name":"Xanh Mint","hex":"#98FF98"},{"name":"Trắng Kem","hex":"#FFFDD0"}]'
WHERE id = 11;

-- Quần Palazzo Ống Rộng (id=12) - Quần
UPDATE products SET 
    sizes = '["S","M","L","XL"]',
    colors = '[{"name":"Đen","hex":"#2D2D2D"},{"name":"Trắng","hex":"#FFFFFF"},{"name":"Nâu","hex":"#8B4513"}]'
WHERE id = 12;

-- Túi Xách Mini Đeo Chéo (id=13) - Túi xách
UPDATE products SET 
    sizes = '["One Size"]',
    colors = '[{"name":"Đen","hex":"#2D2D2D"},{"name":"Nâu Bò","hex":"#964B00"},{"name":"Hồng Nhạt","hex":"#FFB6C1"}]'
WHERE id = 13;

-- Giày Sandal Quai Ngang (id=14) - Giày
UPDATE products SET 
    sizes = '["36","37","38","39","40","41","42"]',
    colors = '[{"name":"Đen","hex":"#2D2D2D"},{"name":"Trắng","hex":"#FFFFFF"},{"name":"Be","hex":"#D4C4A8"}]'
WHERE id = 14;

-- Áo Thun Basic Cotton (id=15) - Áo
UPDATE products SET 
    sizes = '["S","M","L","XL","XXL"]',
    colors = '[{"name":"Trắng","hex":"#FFFFFF"},{"name":"Đen","hex":"#2D2D2D"},{"name":"Xám","hex":"#6B7280"},{"name":"Navy","hex":"#000080"}]'
WHERE id = 15;

-- Quần Jean Slim Fit (id=16) - Quần
UPDATE products SET 
    sizes = '["28","29","30","31","32","33","34"]',
    colors = '[{"name":"Xanh Đậm","hex":"#1E3A5F"},{"name":"Xanh Nhạt","hex":"#6CA0DC"},{"name":"Đen","hex":"#2D2D2D"}]'
WHERE id = 16;

-- Ví Da Nam Cao Cấp (id=17) - Phụ kiện
UPDATE products SET 
    sizes = '["One Size"]',
    colors = '[{"name":"Đen","hex":"#2D2D2D"},{"name":"Nâu","hex":"#8B4513"},{"name":"Nâu Đậm","hex":"#654321"}]'
WHERE id = 17;
