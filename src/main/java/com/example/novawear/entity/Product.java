package com.example.novawear.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String name;

    @NotNull
    @DecimalMin("0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Size(max = 2000)
    @Column(length = 2000)
    private String description;

    @Size(max = 500)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotNull
    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;

    /** Giá khuyến mãi (null = không giảm). Nếu set và < price thì hiển thị sale. */
    @DecimalMin("0")
    @Column(name = "sale_price", precision = 12, scale = 2)
    private BigDecimal salePrice;

    /** Nổi bật: hiển thị ở block "Sản phẩm nổi bật" trang chủ */
    @Column(nullable = false)
    @Builder.Default
    private Boolean featured = false;

    /** Bán chạy: nhãn do admin đánh dấu */
    @Column(nullable = false)
    @Builder.Default
    private Boolean bestseller = false;

    /** Hàng mới: hiển thị badge "Mới" trên thẻ sản phẩm */
    @Column(name = "is_new", nullable = false)
    @Builder.Default
    private Boolean isNew = false;

    /** JSON array of size codes, e.g. ["S","M","L","XL"]. Max 500 chars. */
    @Size(max = 500)
    @Column(name = "sizes", length = 500)
    private String sizes;

    /** JSON array of {name, hex}, e.g. [{"name":"Đen","hex":"#2D2D2D"}]. Max 2000 chars. */
    @Size(max = 2000)
    @Column(name = "colors", length = 2000)
    private String colors;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();
}
