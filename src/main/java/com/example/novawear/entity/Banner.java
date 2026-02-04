package com.example.novawear.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "banners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 200)
    @Column(length = 200)
    private String title;

    @Size(max = 300)
    @Column(length = 300)
    private String subtitle;

    @Size(max = 500)
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Size(max = 500)
    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Size(max = 50)
    @Column(name = "cta_text", length = 50)
    private String ctaText;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
