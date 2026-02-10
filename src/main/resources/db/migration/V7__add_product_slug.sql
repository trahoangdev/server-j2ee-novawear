-- Add slug column to products table
ALTER TABLE products ADD COLUMN slug VARCHAR(255) NULL;

-- Create index for slug lookups
CREATE INDEX idx_products_slug ON products(slug);

-- Note: Slug generation from existing names will be handled by application code
-- when products are updated, or can be done manually via admin interface
