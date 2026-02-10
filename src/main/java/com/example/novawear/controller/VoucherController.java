package com.example.novawear.controller;

import com.example.novawear.dto.VoucherDto;
import com.example.novawear.dto.VoucherValidateResponse;
import com.example.novawear.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@Tag(name = "Voucher", description = "API voucher cho khách hàng")
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping("/available")
    @Operation(summary = "Lấy danh sách voucher đang khả dụng")
    public ResponseEntity<List<VoucherDto>> getAvailableVouchers(
            @RequestParam(required = false) BigDecimal orderTotal,
            Authentication auth) {
        String username = auth != null ? auth.getName() : null;

        List<VoucherDto> vouchers;
        if (orderTotal != null) {
            vouchers = voucherService.getApplicableVouchers(orderTotal, username);
        } else {
            vouchers = voucherService.getActiveVouchers();
        }

        return ResponseEntity.ok(vouchers);
    }

    @PostMapping("/validate")
    @Operation(summary = "Kiểm tra mã voucher")
    public ResponseEntity<VoucherValidateResponse> validateVoucher(
            @RequestParam String code,
            @RequestParam BigDecimal orderTotal,
            Authentication auth) {
        String username = auth != null ? auth.getName() : null;
        VoucherValidateResponse response = voucherService.validateVoucher(code, orderTotal, username);
        return ResponseEntity.ok(response);
    }
}
