package com.example.novawear.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductFiltersDto {
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<String> sizes;
    private List<ProductColorDto> colors;
}
