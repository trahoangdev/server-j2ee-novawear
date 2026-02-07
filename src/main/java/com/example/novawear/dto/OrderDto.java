package com.example.novawear.dto;

import com.example.novawear.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Long id;
    /** Mã đơn hàng: dãy 4–6 chữ số (pad ID thành 6 chữ số) */
    private String orderNumber;
    private Long userId;
    private String username;
    private BigDecimal totalAmount;
    private String status;
    private Instant orderDate;
    private String recipientName;
    private String address;
    private String phone;
    private String note;
    private List<OrderDetailDto> orderDetails;

    public static OrderDto from(Order o) {
        OrderDto dto = new OrderDto();
        dto.setId(o.getId());
        dto.setOrderNumber(String.format("%06d", o.getId()));
        dto.setUserId(o.getUser().getId());
        dto.setUsername(o.getUser().getUsername());
        dto.setTotalAmount(o.getTotalAmount());
        dto.setStatus(o.getStatus().name());
        dto.setOrderDate(o.getOrderDate());
        dto.setRecipientName(o.getRecipientName());
        dto.setAddress(o.getAddress());
        dto.setPhone(o.getPhone());
        dto.setNote(o.getNote());
        if (o.getOrderDetails() != null) {
            dto.setOrderDetails(o.getOrderDetails().stream()
                    .map(OrderDetailDto::from)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
