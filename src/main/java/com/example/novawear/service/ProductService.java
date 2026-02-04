package com.example.novawear.service;

import com.example.novawear.dto.ProductColorDto;
import com.example.novawear.dto.ProductDto;
import com.example.novawear.dto.ProductFlagsDto;
import com.example.novawear.entity.Category;
import com.example.novawear.entity.Product;
import com.example.novawear.repository.CategoryRepository;
import com.example.novawear.repository.ProductRepository;
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
    private final ObjectMapper objectMapper;

    private List<String> parseSizes(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<ProductColorDto> parseColors(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String serializeSizes(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }

    private String serializeColors(List<ProductColorDto> list) {
        if (list == null || list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }

    private ProductDto toDto(Product p) {
        ProductDto dto = ProductDto.from(p);
        dto.setSizes(parseSizes(p.getSizes()));
        dto.setColors(parseColors(p.getColors()));
        return dto;
    }

    @Transactional(readOnly = true)
    public ProductDto getById(Long id) {
        Product p = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        return toDto(p);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findAllFiltered(Boolean onSale, Boolean bestseller, Pageable pageable) {
        return productRepository.findAllFiltered(onSale, bestseller, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable).map(this::toDto);
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

    @Transactional
    public ProductDto create(ProductDto dto) {
        Category cat = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new IllegalArgumentException("Category not found"));
        Product p = Product.builder()
                .name(dto.getName())
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
        p.setSizes(serializeSizes(dto.getSizes()));
        p.setColors(serializeColors(dto.getColors()));
        p = productRepository.save(p);
        return toDto(p);
    }

    @Transactional
    public ProductDto update(Long id, ProductDto dto) {
        Product p = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        if (dto.getCategoryId() != null) {
            Category cat = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new IllegalArgumentException("Category not found"));
            p.setCategory(cat);
        }
        if (dto.getName() != null) p.setName(dto.getName());
        if (dto.getPrice() != null) p.setPrice(dto.getPrice());
        if (dto.getDescription() != null) p.setDescription(dto.getDescription());
        if (dto.getImageUrl() != null) p.setImageUrl(dto.getImageUrl());
        if (dto.getStock() != null) p.setStock(dto.getStock());
        p.setSalePrice(dto.getSalePrice()); // null = bỏ giảm giá
        if (dto.getFeatured() != null) p.setFeatured(dto.getFeatured());
        if (dto.getBestseller() != null) p.setBestseller(dto.getBestseller());
        if (dto.getIsNew() != null) p.setIsNew(dto.getIsNew());
        if (dto.getSizes() != null) p.setSizes(serializeSizes(dto.getSizes()));
        if (dto.getColors() != null) p.setColors(serializeColors(dto.getColors()));
        p = productRepository.save(p);
        return toDto(p);
    }

    /** Cập nhật nhanh cờ nổi bật / bán chạy (từ bảng quản lý) */
    @Transactional
    public ProductDto updateFlags(Long id, ProductFlagsDto dto) {
        Product p = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        if (dto.getFeatured() != null) p.setFeatured(dto.getFeatured());
        if (dto.getBestseller() != null) p.setBestseller(dto.getBestseller());
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
}
