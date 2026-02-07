package com.example.novawear.dto;

import com.example.novawear.entity.Voucher;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherCreateRequest {

    @NotBlank(message = "Mã voucher không được để trống")
    @Size(max = 50, message = "Mã voucher tối đa 50 ký tự")
    private String code;

    @Size(max = 200, message = "Mô tả tối đa 200 ký tự")
    private String description;

    @NotNull(message = "Loại giảm giá không được để trống")
    private Voucher.DiscountType discountType;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0", message = "Giá trị giảm phải >= 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0", message = "Giá trị đơn tối thiểu phải >= 0")
    private BigDecimal minOrderValue;

    @DecimalMin(value = "0", message = "Giảm tối đa phải >= 0")
    private BigDecimal maxDiscount;

    private Instant startDate;
    private Instant endDate;

    private Integer usageLimit;
    private Integer usageLimitPerUser;

    @Builder.Default
    private Boolean active = true;
}
