package com.example.novawear.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDto {
    private Long id;
    private String name;
    private String imageUrl;
    private Long totalSold;
}
