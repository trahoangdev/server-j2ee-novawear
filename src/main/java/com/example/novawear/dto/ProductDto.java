package com.example.novawear.dto;

import com.example.novawear.entity.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private Long id;
    @NotBlank
    @Size(max = 200)
    private String name;
    @NotNull
    @DecimalMin("0")
    private BigDecimal price;
    @Size(max = 2000)
    private String description;
    @Size(max = 500)
    private String imageUrl;
    @NotNull
    private Long categoryId;
    private String categoryName;
    @NotNull
    private Integer stock;

    public static ProductDto from(Product p) {
        ProductDto dto = new ProductDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setPrice(p.getPrice());
        dto.setDescription(p.getDescription());
        dto.setImageUrl(p.getImageUrl());
        dto.setCategoryId(p.getCategory().getId());
        dto.setCategoryName(p.getCategory().getName());
        dto.setStock(p.getStock());
        return dto;
    }
}
