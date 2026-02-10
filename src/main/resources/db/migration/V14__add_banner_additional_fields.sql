-- Thêm các field bổ sung cho banner: description, button thứ 2, badge text
ALTER TABLE banners 
ADD COLUMN description TEXT,
ADD COLUMN cta_text2 VARCHAR(50),
ADD COLUMN link_url2 VARCHAR(500),
ADD COLUMN badge_text VARCHAR(50);
