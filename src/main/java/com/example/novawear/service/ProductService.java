package com.example.novawear.service;

import com.example.novawear.dto.ProductDto;
import com.example.novawear.entity.Category;
import com.example.novawear.entity.Product;
import com.example.novawear.repository.CategoryRepository;
import com.example.novawear.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public ProductDto getById(Long id) {
        Product p = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        return ProductDto.from(p);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductDto::from);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable).map(ProductDto::from);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return productRepository.findAll(pageable).map(ProductDto::from);
        }
        return productRepository.searchByName(keyword.trim(), pageable).map(ProductDto::from);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> findFeatured() {
        return productRepository.findTop8ByOrderByIdDesc().stream().map(ProductDto::from).collect(Collectors.toList());
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
                .build();
        p = productRepository.save(p);
        return ProductDto.from(p);
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
        p = productRepository.save(p);
        return ProductDto.from(p);
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }
}
