package com.example.novawear.controller;

import com.example.novawear.entity.Order;
import com.example.novawear.repository.OrderRepository;
import com.example.novawear.service.VnPayService;
import com.example.novawear.util.VnPayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment/vnpay")
@RequiredArgsConstructor
public class VnPayController {

    private final VnPayService vnPayService;
    private final OrderRepository orderRepository;

    /**
     * Tạo URL thanh toán VNPAY
     * POST /api/payment/vnpay/create-payment-url
     */
    @PostMapping("/create-payment-url")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> createPaymentUrl(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam Long orderId,
            jakarta.servlet.http.HttpServletRequest request) {
        
        if (user == null) {
            log.warn("Unauthorized access to create payment URL");
            return ResponseEntity.status(401).build();
        }
        
        log.info("Creating payment URL for orderId: {}, user: {}", orderId, user.getUsername());
        
        try {
            // Lấy đơn hàng cùng với user (JOIN FETCH để tránh lazy loading)
            Order order = orderRepository.findByIdWithUser(orderId)
                    .orElse(orderRepository.findById(orderId)
                            .orElseThrow(() -> {
                                log.error("Order not found: {}", orderId);
                                return new IllegalArgumentException("Order not found: " + orderId);
                            }));
            
            log.debug("Order found: id={}, status={}, paymentMethod={}", 
                    order.getId(), order.getStatus(), order.getPaymentMethod());
            
            // Kiểm tra quyền (chỉ chủ đơn hàng) - fetch user để tránh lazy loading
            String orderOwnerUsername = order.getUser().getUsername();
            if (!orderOwnerUsername.equals(user.getUsername())) {
                log.warn("User {} tried to access order {} owned by {}", user.getUsername(), orderId, orderOwnerUsername);
                return ResponseEntity.status(403).build();
            }
            
            // Kiểm tra trạng thái (chỉ cho phép khi PENDING)
            if (order.getStatus() != Order.OrderStatus.PENDING) {
                log.warn("Order {} is not in PENDING status, current status: {}", orderId, order.getStatus());
                Map<String, Object> error = new HashMap<>();
                error.put("code", "01");
                error.put("message", "Order is not in PENDING status. Current status: " + order.getStatus());
                return ResponseEntity.badRequest().body(error);
            }
            
            // Kiểm tra payment method
            if (order.getPaymentMethod() == null || !"VNPAY".equalsIgnoreCase(order.getPaymentMethod())) {
                log.warn("Order {} payment method is not VNPAY, current: {}", orderId, order.getPaymentMethod());
                Map<String, Object> error = new HashMap<>();
                error.put("code", "02");
                error.put("message", "Payment method is not VNPAY. Current: " + order.getPaymentMethod());
                return ResponseEntity.badRequest().body(error);
            }
            
            // Lấy IP address
            String ipAddr = VnPayUtil.getIpAddress(request);
            log.debug("Client IP address: {}", ipAddr);
            
            // Tạo URL thanh toán
            String paymentUrl = vnPayService.createPaymentUrl(order, ipAddr);
            log.info("Payment URL created successfully for order: {}", orderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", "00");
            response.put("message", "success");
            response.put("data", paymentUrl);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            log.error("Configuration error creating payment URL: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", "03");
            error.put("message", "VNPAY configuration error: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        } catch (Exception e) {
            log.error("Error creating payment URL for orderId: {}", orderId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", "99");
            error.put("message", e.getMessage() != null ? e.getMessage() : "Internal server error");
            error.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * IPN URL - VNPAY gọi về để cập nhật kết quả thanh toán
     * GET /api/payment/vnpay/ipn
     */
    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> ipnCallback(
            @RequestParam Map<String, String> params) {
        
        log.info("Received IPN callback from VNPAY: {}", params);
        
        // Xử lý IPN
        Map<String, String> response = vnPayService.processIpnResponse(params);
        
        log.info("IPN response: {}", response);
        return ResponseEntity.ok(response);
    }

    /**
     * Return URL - VNPAY redirect khách hàng về sau khi thanh toán
     * GET /api/payment/vnpay/return
     */
    @GetMapping("/return")
    public ResponseEntity<Map<String, Object>> returnCallback(
            @RequestParam Map<String, String> params) {
        
        log.info("=== VNPAY Return URL Callback ===");
        log.info("Received params count: {}", params.size());
        log.info("Params: {}", params);
        log.info("vnp_TxnRef: {}", params.get("vnp_TxnRef"));
        log.info("vnp_ResponseCode: {}", params.get("vnp_ResponseCode"));
        log.info("vnp_TransactionStatus: {}", params.get("vnp_TransactionStatus"));
        log.info("vnp_SecureHash: {}", params.get("vnp_SecureHash"));
        
        try {
            // Xử lý return URL
            Map<String, Object> result = vnPayService.processReturnResponse(params);
            
            log.info("Return URL result: success={}, message={}", 
                    result.get("success"), result.get("message"));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Unexpected error in return callback", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.ok(error);
        }
    }
}
