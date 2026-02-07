package com.example.novawear.repository;

import com.example.novawear.entity.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    Optional<Voucher> findByCode(String code);

    Optional<Voucher> findByCodeIgnoreCase(String code);

    boolean existsByCode(String code);

    /**
     * Tìm các voucher đang active và còn hạn
     */
    @Query("SELECT v FROM Voucher v WHERE v.active = true " +
            "AND (v.startDate IS NULL OR v.startDate <= :now) " +
            "AND (v.endDate IS NULL OR v.endDate >= :now) " +
            "AND (v.usageLimit IS NULL OR v.usedCount < v.usageLimit)")
    List<Voucher> findActiveVouchers(@Param("now") Instant now);

    /**
     * Tìm các voucher có thể áp dụng cho đơn hàng với tổng tiền cụ thể
     */
    @Query("SELECT v FROM Voucher v WHERE v.active = true " +
            "AND (v.startDate IS NULL OR v.startDate <= :now) " +
            "AND (v.endDate IS NULL OR v.endDate >= :now) " +
            "AND (v.usageLimit IS NULL OR v.usedCount < v.usageLimit) " +
            "AND (v.minOrderValue IS NULL OR v.minOrderValue <= :orderTotal)")
    List<Voucher> findApplicableVouchers(@Param("now") Instant now,
            @Param("orderTotal") java.math.BigDecimal orderTotal);

    /**
     * Tìm kiếm voucher cho admin
     */
    @Query("SELECT v FROM Voucher v WHERE " +
            "(:keyword IS NULL OR LOWER(v.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(v.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:active IS NULL OR v.active = :active)")
    Page<Voucher> search(@Param("keyword") String keyword, @Param("active") Boolean active, Pageable pageable);

    /**
     * Đếm số voucher đang active
     */
    @Query("SELECT COUNT(v) FROM Voucher v WHERE v.active = true " +
            "AND (v.startDate IS NULL OR v.startDate <= :now) " +
            "AND (v.endDate IS NULL OR v.endDate >= :now)")
    long countActiveVouchers(@Param("now") Instant now);
}
