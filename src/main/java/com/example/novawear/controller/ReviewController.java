package com.example.novawear.controller;

import com.example.novawear.dto.ReviewDto;
import com.example.novawear.service.CloudinaryService;
import com.example.novawear.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final CloudinaryService cloudinaryService;

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

    @PostMapping(value = "/product/{productId}/with-images", consumes = "multipart/form-data")
    public ResponseEntity<ReviewDto> createWithImages(@AuthenticationPrincipal UserDetails user,
                                                      @PathVariable Long productId,
                                                      @RequestParam int rating,
                                                      @RequestParam(required = false) String comment,
                                                      @RequestPart(required = false) List<MultipartFile> images) throws IOException {
        if (user == null) return ResponseEntity.status(401).build();

        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            int maxImages = Math.min(images.size(), 5);
            for (int i = 0; i < maxImages; i++) {
                MultipartFile file = images.get(i);
                if (!file.isEmpty()) {
                    String url = cloudinaryService.uploadFile(file);
                    imageUrls.add(url);
                }
            }
        }

        ReviewDto dto = new ReviewDto();
        dto.setRating(rating);
        dto.setComment(comment != null ? comment : "");
        return ResponseEntity.ok(reviewService.createWithImages(user.getUsername(), productId, dto, imageUrls));
    }
}
