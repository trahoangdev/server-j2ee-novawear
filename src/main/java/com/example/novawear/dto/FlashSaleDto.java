package com.example.novawear.dto;

import com.example.novawear.entity.FlashSale;
import com.example.novawear.entity.FlashSaleProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleDto {

    private Long id;
    private String name;
    private Instant startTime;
    private Instant endTime;
    private Integer discountPercent;
    private Boolean active;
    private List<FlashSaleItemDto> products;
    private Instant createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlashSaleItemDto {
        private Long id;
        private Long productId;
        private String productName;
        private String productSlug;
        private String productImage;
        private BigDecimal originalPrice;
        private BigDecimal salePrice;
        private Integer quantity;
        private Integer soldCount;

        public static FlashSaleItemDto from(FlashSaleProduct fp) {
            FlashSaleItemDto dto = new FlashSaleItemDto();
            dto.setId(fp.getId());
            dto.setProductId(fp.getProduct().getId());
            dto.setProductName(fp.getProduct().getName());
            dto.setProductSlug(fp.getProduct().getSlug());
            dto.setProductImage(fp.getProduct().getImageUrl());
            dto.setOriginalPrice(fp.getProduct().getPrice());
            dto.setSalePrice(fp.getSalePrice());
            dto.setQuantity(fp.getQuantity());
            dto.setSoldCount(fp.getSoldCount());
            return dto;
        }
    }

    public static FlashSaleDto from(FlashSale fs) {
        FlashSaleDto dto = new FlashSaleDto();
        dto.setId(fs.getId());
        dto.setName(fs.getName());
        dto.setStartTime(fs.getStartTime());
        dto.setEndTime(fs.getEndTime());
        dto.setDiscountPercent(fs.getDiscountPercent());
        dto.setActive(fs.getActive());
        dto.setCreatedAt(fs.getCreatedAt());
        if (fs.getProducts() != null) {
            dto.setProducts(fs.getProducts().stream()
                    .map(FlashSaleItemDto::from)
                    .collect(Collectors.toList()));
        } else {
            dto.setProducts(Collections.emptyList());
        }
        return dto;
    }
}
