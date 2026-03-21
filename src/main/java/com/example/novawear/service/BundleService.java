package com.example.novawear.service;

import com.example.novawear.dto.BundleDto;
import com.example.novawear.entity.BundleItem;
import com.example.novawear.entity.Product;
import com.example.novawear.entity.ProductBundle;
import com.example.novawear.repository.ProductBundleRepository;
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
public class BundleService {

    private final ProductBundleRepository bundleRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<BundleDto> findActive() {
        return bundleRepository.findByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(BundleDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<BundleDto> findAll(Pageable pageable) {
        return bundleRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(BundleDto::from);
    }

    @Transactional(readOnly = true)
    public BundleDto getById(Long id) {
        ProductBundle bundle = bundleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bundle not found: " + id));
        return BundleDto.from(bundle);
    }

    @Transactional
    public BundleDto create(BundleDto dto) {
        ProductBundle bundle = ProductBundle.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .discountPercent(dto.getDiscountPercent())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
        return BundleDto.from(bundleRepository.save(bundle));
    }

    @Transactional
    public BundleDto update(Long id, BundleDto dto) {
        ProductBundle bundle = bundleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bundle not found: " + id));
        bundle.setName(dto.getName());
        bundle.setDescription(dto.getDescription());
        bundle.setImageUrl(dto.getImageUrl());
        bundle.setDiscountPercent(dto.getDiscountPercent());
        if (dto.getActive() != null) bundle.setActive(dto.getActive());
        return BundleDto.from(bundleRepository.save(bundle));
    }

    @Transactional
    public BundleDto addItem(Long bundleId, Long productId, Integer quantity) {
        ProductBundle bundle = bundleRepository.findById(bundleId)
                .orElseThrow(() -> new IllegalArgumentException("Bundle not found: " + bundleId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        BundleItem item = BundleItem.builder()
                .bundle(bundle)
                .product(product)
                .quantity(quantity != null ? quantity : 1)
                .build();
        bundle.getItems().add(item);
        return BundleDto.from(bundleRepository.save(bundle));
    }

    @Transactional
    public void removeItem(Long bundleId, Long itemId) {
        ProductBundle bundle = bundleRepository.findById(bundleId)
                .orElseThrow(() -> new IllegalArgumentException("Bundle not found: " + bundleId));
        bundle.getItems().removeIf(item -> item.getId().equals(itemId));
        bundleRepository.save(bundle);
    }

    @Transactional
    public void delete(Long id) {
        bundleRepository.deleteById(id);
    }
}
