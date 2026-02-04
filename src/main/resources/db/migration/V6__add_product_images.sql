-- Add images column to store JSON array of image URLs
ALTER TABLE products ADD COLUMN images TEXT NULL;

-- Migrate existing image_url to images array (for backward compatibility)
UPDATE products 
SET images = CONCAT('["', image_url, '"]')
WHERE image_url IS NOT NULL AND image_url != '';
