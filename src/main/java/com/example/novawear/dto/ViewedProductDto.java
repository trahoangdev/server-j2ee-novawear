package com.example.novawear.dto;

import com.example.novawear.entity.ViewedProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewedProductDto {

    private Long id;
    private Long productId;
    private Instant viewedAt;
    private ProductDto product;

    public static ViewedProductDto from(ViewedProduct vp) {
        ViewedProductDto dto = new ViewedProductDto();
        dto.setId(vp.getId());
        dto.setProductId(vp.getProduct().getId());
        dto.setViewedAt(vp.getViewedAt());
        dto.setProduct(ProductDto.from(vp.getProduct()));
        return dto;
    }
}
