package com.example.novawear.dto;

import com.example.novawear.entity.Banner;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerDto {

    private Long id;

    @Size(max = 200)
    private String title;

    @Size(max = 300)
    private String subtitle;

    @Size(max = 500)
    private String imageUrl;

    @Size(max = 500)
    private String linkUrl;

    @Size(max = 50)
    private String ctaText;

    private String description;

    @Size(max = 50)
    private String ctaText2;

    @Size(max = 500)
    private String linkUrl2;

    @Size(max = 50)
    private String badgeText;

    private String bannerType;

    private Integer sortOrder;

    private Boolean active;

    private Instant createdAt;

    public static BannerDto from(Banner b) {
        BannerDto dto = new BannerDto();
        dto.setId(b.getId());
        dto.setTitle(b.getTitle());
        dto.setSubtitle(b.getSubtitle());
        dto.setImageUrl(b.getImageUrl());
        dto.setLinkUrl(b.getLinkUrl());
        dto.setCtaText(b.getCtaText());
        dto.setDescription(b.getDescription());
        dto.setCtaText2(b.getCtaText2());
        dto.setLinkUrl2(b.getLinkUrl2());
        dto.setBadgeText(b.getBadgeText());
        dto.setBannerType(b.getBannerType() != null ? b.getBannerType().name() : "CAROUSEL");
        dto.setSortOrder(b.getSortOrder());
        dto.setActive(b.getActive());
        dto.setCreatedAt(b.getCreatedAt());
        return dto;
    }
}
