package com.example.novawear.controller;

import com.example.novawear.dto.ReturnRequestDto;
import com.example.novawear.service.CloudinaryService;
import com.example.novawear.service.ReturnRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnRequestController {

    private final ReturnRequestService returnRequestService;
    private final CloudinaryService cloudinaryService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ReturnRequestDto> create(@AuthenticationPrincipal UserDetails user,
                                                    @RequestParam Long orderId,
                                                    @RequestParam String reason,
                                                    @RequestPart(required = false) List<MultipartFile> images) throws IOException {
        List<String> imageUrls = new ArrayList<>();
        if (images != null) {
            int max = Math.min(images.size(), 5);
            for (int i = 0; i < max; i++) {
                if (!images.get(i).isEmpty()) {
                    imageUrls.add(cloudinaryService.uploadFile(images.get(i)));
                }
            }
        }
        return ResponseEntity.ok(returnRequestService.create(user.getUsername(), orderId, reason, imageUrls));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<ReturnRequestDto>> myReturns(@AuthenticationPrincipal UserDetails user,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(returnRequestService.findByUser(user.getUsername(), PageRequest.of(page, size)));
    }
}
