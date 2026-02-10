package com.example.novawear.controller;

import com.example.novawear.dto.ProductDto;
import com.example.novawear.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductDto>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean onSale,
            @RequestParam(required = false) Boolean bestseller,
            @RequestParam(required = false) Boolean isNew,
            @RequestParam(required = false) Boolean lowStock,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) List<String> sizes,
            @RequestParam(required = false) List<String> colors,
            @RequestParam(required = false) Double rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        List<Long> categoryIds = categoryId != null ? List.of(categoryId) : null;

        return ResponseEntity.ok(productService.findWithFilters(
                categoryIds,
                minPrice,
                maxPrice,
                sizes,
                colors,
                rating,
                gender,
                search,
                onSale,
                bestseller,
                isNew,
                lowStock,
                pageable));
    }

    @GetMapping("/filters")
    public ResponseEntity<com.example.novawear.dto.ProductFiltersDto> getFilters() {
        return ResponseEntity.ok(productService.getAvailableFilters());
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductDto>> featured() {
        return ResponseEntity.ok(productService.findFeatured());
    }

    @GetMapping("/bestseller")
    public ResponseEntity<List<ProductDto>> bestseller() {
        return ResponseEntity.ok(productService.findBestseller());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductDto> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getBySlug(slug));
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<List<ProductDto>> getRelatedProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(productService.findRelatedProducts(id, Math.min(limit, 12)));
    }

    @GetMapping("/{id}/similar")
    public ResponseEntity<List<ProductDto>> getSimilarProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(productService.findSimilarProducts(id, Math.min(limit, 12)));
    }
}
