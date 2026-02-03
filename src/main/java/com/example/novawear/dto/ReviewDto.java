package com.example.novawear.dto;

import com.example.novawear.entity.Review;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {

    private Long id;
    private Long productId;
    private Long userId;
    private String username;
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;
    @Size(max = 1000)
    private String comment;
    private Boolean approved;
    private Instant createdAt;

    public static ReviewDto from(Review r) {
        ReviewDto dto = new ReviewDto();
        dto.setId(r.getId());
        dto.setProductId(r.getProduct().getId());
        dto.setUserId(r.getUser().getId());
        dto.setUsername(r.getUser().getUsername());
        dto.setRating(r.getRating());
        dto.setComment(r.getComment());
        dto.setApproved(r.getApproved());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }
}
