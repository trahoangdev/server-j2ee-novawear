package com.example.novawear.dto;

import com.example.novawear.entity.Voucher;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherDto {
    private Long id;
    private String code;
    private String description;
    private Voucher.DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscount;
    private Instant startDate;
    private Instant endDate;
    private Integer usageLimit;
    private Integer usedCount;
    private Integer usageLimitPerUser;
    private Boolean active;
    private Instant createdAt;

    // Computed fields
    private Boolean isValid;
    private String discountDisplay;

    public static VoucherDto from(Voucher v) {
        if (v == null)
            return null;

        String display = "";
        if (v.getDiscountType() == Voucher.DiscountType.PERCENT) {
            display = v.getDiscountValue().stripTrailingZeros().toPlainString() + "%";
            if (v.getMaxDiscount() != null) {
                display += " (tối đa " + formatCurrency(v.getMaxDiscount()) + ")";
            }
        } else {
            display = formatCurrency(v.getDiscountValue());
        }

        return VoucherDto.builder()
                .id(v.getId())
                .code(v.getCode())
                .description(v.getDescription())
                .discountType(v.getDiscountType())
                .discountValue(v.getDiscountValue())
                .minOrderValue(v.getMinOrderValue())
                .maxDiscount(v.getMaxDiscount())
                .startDate(v.getStartDate())
                .endDate(v.getEndDate())
                .usageLimit(v.getUsageLimit())
                .usedCount(v.getUsedCount())
                .usageLimitPerUser(v.getUsageLimitPerUser())
                .active(v.getActive())
                .createdAt(v.getCreatedAt())
                .isValid(v.isValid())
                .discountDisplay(display)
                .build();
    }

    private static String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f₫", amount);
    }
}
