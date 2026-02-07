package com.example.novawear.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Size(max = 200)
    @Column(length = 200)
    private String description;

    /**
     * Loại giảm giá: PERCENT hoặc FIXED
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    /**
     * Giá trị giảm: nếu PERCENT thì là % (0-100), nếu FIXED thì là số tiền
     */
    @NotNull
    @DecimalMin("0")
    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    /**
     * Giá trị đơn hàng tối thiểu để áp dụng voucher
     */
    @DecimalMin("0")
    @Column(name = "min_order_value", precision = 12, scale = 2)
    private BigDecimal minOrderValue;

    /**
     * Giảm tối đa (chỉ áp dụng khi discountType = PERCENT)
     */
    @DecimalMin("0")
    @Column(name = "max_discount", precision = 12, scale = 2)
    private BigDecimal maxDiscount;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    /**
     * Số lượt sử dụng tối đa (null = không giới hạn)
     */
    @Column(name = "usage_limit")
    private Integer usageLimit;

    /**
     * Số lượt đã sử dụng
     */
    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    /**
     * Số lượt tối đa mỗi user có thể dùng (null = không giới hạn)
     */
    @Column(name = "usage_limit_per_user")
    private Integer usageLimitPerUser;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    public enum DiscountType {
        PERCENT, // Giảm theo phần trăm
        FIXED // Giảm số tiền cố định
    }

    /**
     * Kiểm tra voucher có còn hạn và còn lượt dùng không
     */
    public boolean isValid() {
        Instant now = Instant.now();

        // Check active
        if (!Boolean.TRUE.equals(active))
            return false;

        // Check dates
        if (startDate != null && now.isBefore(startDate))
            return false;
        if (endDate != null && now.isAfter(endDate))
            return false;

        // Check usage limit
        if (usageLimit != null && usedCount >= usageLimit)
            return false;

        return true;
    }

    /**
     * Tính số tiền giảm dựa trên tổng đơn hàng
     */
    public BigDecimal calculateDiscount(BigDecimal orderTotal) {
        if (minOrderValue != null && orderTotal.compareTo(minOrderValue) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        if (discountType == DiscountType.PERCENT) {
            discount = orderTotal.multiply(discountValue).divide(BigDecimal.valueOf(100));
            if (maxDiscount != null && discount.compareTo(maxDiscount) > 0) {
                discount = maxDiscount;
            }
        } else {
            discount = discountValue;
        }

        // Không giảm quá tổng đơn hàng
        if (discount.compareTo(orderTotal) > 0) {
            discount = orderTotal;
        }

        return discount;
    }
}
