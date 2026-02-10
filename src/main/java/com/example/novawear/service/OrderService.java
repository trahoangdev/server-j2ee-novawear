package com.example.novawear.service;

import com.example.novawear.dto.CheckoutRequest;
import com.example.novawear.dto.OrderDto;
import com.example.novawear.dto.VoucherValidateResponse;
import com.example.novawear.entity.Order;
import com.example.novawear.entity.OrderDetail;
import com.example.novawear.entity.Product;
import com.example.novawear.entity.User;
import com.example.novawear.entity.Voucher;
import com.example.novawear.repository.OrderRepository;
import com.example.novawear.repository.ProductRepository;
import com.example.novawear.repository.UserRepository;
import com.example.novawear.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherService voucherService;

    @Transactional(readOnly = true)
    public Page<OrderDto> findByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(OrderDto::from);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> findByUsername(String username, Pageable pageable) {
        return orderRepository.findByUserUsername(username, pageable).map(OrderDto::from);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable).map(OrderDto::from);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> findByStatus(Order.OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable).map(OrderDto::from);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> search(String keyword, Order.OrderStatus status, Instant fromDate, Instant toDate,
            Pageable pageable) {
        return orderRepository.searchOrders(keyword, status, fromDate, toDate, pageable).map(OrderDto::from);
    }

    @Transactional(readOnly = true)
    public OrderDto getById(Long id) {
        Order o = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        return OrderDto.from(o);
    }

    @Transactional
    public OrderDto checkout(String username, CheckoutRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart must not be empty");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<OrderDetail> details = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (var item : request.getItems()) {
            if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity() < 1) {
                throw new IllegalArgumentException("Invalid item: productId and quantity required");
            }
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProductId()));
            if (product.getPrice() == null) {
                throw new IllegalArgumentException("Product has no price: " + product.getName());
            }
            if (product.getStock() < item.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }

            // Sử dụng giá sale nếu có
            BigDecimal unitPrice = product.getSalePrice() != null
                    && product.getSalePrice().compareTo(BigDecimal.ZERO) > 0
                            ? product.getSalePrice()
                            : product.getPrice();

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            OrderDetail od = OrderDetail.builder()
                    .product(product)
                    .quantity(item.getQuantity())
                    .price(unitPrice)
                    .build();
            details.add(od);
            product.setStock(product.getStock() - item.getQuantity());
        }

        // === Xử lý Voucher ===
        Voucher voucher = null;
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            VoucherValidateResponse validation = voucherService.validateVoucher(
                    request.getVoucherCode(), subtotal, username);

            if (!Boolean.TRUE.equals(validation.getValid())) {
                throw new IllegalArgumentException(validation.getMessage());
            }

            voucher = voucherRepository.findByCodeIgnoreCase(request.getVoucherCode().trim())
                    .orElse(null);

            if (voucher != null) {
                discountAmount = voucher.calculateDiscount(subtotal);
            }
        }

        BigDecimal totalAmount = subtotal.subtract(discountAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .status(Order.OrderStatus.PENDING)
                .recipientName(request.getRecipientName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .note(request.getNote())
                .paymentMethod(request.getPaymentMethod())
                .voucher(voucher)
                .discountAmount(discountAmount)
                .orderCode(generateOrderCode())
                .build();

        for (OrderDetail od : details) {
            od.setOrder(order);
            order.getOrderDetails().add(od);
        }

        order = orderRepository.save(order);

        // Ghi nhận sử dụng voucher
        if (voucher != null) {
            voucherService.useVoucher(voucher.getId(), user, order);
        }

        Order saved = orderRepository.findById(order.getId()).orElseThrow();
        return OrderDto.from(saved);
    }

    @Transactional
    public OrderDto updateStatus(Long id, Order.OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        order.setStatus(status);
        order = orderRepository.save(order);
        return OrderDto.from(order);
    }

    /**
     * Hủy đơn hàng (chỉ cho phép khi status = PENDING hoặc CONFIRMED)
     */
    @Transactional
    public OrderDto cancelOrder(Long id, String username, String cancelReason) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        // Kiểm tra quyền (chỉ chủ đơn hàng mới được hủy)
        if (!order.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Bạn không có quyền hủy đơn hàng này");
        }

        // Kiểm tra trạng thái
        if (order.getStatus() != Order.OrderStatus.PENDING && order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new IllegalArgumentException("Chỉ có thể hủy đơn hàng ở trạng thái Chờ xử lý hoặc Đã xác nhận");
        }

        // Hoàn stock sản phẩm
        for (OrderDetail od : order.getOrderDetails()) {
            Product product = od.getProduct();
            product.setStock(product.getStock() + od.getQuantity());
            productRepository.save(product);
        }

        // Cập nhật trạng thái
        order.setStatus(Order.OrderStatus.CANCELLED);
        // Lưu lý do hủy vào note
        String note = order.getNote() != null ? order.getNote() : "";
        order.setNote(note + "\n[Đã hủy] " + (cancelReason != null ? cancelReason : "Không có lý do"));

        order = orderRepository.save(order);
        return OrderDto.from(order);
    }

    private String generateOrderCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
