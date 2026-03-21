package com.example.novawear.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {

    private Long productId;
    private String productName;
    private String imageUrl;
    private BigDecimal price;
    @NotNull
    @Min(1)
    private Integer quantity;
    private BigDecimal subtotal;
    private Boolean isFlashSale;
}
