package com.example.novawear.service;

import com.example.novawear.dto.ReturnRequestDto;
import com.example.novawear.entity.Order;
import com.example.novawear.entity.ReturnRequest;
import com.example.novawear.entity.User;
import com.example.novawear.repository.OrderRepository;
import com.example.novawear.repository.ReturnRequestRepository;
import com.example.novawear.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnRequestService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public ReturnRequestDto create(String username, Long orderId, String reason, List<String> imageUrls) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (order.getStatus() != Order.OrderStatus.DELIVERED) {
            throw new IllegalStateException("Chỉ có thể yêu cầu trả hàng cho đơn đã giao");
        }
        if (returnRequestRepository.existsByOrderId(orderId)) {
            throw new IllegalStateException("Đơn hàng này đã có yêu cầu trả hàng");
        }

        ReturnRequest rr = ReturnRequest.builder()
                .order(order)
                .user(user)
                .reason(reason)
                .images(imageUrls != null && !imageUrls.isEmpty() ? String.join(",", imageUrls) : null)
                .build();
        rr = returnRequestRepository.save(rr);

        notificationService.create(user.getId(), "RETURN",
                "Yêu cầu trả hàng đã gửi",
                "Yêu cầu trả hàng cho đơn #" + order.getOrderCode() + " đang được xử lý",
                "/orders");

        return ReturnRequestDto.from(rr);
    }

    @Transactional(readOnly = true)
    public Page<ReturnRequestDto> findAll(Pageable pageable) {
        return returnRequestRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(ReturnRequestDto::from);
    }

    @Transactional(readOnly = true)
    public Page<ReturnRequestDto> findByUser(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return returnRequestRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(ReturnRequestDto::from);
    }

    @Transactional(readOnly = true)
    public ReturnRequestDto getById(Long id) {
        ReturnRequest rr = returnRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Return request not found: " + id));
        return ReturnRequestDto.from(rr);
    }

    @Transactional
    public ReturnRequestDto updateStatus(Long id, String status, String adminNote) {
        ReturnRequest rr = returnRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Return request not found: " + id));
        rr.setStatus(ReturnRequest.ReturnStatus.valueOf(status.toUpperCase()));
        if (adminNote != null) rr.setAdminNote(adminNote);
        rr = returnRequestRepository.save(rr);

        String statusVi = switch (rr.getStatus()) {
            case APPROVED -> "được duyệt";
            case REJECTED -> "bị từ chối";
            case COMPLETED -> "hoàn tất";
            default -> "đang xử lý";
        };
        notificationService.create(rr.getUser().getId(), "RETURN",
                "Cập nhật yêu cầu trả hàng",
                "Yêu cầu trả hàng cho đơn #" + rr.getOrder().getOrderCode() + " đã " + statusVi,
                "/orders");

        return ReturnRequestDto.from(rr);
    }
}
