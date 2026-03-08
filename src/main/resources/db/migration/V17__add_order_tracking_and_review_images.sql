-- Add tracking info to orders
ALTER TABLE orders ADD COLUMN tracking_number VARCHAR(100) DEFAULT NULL;
ALTER TABLE orders ADD COLUMN carrier VARCHAR(100) DEFAULT NULL;

-- Add images to reviews (comma-separated URLs)
ALTER TABLE reviews ADD COLUMN images TEXT DEFAULT NULL;
