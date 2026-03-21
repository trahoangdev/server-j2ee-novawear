package com.example.novawear.controller.admin;

import com.example.novawear.dto.OrderDto;
import com.example.novawear.entity.Order;
import com.example.novawear.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<Page<OrderDto>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Instant fromDate,
            @RequestParam(required = false) Instant toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());

        Order.OrderStatus s = null;
        if (status != null && !status.isBlank()) {
            try {
                s = Order.OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        return ResponseEntity.ok(orderService.search(search, s, fromDate, toDate, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Order.OrderStatus s = Order.OrderStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(orderService.updateStatus(id, s));
    }

    @PatchMapping("/{id}/tracking")
    public ResponseEntity<OrderDto> updateTracking(@PathVariable Long id,
                                                    @RequestParam(required = false) String trackingNumber,
                                                    @RequestParam(required = false) String carrier) {
        return ResponseEntity.ok(orderService.updateTracking(id, trackingNumber, carrier));
    }
}
