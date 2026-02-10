package com.example.novawear.controller.admin;

import com.example.novawear.dto.ReviewCreateRequest;
import com.example.novawear.dto.ReviewDto;
import com.example.novawear.dto.ReviewUpdateRequest;
import com.example.novawear.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<Page<ReviewDto>> list(
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        if (productId != null) {
            return ResponseEntity.ok(reviewService.findByProductId(productId, pageable));
        }
        return ResponseEntity.ok(reviewService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ReviewDto> create(@Valid @RequestBody ReviewCreateRequest request) {
        return ResponseEntity.ok(reviewService.createByAdmin(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> update(@PathVariable Long id, @Valid @RequestBody ReviewUpdateRequest request) {
        return ResponseEntity.ok(reviewService.update(id, request));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ReviewDto> approve(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean approved) {
        return ResponseEntity.ok(reviewService.approve(id, approved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
