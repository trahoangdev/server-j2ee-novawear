package com.example.novawear.controller.admin;

import com.example.novawear.dto.FlashSaleDto;
import com.example.novawear.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/flash-sales")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminFlashSaleController {

    private final FlashSaleService flashSaleService;

    @GetMapping
    public ResponseEntity<Page<FlashSaleDto>> list(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(flashSaleService.findAll(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlashSaleDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(flashSaleService.getById(id));
    }

    @PostMapping
    public ResponseEntity<FlashSaleDto> create(@RequestBody FlashSaleDto dto) {
        return ResponseEntity.ok(flashSaleService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlashSaleDto> update(@PathVariable Long id, @RequestBody FlashSaleDto dto) {
        return ResponseEntity.ok(flashSaleService.update(id, dto));
    }

    @PostMapping("/{id}/products")
    public ResponseEntity<FlashSaleDto> addProduct(@PathVariable Long id,
                                                    @RequestParam Long productId,
                                                    @RequestParam(required = false) Integer quantity) {
        return ResponseEntity.ok(flashSaleService.addProduct(id, productId, quantity));
    }

    @DeleteMapping("/{id}/products/{itemId}")
    public ResponseEntity<Void> removeProduct(@PathVariable Long id, @PathVariable Long itemId) {
        flashSaleService.removeProduct(id, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        flashSaleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
