package com.example.novawear.service;

import com.example.novawear.dto.ProductColorDto;
import com.example.novawear.dto.ProductDto;
import com.example.novawear.dto.ProductFlagsDto;
import com.example.novawear.entity.Category;
import com.example.novawear.entity.Product;
import com.example.novawear.repository.CategoryRepository;
import com.example.novawear.repository.ProductRepository;
import com.example.novawear.repository.FlashSaleRepository;
import com.example.novawear.entity.FlashSale;
import com.example.novawear.entity.FlashSaleProduct;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FlashSaleRepository flashSaleRepository;
    private final ObjectMapper objectMapper;

    private List<String> parseSizes(String json) {
        if (json == null || json.isBlank())
            return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<ProductColorDto> parseColors(String json) {
        if (json == null || json.isBlank())
            return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String serializeSizes(List<String> list) {
        if (list == null || list.isEmpty())
            return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }

    private String serializeColors(List<ProductColorDto> list) {
        if (list == null || list.isEmpty())
            return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> parseImages(String json) {
        if (json == null || json.isBlank())
            return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String serializeImages(List<String> list) {
        if (list == null || list.isEmpty())
            return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Generate slug from product name.
     * Converts to lowercase, replaces spaces with hyphens, removes special
     * characters.
     */
    private String generateSlug(String name) {
        if (name == null || name.isBlank())
            return null;
        String slug = name.trim()
                .toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return slug.isEmpty() ? null : slug;
    }

    private ProductDto toDto(Product p) {
        ProductDto dto = ProductDto.from(p);
        dto.setSizes(parseSizes(p.getSizes()));
        dto.setColors(parseColors(p.getColors()));
        List<String> imageList = parseImages(p.getImages());
        // Backward compatibility: if images is empty but imageUrl exists, use imageUrl
        if (imageList.isEmpty() && p.getImageUrl() != null && !p.getImageUrl().isBlank()) {
            imageList = Collections.singletonList(p.getImageUrl());
        }
        dto.setImages(imageList);

        // Apply Flash Sale Override if active
        List<FlashSale> activeSales = flashSaleRepository.findActiveNow(java.time.Instant.now());
        for (FlashSale sale : activeSales) {
            for (FlashSaleProduct fp : sale.getProducts()) {
                if (fp.getProduct().getId().equals(p.getId())) {
                    dto.setSalePrice(fp.getSalePrice());
                    dto.setIsFlashSale(true);
                    break;
                }
            }
            if (Boolean.TRUE.equals(dto.getIsFlashSale())) break;
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public ProductDto getById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        return toDto(p);
    }

    @Transactional(readOnly = true)
    public ProductDto getBySlug(String slug) {
        Product p = productRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with slug: " + slug));
        return toDto(p);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findAllFiltered(Boolean onSale, Boolean bestseller, Boolean isNew, Pageable pageable) {
        return productRepository.findAllFiltered(onSale, bestseller, isNew, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findLowStock(int threshold, Pageable pageable) {
        return productRepository.findByStockLessThan(threshold, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return productRepository.findAll(pageable).map(this::toDto);
        }
        return productRepository.searchByName(keyword.trim(), pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> findFeatured() {
        List<Product> list = productRepository.findTop8ByFeaturedTrueOrderByIdDesc();
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDto> findBestseller() {
        List<Product> list = productRepository.findTop8ByBestsellerTrueOrderByIdDesc();
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findWithFilters(
            List<Long> categoryIds,
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice,
            List<String> sizes,
            List<String> colors,
            Double minRating,
            String gender,
            String search,
            Boolean onSale,
            Boolean bestseller,
            Boolean isNew,
            Boolean lowStock,
            Pageable pageable) {

        org.springframework.data.jpa.domain.Specification<Product> spec = com.example.novawear.specification.ProductSpecification
                .filter(
                        categoryIds, minPrice, maxPrice, sizes, colors, minRating, gender,
                        search, onSale, bestseller, isNew, lowStock);

        return productRepository.findAll(spec, pageable).map(this::toDto);
    }

    @Transactional
    public ProductDto create(ProductDto dto) {
        Category cat = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        // Generate slug if not provided
        String slug = dto.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = generateSlug(dto.getName());
        }
        Product p = Product.builder()
                .name(dto.getName())
                .slug(slug)
                .price(dto.getPrice())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .category(cat)
                .stock(dto.getStock() != null ? dto.getStock() : 0)
                .salePrice(dto.getSalePrice())
                .featured(Boolean.TRUE.equals(dto.getFeatured()))
                .bestseller(Boolean.TRUE.equals(dto.getBestseller()))
                .isNew(Boolean.TRUE.equals(dto.getIsNew()))
                .build();
        // Set gender
        if (dto.getGender() != null && !dto.getGender().isBlank()) {
            try {
                p.setGender(Product.Gender.valueOf(dto.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Invalid gender, use default UNISEX
                p.setGender(Product.Gender.UNISEX);
            }
        } else {
            p.setGender(Product.Gender.UNISEX);
        }
        p.setSizes(serializeSizes(dto.getSizes()));
        p.setColors(serializeColors(dto.getColors()));
        // Set images: use images list if provided, otherwise fallback to imageUrl
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            p.setImages(serializeImages(dto.getImages()));
            // Also set imageUrl to first image for backward compatibility
            if (p.getImageUrl() == null || p.getImageUrl().isBlank()) {
                p.setImageUrl(dto.getImages().get(0));
            }
        } else if (dto.getImageUrl() != null && !dto.getImageUrl().isBlank()) {
            // If only imageUrl provided, convert to images array
            p.setImages(serializeImages(Collections.singletonList(dto.getImageUrl())));
        }
        p = productRepository.save(p);
        return toDto(p);
    }

    @Transactional
    public ProductDto update(Long id, ProductDto dto) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        if (dto.getCategoryId() != null) {
            Category cat = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            p.setCategory(cat);
        }
        if (dto.getName() != null) {
            p.setName(dto.getName());
            // Regenerate slug if name changed and slug not explicitly provided
            if (dto.getSlug() == null || dto.getSlug().isBlank()) {
                p.setSlug(generateSlug(dto.getName()));
            }
        }
        if (dto.getSlug() != null && !dto.getSlug().isBlank()) {
            p.setSlug(dto.getSlug());
        }
        if (dto.getPrice() != null)
            p.setPrice(dto.getPrice());
        if (dto.getDescription() != null)
            p.setDescription(dto.getDescription());
        if (dto.getImageUrl() != null)
            p.setImageUrl(dto.getImageUrl());
        if (dto.getStock() != null)
            p.setStock(dto.getStock());
        p.setSalePrice(dto.getSalePrice()); // null = bỏ giảm giá
        if (dto.getFeatured() != null)
            p.setFeatured(dto.getFeatured());
        if (dto.getBestseller() != null)
            p.setBestseller(dto.getBestseller());
        if (dto.getIsNew() != null)
            p.setIsNew(dto.getIsNew());
        if (dto.getSizes() != null)
            p.setSizes(serializeSizes(dto.getSizes()));
        if (dto.getColors() != null)
            p.setColors(serializeColors(dto.getColors()));
        // Update gender
        if (dto.getGender() != null && !dto.getGender().isBlank()) {
            try {
                p.setGender(Product.Gender.valueOf(dto.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Invalid gender, keep current value or use default
                if (p.getGender() == null) {
                    p.setGender(Product.Gender.UNISEX);
                }
            }
        }
        // Update images: use images list if provided, otherwise fallback to imageUrl
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            p.setImages(serializeImages(dto.getImages()));
            // Also update imageUrl to first image for backward compatibility
            if (p.getImageUrl() == null || p.getImageUrl().isBlank()) {
                p.setImageUrl(dto.getImages().get(0));
            }
        } else if (dto.getImageUrl() != null && !dto.getImageUrl().isBlank()) {
            // If only imageUrl provided, convert to images array
            p.setImages(serializeImages(Collections.singletonList(dto.getImageUrl())));
        }
        p = productRepository.save(p);
        return toDto(p);
    }

    /** Cập nhật nhanh cờ nổi bật / bán chạy (từ bảng quản lý) */
    @Transactional
    public ProductDto updateFlags(Long id, ProductFlagsDto dto) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        if (dto.getFeatured() != null)
            p.setFeatured(dto.getFeatured());
        if (dto.getBestseller() != null)
            p.setBestseller(dto.getBestseller());
        p = productRepository.save(p);
        return toDto(p);
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    /**
     * Sản phẩm liên quan: cùng category, tối đa 8 sản phẩm
     */
    @Transactional(readOnly = true)
    public List<ProductDto> findRelatedProducts(Long productId, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        List<Product> related = productRepository.findRelatedByCategory(
                product.getCategory().getId(), productId, pageable);

        return related.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Sản phẩm tương tự: cùng khoảng giá (+-30%), tối đa 8 sản phẩm
     */
    @Transactional(readOnly = true)
    public List<ProductDto> findSimilarProducts(Long productId, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        java.math.BigDecimal price = product.getPrice();
        java.math.BigDecimal minPrice = price.multiply(java.math.BigDecimal.valueOf(0.7));
        java.math.BigDecimal maxPrice = price.multiply(java.math.BigDecimal.valueOf(1.3));

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        List<Product> similar = productRepository.findSimilarByPrice(
                productId, price, minPrice, maxPrice, pageable);

        return similar.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public com.example.novawear.dto.ProductFiltersDto getAvailableFilters() {
        List<Product> products = productRepository.findAll();

        java.math.BigDecimal minPrice = null;
        java.math.BigDecimal maxPrice = null;
        java.util.Set<String> sizes = new java.util.HashSet<>();
        java.util.Map<String, String> colorMap = new java.util.HashMap<>();

        for (Product p : products) {
            // Price
            java.math.BigDecimal price = p.getSalePrice() != null ? p.getSalePrice() : p.getPrice();
            if (minPrice == null || price.compareTo(minPrice) < 0) {
                minPrice = price;
            }
            if (maxPrice == null || price.compareTo(maxPrice) > 0) {
                maxPrice = price;
            }

            // Sizes
            sizes.addAll(parseSizes(p.getSizes()));

            // Colors
            List<ProductColorDto> productColors = parseColors(p.getColors());
            for (ProductColorDto c : productColors) {
                if (c.getName() != null) {
                    colorMap.putIfAbsent(c.getName(), c.getHex() != null && !c.getHex().isBlank() ? c.getHex() : "#CCCCCC");
                }
            }
        }

        List<ProductColorDto> colorList = colorMap.entrySet().stream()
                .map(e -> new ProductColorDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return new com.example.novawear.dto.ProductFiltersDto(
                minPrice, maxPrice, new java.util.ArrayList<>(sizes), colorList);
    }
}
