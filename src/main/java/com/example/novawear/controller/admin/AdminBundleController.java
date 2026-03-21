package com.example.novawear.controller.admin;

import com.example.novawear.dto.BundleDto;
import com.example.novawear.service.BundleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/bundles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBundleController {

    private final BundleService bundleService;

    @GetMapping
    public ResponseEntity<Page<BundleDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bundleService.findAll(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BundleDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bundleService.getById(id));
    }

    @PostMapping
    public ResponseEntity<BundleDto> create(@RequestBody BundleDto dto) {
        return ResponseEntity.ok(bundleService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BundleDto> update(@PathVariable Long id, @RequestBody BundleDto dto) {
        return ResponseEntity.ok(bundleService.update(id, dto));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<BundleDto> addItem(
            @PathVariable Long id,
            @RequestParam Long productId,
            @RequestParam(required = false, defaultValue = "1") Integer quantity) {
        return ResponseEntity.ok(bundleService.addItem(id, productId, quantity));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long id, @PathVariable Long itemId) {
        bundleService.removeItem(id, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bundleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
