package com.example.novawear.controller;

import com.example.novawear.dto.CartAddRequest;
import com.example.novawear.dto.CartItemDto;
import com.example.novawear.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<List<CartItemDto>> getCart(@AuthenticationPrincipal UserDetails user) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(cartService.getCart(user.getUsername()));
    }

    @PostMapping("/add")
    public ResponseEntity<List<CartItemDto>> add(@AuthenticationPrincipal UserDetails user,
                                                  @Valid @RequestBody CartAddRequest request) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(cartService.add(user.getUsername(), request));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<List<CartItemDto>> update(@AuthenticationPrincipal UserDetails user,
                                                    @PathVariable Long productId,
                                                    @RequestParam int quantity) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(cartService.update(user.getUsername(), productId, quantity));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<List<CartItemDto>> remove(@AuthenticationPrincipal UserDetails user,
                                                     @PathVariable Long productId) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(cartService.remove(user.getUsername(), productId));
    }
}
