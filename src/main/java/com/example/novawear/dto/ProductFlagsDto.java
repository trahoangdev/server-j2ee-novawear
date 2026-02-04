package com.example.novawear.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Body cho PATCH /api/admin/products/:id – chỉ cập nhật nổi bật, bán chạy (toggle nhanh từ bảng) */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductFlagsDto {
    private Boolean featured;
    private Boolean bestseller;
}
