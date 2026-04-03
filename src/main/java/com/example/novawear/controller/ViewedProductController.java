package com.example.novawear.controller;

import com.example.novawear.dto.ViewedProductDto;
import com.example.novawear.service.ViewedProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/viewed-products")
@RequiredArgsConstructor
public class ViewedProductController {

    private final ViewedProductService viewedProductService;

    /**
     * GET /api/viewed-products  – Lay danh sach san pham da xem
     */
    @GetMapping
    public ResponseEntity<List<ViewedProductDto>> getViewedProducts(
            @AuthenticationPrincipal UserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        List<ViewedProductDto> products = viewedProductService.getViewedProducts(user.getUsername());
        return ResponseEntity.ok(products);
    }

    /**
     * POST /api/viewed-products  – Ghi nhan da xem san pham
     * Body: { "productId": 123 }
     */
    @PostMapping
    public ResponseEntity<ViewedProductDto> recordViewed(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody Map<String, Long> body) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        Long productId = body.get("productId");
        if (productId == null) {
            return ResponseEntity.badRequest().build();
        }
        ViewedProductDto result = viewedProductService.recordViewed(user.getUsername(), productId);
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/viewed-products/:productId  – Xoa mot san pham khoi lich su
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeViewed(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long productId) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        viewedProductService.removeViewed(user.getUsername(), productId);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/viewed-products  – Xoa toan bo lich su xem
     */
    @DeleteMapping
    public ResponseEntity<Void> clearAll(
            @AuthenticationPrincipal UserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        viewedProductService.clearAll(user.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/viewed-products/count  – Lay so luong san pham da xem
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getViewedCount(
            @AuthenticationPrincipal UserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        long count = viewedProductService.getViewedCount(user.getUsername());
        return ResponseEntity.ok(Map.of("count", count));
    }
}
