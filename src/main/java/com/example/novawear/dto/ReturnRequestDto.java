package com.example.novawear.dto;

import com.example.novawear.entity.ReturnRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequestDto {

    private Long id;
    private Long orderId;
    private String orderCode;
    private Long userId;
    private String username;
    private String reason;
    private String status;
    private List<String> images;
    private String adminNote;
    private Instant createdAt;

    public static ReturnRequestDto from(ReturnRequest r) {
        ReturnRequestDto dto = new ReturnRequestDto();
        dto.setId(r.getId());
        dto.setOrderId(r.getOrder().getId());
        dto.setOrderCode(r.getOrder().getOrderCode());
        dto.setUserId(r.getUser().getId());
        dto.setUsername(r.getUser().getUsername());
        dto.setReason(r.getReason());
        dto.setStatus(r.getStatus().name());
        dto.setAdminNote(r.getAdminNote());
        dto.setCreatedAt(r.getCreatedAt());
        if (r.getImages() != null && !r.getImages().isBlank()) {
            dto.setImages(Arrays.asList(r.getImages().split(",")));
        } else {
            dto.setImages(Collections.emptyList());
        }
        return dto;
    }
}
