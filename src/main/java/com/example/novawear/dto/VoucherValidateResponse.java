package com.example.novawear.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * Response khi validate voucher code
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherValidateResponse {
    private Boolean valid;
    private String message;
    private VoucherDto voucher;
    private BigDecimal discountAmount;
    private BigDecimal finalTotal;

    public static VoucherValidateResponse invalid(String message) {
        return VoucherValidateResponse.builder()
                .valid(false)
                .message(message)
                .build();
    }

    public static VoucherValidateResponse valid(VoucherDto voucher, BigDecimal discount, BigDecimal finalTotal) {
        return VoucherValidateResponse.builder()
                .valid(true)
                .message("Áp dụng voucher thành công!")
                .voucher(voucher)
                .discountAmount(discount)
                .finalTotal(finalTotal)
                .build();
    }
}
