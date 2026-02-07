package com.example.novawear.service;

import com.example.novawear.dto.VoucherCreateRequest;
import com.example.novawear.dto.VoucherDto;
import com.example.novawear.dto.VoucherValidateResponse;
import com.example.novawear.entity.Order;
import com.example.novawear.entity.User;
import com.example.novawear.entity.UserVoucher;
import com.example.novawear.entity.Voucher;
import com.example.novawear.repository.UserRepository;
import com.example.novawear.repository.UserVoucherRepository;
import com.example.novawear.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;

    // ==================== PUBLIC APIs ====================

    /**
     * Lấy danh sách voucher đang active (cho user xem)
     */
    @Transactional(readOnly = true)
    public List<VoucherDto> getActiveVouchers() {
        return voucherRepository.findActiveVouchers(Instant.now())
                .stream()
                .map(VoucherDto::from)
                .collect(Collectors.toList());
    }

    /**
     * Lấy voucher có thể áp dụng cho đơn hàng với tổng tiền cụ thể
     */
    @Transactional(readOnly = true)
    public List<VoucherDto> getApplicableVouchers(BigDecimal orderTotal, String username) {
        List<Voucher> vouchers = voucherRepository.findApplicableVouchers(Instant.now(), orderTotal);

        // Nếu có username, filter thêm theo usage limit per user
        if (username != null) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                Long userId = user.getId();
                vouchers = vouchers.stream()
                        .filter(v -> canUserUseVoucher(userId, v))
                        .collect(Collectors.toList());
            }
        }

        return vouchers.stream()
                .map(VoucherDto::from)
                .collect(Collectors.toList());
    }

    /**
     * Validate mã voucher và tính tiền giảm
     */
    @Transactional(readOnly = true)
    public VoucherValidateResponse validateVoucher(String code, BigDecimal orderTotal, String username) {
        if (code == null || code.isBlank()) {
            return VoucherValidateResponse.invalid("Vui lòng nhập mã voucher");
        }

        Voucher voucher = voucherRepository.findByCodeIgnoreCase(code.trim()).orElse(null);
        if (voucher == null) {
            return VoucherValidateResponse.invalid("Mã voucher không tồn tại");
        }

        // Check voucher còn hiệu lực không
        if (!voucher.isValid()) {
            if (!Boolean.TRUE.equals(voucher.getActive())) {
                return VoucherValidateResponse.invalid("Voucher đã bị vô hiệu hóa");
            }
            if (voucher.getStartDate() != null && Instant.now().isBefore(voucher.getStartDate())) {
                return VoucherValidateResponse.invalid("Voucher chưa đến ngày áp dụng");
            }
            if (voucher.getEndDate() != null && Instant.now().isAfter(voucher.getEndDate())) {
                return VoucherValidateResponse.invalid("Voucher đã hết hạn");
            }
            if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
                return VoucherValidateResponse.invalid("Voucher đã hết lượt sử dụng");
            }
        }

        // Check đơn hàng tối thiểu
        if (voucher.getMinOrderValue() != null && orderTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            return VoucherValidateResponse.invalid(
                    String.format("Đơn hàng tối thiểu %,.0f₫ để áp dụng voucher này", voucher.getMinOrderValue()));
        }

        // Check usage limit per user
        if (username != null && voucher.getUsageLimitPerUser() != null) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                long usedCount = userVoucherRepository.countByUserIdAndVoucherId(user.getId(), voucher.getId());
                if (usedCount >= voucher.getUsageLimitPerUser()) {
                    return VoucherValidateResponse.invalid("Bạn đã sử dụng hết lượt của voucher này");
                }
            }
        }

        // Tính tiền giảm
        BigDecimal discount = voucher.calculateDiscount(orderTotal);
        BigDecimal finalTotal = orderTotal.subtract(discount);

        return VoucherValidateResponse.valid(VoucherDto.from(voucher), discount, finalTotal);
    }

    /**
     * Sử dụng voucher cho đơn hàng (gọi khi checkout thành công)
     */
    @Transactional
    public void useVoucher(Long voucherId, User user, Order order) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found"));

        // Tăng số lượt đã dùng
        voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);

        // Lưu lịch sử user đã dùng voucher
        UserVoucher userVoucher = UserVoucher.builder()
                .user(user)
                .voucher(voucher)
                .order(order)
                .usedAt(Instant.now())
                .build();
        userVoucherRepository.save(userVoucher);
    }

    // ==================== ADMIN APIs ====================

    @Transactional(readOnly = true)
    public Page<VoucherDto> search(String keyword, Boolean active, Pageable pageable) {
        return voucherRepository.search(keyword, active, pageable).map(VoucherDto::from);
    }

    @Transactional(readOnly = true)
    public VoucherDto getById(Long id) {
        Voucher v = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found: " + id));
        return VoucherDto.from(v);
    }

    @Transactional
    public VoucherDto create(VoucherCreateRequest request) {
        // Check duplicate code
        if (voucherRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Mã voucher đã tồn tại: " + request.getCode());
        }

        Voucher voucher = Voucher.builder()
                .code(request.getCode().toUpperCase().trim())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderValue(request.getMinOrderValue())
                .maxDiscount(request.getMaxDiscount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .usageLimit(request.getUsageLimit())
                .usageLimitPerUser(request.getUsageLimitPerUser())
                .active(request.getActive() != null ? request.getActive() : true)
                .usedCount(0)
                .createdAt(Instant.now())
                .build();

        voucher = voucherRepository.save(voucher);
        return VoucherDto.from(voucher);
    }

    @Transactional
    public VoucherDto update(Long id, VoucherCreateRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found: " + id));

        // Check duplicate code if changed
        if (!voucher.getCode().equalsIgnoreCase(request.getCode())
                && voucherRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Mã voucher đã tồn tại: " + request.getCode());
        }

        voucher.setCode(request.getCode().toUpperCase().trim());
        voucher.setDescription(request.getDescription());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setMaxDiscount(request.getMaxDiscount());
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());
        voucher.setUsageLimit(request.getUsageLimit());
        voucher.setUsageLimitPerUser(request.getUsageLimitPerUser());
        if (request.getActive() != null) {
            voucher.setActive(request.getActive());
        }

        voucher = voucherRepository.save(voucher);
        return VoucherDto.from(voucher);
    }

    @Transactional
    public void delete(Long id) {
        if (!voucherRepository.existsById(id)) {
            throw new IllegalArgumentException("Voucher not found: " + id);
        }
        voucherRepository.deleteById(id);
    }

    @Transactional
    public VoucherDto toggleActive(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found: " + id));
        voucher.setActive(!voucher.getActive());
        voucher = voucherRepository.save(voucher);
        return VoucherDto.from(voucher);
    }

    // ==================== HELPER ====================

    private boolean canUserUseVoucher(Long userId, Voucher voucher) {
        if (voucher.getUsageLimitPerUser() == null) {
            return true;
        }
        long usedCount = userVoucherRepository.countByUserIdAndVoucherId(userId, voucher.getId());
        return usedCount < voucher.getUsageLimitPerUser();
    }
}
