package com.example.novawear.dto;

import com.example.novawear.entity.BundleItem;
import com.example.novawear.entity.ProductBundle;
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
public class BundleDto {

    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Integer discountPercent;
    private Boolean active;
    private BigDecimal totalOriginalPrice;
    private BigDecimal bundlePrice;
    private List<BundleItemDto> items;
    private Instant createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BundleItemDto {
        private Long id;
        private Long productId;
        private String productName;
        private String productSlug;
        private String productImage;
        private BigDecimal productPrice;
        private Integer quantity;

        public static BundleItemDto from(BundleItem bi) {
            BundleItemDto dto = new BundleItemDto();
            dto.setId(bi.getId());
            dto.setProductId(bi.getProduct().getId());
            dto.setProductName(bi.getProduct().getName());
            dto.setProductSlug(bi.getProduct().getSlug());
            dto.setProductImage(bi.getProduct().getImageUrl());
            dto.setProductPrice(bi.getProduct().getPrice());
            dto.setQuantity(bi.getQuantity());
            return dto;
        }
    }

    public static BundleDto from(ProductBundle bundle) {
        BundleDto dto = new BundleDto();
        dto.setId(bundle.getId());
        dto.setName(bundle.getName());
        dto.setDescription(bundle.getDescription());
        dto.setImageUrl(bundle.getImageUrl());
        dto.setDiscountPercent(bundle.getDiscountPercent());
        dto.setActive(bundle.getActive());
        dto.setCreatedAt(bundle.getCreatedAt());
        if (bundle.getItems() != null && !bundle.getItems().isEmpty()) {
            dto.setTotalOriginalPrice(bundle.getTotalOriginalPrice());
            dto.setBundlePrice(bundle.getBundlePrice());
            dto.setItems(bundle.getItems().stream()
                    .map(BundleItemDto::from)
                    .collect(Collectors.toList()));
        } else {
            dto.setTotalOriginalPrice(BigDecimal.ZERO);
            dto.setBundlePrice(BigDecimal.ZERO);
            dto.setItems(Collections.emptyList());
        }
        return dto;
    }
}
