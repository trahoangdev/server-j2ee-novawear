package com.example.novawear.controller.admin;

import com.example.novawear.dto.VoucherCreateRequest;
import com.example.novawear.dto.VoucherDto;
import com.example.novawear.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Voucher", description = "API quản lý voucher cho admin")
public class AdminVoucherController {

    private final VoucherService voucherService;

    @GetMapping
    @Operation(summary = "Danh sách voucher có phân trang và tìm kiếm")
    public ResponseEntity<Page<VoucherDto>> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<VoucherDto> result = voucherService.search(
                keyword.isBlank() ? null : keyword,
                active,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết voucher")
    public ResponseEntity<VoucherDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(voucherService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Tạo voucher mới")
    public ResponseEntity<VoucherDto> create(@Valid @RequestBody VoucherCreateRequest request) {
        return ResponseEntity.ok(voucherService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật voucher")
    public ResponseEntity<VoucherDto> update(
            @PathVariable Long id,
            @Valid @RequestBody VoucherCreateRequest request) {
        return ResponseEntity.ok(voucherService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa voucher")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        voucherService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Bật/tắt voucher")
    public ResponseEntity<VoucherDto> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(voucherService.toggleActive(id));
    }
}
