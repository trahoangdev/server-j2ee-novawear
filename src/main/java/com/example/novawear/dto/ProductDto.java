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
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private Long id;
    @NotBlank
    @Size(max = 200)
    private String name;
    
    @Size(max = 255)
    private String slug;
    
    @NotNull
    @DecimalMin("0")
    private BigDecimal price;
    @Size(max = 2000)
    private String description;
    @Size(max = 500)
    private String imageUrl;
    
    /** Danh sách URL hình ảnh */
    private List<String> images;
    
    @NotNull
    private Long categoryId;
    private String categoryName;
    @NotNull
    private Integer stock;

    /** Giá khuyến mãi (null = không giảm) */
    private BigDecimal salePrice;

    /** Nổi bật: hiển thị block "Sản phẩm nổi bật" */
    private Boolean featured;

    /** Bán chạy: nhãn do admin đánh dấu */
    private Boolean bestseller;

    /** Hàng mới: badge "Mới" trên thẻ sản phẩm */
    private Boolean isNew;

    /** Mã size, e.g. ["S","M","L","XL"] */
    private List<String> sizes;

    /** Màu sắc: tên + mã hex */
    private List<ProductColorDto> colors;

    public static ProductDto from(Product p) {
        ProductDto dto = new ProductDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setSlug(p.getSlug());
        dto.setPrice(p.getPrice());
        dto.setDescription(p.getDescription());
        dto.setImageUrl(p.getImageUrl());
        dto.setCategoryId(p.getCategory().getId());
        dto.setCategoryName(p.getCategory().getName());
        dto.setStock(p.getStock());
        dto.setSalePrice(p.getSalePrice());
        dto.setFeatured(p.getFeatured() != null ? p.getFeatured() : false);
        dto.setBestseller(p.getBestseller() != null ? p.getBestseller() : false);
        dto.setIsNew(p.getIsNew() != null ? p.getIsNew() : false);
        return dto;
    }
}
