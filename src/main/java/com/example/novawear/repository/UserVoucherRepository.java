package com.example.novawear.repository;

import com.example.novawear.entity.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {

    /**
     * Đếm số lần user đã dùng voucher cụ thể
     */
    @Query("SELECT COUNT(uv) FROM UserVoucher uv WHERE uv.user.id = :userId AND uv.voucher.id = :voucherId")
    long countByUserIdAndVoucherId(@Param("userId") Long userId, @Param("voucherId") Long voucherId);

    /**
     * Kiểm tra user đã dùng voucher cho order cụ thể chưa
     */
    boolean existsByUserIdAndVoucherIdAndOrderId(Long userId, Long voucherId, Long orderId);

    /**
     * Lấy lịch sử sử dụng voucher của user
     */
    List<UserVoucher> findByUserIdOrderByUsedAtDesc(Long userId);

    /**
     * Lấy danh sách voucher ID mà user đã dùng
     */
    @Query("SELECT uv.voucher.id FROM UserVoucher uv WHERE uv.user.id = :userId")
    List<Long> findVoucherIdsByUserId(@Param("userId") Long userId);
}
