package com.example.novawear.controller;

import com.example.novawear.dto.BundleDto;
import com.example.novawear.service.BundleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bundles")
@RequiredArgsConstructor
public class BundleController {

    private final BundleService bundleService;

    @GetMapping
    public ResponseEntity<List<BundleDto>> getActive() {
        return ResponseEntity.ok(bundleService.findActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BundleDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bundleService.getById(id));
    }
}
