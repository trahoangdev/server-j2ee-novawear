package com.example.novawear.dto;

import com.example.novawear.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private Long id;
    private String type;
    private String title;
    private String message;
    private Boolean isRead;
    private String linkTo;
    private Instant createdAt;

    public static NotificationDto from(Notification n) {
        NotificationDto dto = new NotificationDto();
        dto.setId(n.getId());
        dto.setType(n.getType());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setIsRead(n.getIsRead());
        dto.setLinkTo(n.getLinkTo());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}
