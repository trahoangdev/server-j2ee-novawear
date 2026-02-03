package com.example.novawear.service;

import com.example.novawear.dto.CartAddRequest;
import com.example.novawear.dto.CartItemDto;
import com.example.novawear.entity.Product;
import com.example.novawear.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cart per user (username). For production consider Redis or DB.
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final ProductRepository productRepository;

    private final Map<String, List<CartItemDto>> carts = new ConcurrentHashMap<>();

    public List<CartItemDto> getCart(String username) {
        return new ArrayList<>(carts.getOrDefault(username, List.of()));
    }

    public List<CartItemDto> add(String username, CartAddRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + request.getProductId()));
        List<CartItemDto> items = new ArrayList<>(carts.getOrDefault(username, List.of()));
        boolean found = false;
        for (CartItemDto item : items) {
            if (item.getProductId().equals(request.getProductId())) {
                item.setQuantity(item.getQuantity() + request.getQuantity());
                item.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                found = true;
                break;
            }
        }
        if (!found) {
            CartItemDto newItem = new CartItemDto();
            newItem.setProductId(product.getId());
            newItem.setProductName(product.getName());
            newItem.setImageUrl(product.getImageUrl());
            newItem.setPrice(product.getPrice());
            newItem.setQuantity(request.getQuantity());
            newItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
            items.add(newItem);
        }
        carts.put(username, items);
        return getCart(username);
    }

    public List<CartItemDto> update(String username, Long productId, int quantity) {
        if (quantity <= 0) {
            return remove(username, productId);
        }
        List<CartItemDto> items = new ArrayList<>(carts.getOrDefault(username, List.of()));
        for (CartItemDto item : items) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(quantity);
                item.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(quantity)));
                break;
            }
        }
        carts.put(username, items);
        return getCart(username);
    }

    public List<CartItemDto> remove(String username, Long productId) {
        List<CartItemDto> items = new ArrayList<>(carts.getOrDefault(username, List.of()));
        items.removeIf(item -> item.getProductId().equals(productId));
        carts.put(username, items);
        return getCart(username);
    }

    public void clear(String username) {
        carts.remove(username);
    }
}
