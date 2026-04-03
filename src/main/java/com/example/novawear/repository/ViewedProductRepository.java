package com.example.novawear.repository;

import com.example.novawear.entity.ViewedProduct;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ViewedProductRepository extends JpaRepository<ViewedProduct, Long> {

    /**
     * Lay danh sach san pham da xem cua user, moi nhat truoc.
     * Gioi han toi da 20 san pham.
     */
    List<ViewedProduct> findByUserIdOrderByViewedAtDesc(Long userId, Pageable pageable);

    /**
     * Dem so luong san pham da xem cua user.
     */
    long countByUserId(Long userId);

    /**
     * Kiem tra xem user da xem san pham nay chua.
     */
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    /**
     * Lay viewed record cua user cho mot san pham cu the.
     */
    Optional<ViewedProduct> findByUserIdAndProductId(Long userId, Long productId);

    /**
     * Xoa tat ca lich su xem cua user.
     */
    @Modifying
    @Query("DELETE FROM ViewedProduct vp WHERE vp.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    /**
     * Xoa mot viewed record cu the.
     */
    @Modifying
    @Query("DELETE FROM ViewedProduct vp WHERE vp.user.id = :userId AND vp.product.id = :productId")
    void deleteByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
}
