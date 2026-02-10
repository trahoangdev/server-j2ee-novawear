package com.example.novawear.controller.admin;

import com.example.novawear.dto.BannerDto;
import com.example.novawear.service.BannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBannerController {

    private final BannerService bannerService;

    @GetMapping
    public ResponseEntity<List<BannerDto>> findAll() {
        return ResponseEntity.ok(bannerService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BannerDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bannerService.getById(id));
    }

    @PostMapping
    public ResponseEntity<BannerDto> create(@Valid @RequestBody BannerDto dto) {
        return ResponseEntity.ok(bannerService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BannerDto> update(@PathVariable Long id, @Valid @RequestBody BannerDto dto) {
        return ResponseEntity.ok(bannerService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
