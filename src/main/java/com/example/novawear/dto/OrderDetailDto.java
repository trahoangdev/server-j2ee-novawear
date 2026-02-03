package com.example.novawear.dto;

import com.example.novawear.entity.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDto {

    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;

    public static OrderDetailDto from(OrderDetail od) {
        OrderDetailDto dto = new OrderDetailDto();
        dto.setId(od.getId());
        dto.setProductId(od.getProduct().getId());
        dto.setProductName(od.getProduct().getName());
        dto.setQuantity(od.getQuantity());
        dto.setPrice(od.getPrice());
        dto.setSubtotal(od.getPrice().multiply(BigDecimal.valueOf(od.getQuantity())));
        return dto;
    }
}
