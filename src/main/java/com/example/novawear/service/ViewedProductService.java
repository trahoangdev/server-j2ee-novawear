package com.example.novawear.service;

import com.example.novawear.dto.ViewedProductDto;
import com.example.novawear.entity.Product;
import com.example.novawear.entity.User;
import com.example.novawear.entity.ViewedProduct;
import com.example.novawear.repository.ProductRepository;
import com.example.novawear.repository.UserRepository;
import com.example.novawear.repository.ViewedProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ViewedProductService {

    private static final int MAX_VIEWED = 20;

    private final ViewedProductRepository viewedProductRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * Lay danh sach san pham da xem cua user (toi da 20 san pham moi nhat).
     */
    @Transactional(readOnly = true)
    public List<ViewedProductDto> getViewedProducts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return viewedProductRepository
                .findByUserIdOrderByViewedAtDesc(user.getId(), PageRequest.of(0, MAX_VIEWED))
                .stream()
                .map(ViewedProductDto::from)
                .collect(Collectors.toList());
    }

    /**
     * Ghi nhan user da xem mot san pham.
     * Neu da xem roi -> cap nhat viewedAt.
     * Neu chua xem -> them moi (neu chua du 20) hoac thay the san pham cu nhat.
     */
    @Transactional
    public ViewedProductDto recordViewed(String username, Long productId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        return viewedProductRepository.findByUserIdAndProductId(user.getId(), productId)
                .map(existing -> {
                    existing.setViewedAt(Instant.now());
                    return ViewedProductDto.from(viewedProductRepository.save(existing));
                })
                .orElseGet(() -> {
                    long count = viewedProductRepository.countByUserId(user.getId());
                    if (count >= MAX_VIEWED) {
                        // Xoa san pham cu nhat de them san pham moi
                        viewedProductRepository
                                .findByUserIdOrderByViewedAtDesc(user.getId(), PageRequest.of(0, 1))
                                .stream()
                                .findFirst()
                                .ifPresent(oldest -> viewedProductRepository.delete(oldest));
                    }
                    ViewedProduct vp = ViewedProduct.builder()
                            .user(user)
                            .product(product)
                            .viewedAt(Instant.now())
                            .build();
                    return ViewedProductDto.from(viewedProductRepository.save(vp));
                });
    }

    /**
     * Xoa mot san pham khoi lich su xem.
     */
    @Transactional
    public void removeViewed(String username, Long productId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        viewedProductRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }

    /**
     * Xoa toan bo lich su xem cua user.
     */
    @Transactional
    public void clearAll(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        viewedProductRepository.deleteAllByUserId(user.getId());
    }

    /**
     * Lay so luong san pham da xem cua user.
     */
    @Transactional(readOnly = true)
    public long getViewedCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return viewedProductRepository.countByUserId(user.getId());
    }
}
