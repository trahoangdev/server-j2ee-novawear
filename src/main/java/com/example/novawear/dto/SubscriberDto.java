package com.example.novawear.dto;

import com.example.novawear.entity.Subscriber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberDto {

    private Long id;

    @NotBlank
    @Email
    private String email;

    private Boolean active;
    private Instant subscribedAt;

    public static SubscriberDto from(Subscriber s) {
        SubscriberDto dto = new SubscriberDto();
        dto.setId(s.getId());
        dto.setEmail(s.getEmail());
        dto.setActive(s.getActive());
        dto.setSubscribedAt(s.getSubscribedAt());
        return dto;
    }
}
