package com.example.novawear.controller;

import com.example.novawear.dto.ReviewDto;
import com.example.novawear.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDto>> getByProduct(@PathVariable Long productId,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reviewService.findByProductIdApproved(productId, pageable));
    }

    @PostMapping("/product/{productId}")
    public ResponseEntity<ReviewDto> create(@AuthenticationPrincipal UserDetails user,
                                           @PathVariable Long productId,
                                           @Valid @RequestBody ReviewDto dto) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(reviewService.create(user.getUsername(), productId, dto));
    }
}
