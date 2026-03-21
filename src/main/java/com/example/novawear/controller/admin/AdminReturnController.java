package com.example.novawear.controller.admin;

import com.example.novawear.dto.ReturnRequestDto;
import com.example.novawear.service.ReturnRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/returns")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReturnController {

    private final ReturnRequestService returnRequestService;

    @GetMapping
    public ResponseEntity<Page<ReturnRequestDto>> list(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(returnRequestService.findAll(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReturnRequestDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(returnRequestService.getById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReturnRequestDto> updateStatus(@PathVariable Long id,
                                                          @RequestParam String status,
                                                          @RequestParam(required = false) String adminNote) {
        return ResponseEntity.ok(returnRequestService.updateStatus(id, status, adminNote));
    }
}
