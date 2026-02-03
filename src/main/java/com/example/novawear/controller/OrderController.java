package com.example.novawear.controller;

import com.example.novawear.dto.CheckoutRequest;
import com.example.novawear.dto.OrderDto;
import com.example.novawear.service.CartService;
import com.example.novawear.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<Page<OrderDto>> myOrders(@AuthenticationPrincipal UserDetails user,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        if (user == null) return ResponseEntity.status(401).build();
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        return ResponseEntity.ok(orderService.findByUsername(user.getUsername(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getById(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(orderService.getById(id));
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderDto> checkout(@AuthenticationPrincipal UserDetails user,
                                            @Valid @RequestBody CheckoutRequest request) {
        if (user == null) return ResponseEntity.status(401).build();
        OrderDto order = orderService.checkout(user.getUsername(), request);
        cartService.clear(user.getUsername());
        return ResponseEntity.ok(order);
    }
}
