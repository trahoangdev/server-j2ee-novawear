package com.example.novawear.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "viewed_products",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}),
       indexes = {
           @Index(name = "idx_viewed_user_id", columnList = "user_id"),
           @Index(name = "idx_viewed_viewed_at", columnList = "viewed_at")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewedProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "viewed_at", nullable = false)
    @Builder.Default
    private Instant viewedAt = Instant.now();

    @PrePersist
    void viewedAt() {
        if (this.viewedAt == null) this.viewedAt = Instant.now();
    }
}
