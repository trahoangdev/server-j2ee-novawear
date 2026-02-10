package com.example.novawear.service;

import com.example.novawear.config.VnPayConfig;
import com.example.novawear.entity.Order;
import com.example.novawear.repository.OrderRepository;
import com.example.novawear.util.VnPayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class VnPayService {

    private final VnPayConfig vnPayConfig;
    private final OrderRepository orderRepository;

    /**
     * Tạo URL thanh toán VNPAY
     */
    public String createPaymentUrl(Order order, String ipAddr) {
        // Validate config
        if (vnPayConfig.getTmnCode() == null || vnPayConfig.getTmnCode().isEmpty()) {
            throw new IllegalStateException("VNPAY TmnCode is not configured");
        }
        if (vnPayConfig.getHashSecret() == null || vnPayConfig.getHashSecret().isEmpty()) {
            throw new IllegalStateException("VNPAY HashSecret is not configured");
        }
        
        // Build các tham số theo spec VNPAY
        Map<String, String> params = new HashMap<>();
        
        params.put("vnp_Version", VnPayConfig.VERSION);
        params.put("vnp_Command", VnPayConfig.COMMAND);
        params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        
        // Số tiền nhân 100 (khử phần thập phân)
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order total amount must be greater than 0");
        }
        long amount = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid order amount: " + order.getTotalAmount());
        }
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", VnPayConfig.CURRENCY);
        
        // Mã đơn hàng (dùng orderCode hoặc orderId)
        String txnRef = order.getOrderCode() != null && !order.getOrderCode().isEmpty() 
                ? order.getOrderCode() 
                : String.valueOf(order.getId());
        if (txnRef == null || txnRef.isEmpty()) {
            throw new IllegalArgumentException("Order code and order ID cannot be null");
        }
        params.put("vnp_TxnRef", txnRef);
        
        // Thông tin đơn hàng (không dấu, không ký tự đặc biệt)
        String orderInfo = "Thanh toan don hang " + txnRef;
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", VnPayConfig.ORDER_TYPE);
        
        // Locale
        params.put("vnp_Locale", VnPayConfig.LOCALE_VN);
        
        // Return URL và IP
        params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        params.put("vnp_IpAddr", ipAddr);
        
        // Thời gian tạo và hết hạn
        String createDate = VnPayUtil.formatDate(ZonedDateTime.now());
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", VnPayUtil.createExpireDate(15)); // +15 phút
        
        // Build URL với checksum
        String queryString = VnPayUtil.buildPaymentUrl(params, vnPayConfig.getHashSecret());
        if (queryString == null || queryString.isEmpty()) {
            throw new RuntimeException("Failed to build VNPAY payment URL query string");
        }
        
        String paymentUrl = vnPayConfig.getPaymentUrl() + "?" + queryString;
        
        log.info("Created VNPAY payment URL for order: {}, amount: {}", txnRef, amount);
        return paymentUrl;
    }

    /**
     * Xử lý IPN callback từ VNPAY
     * Trả về Map với RspCode và Message để gửi lại VNPAY
     */
    @Transactional
    public Map<String, String> processIpnResponse(Map<String, String> params) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Lấy secure hash
            String secureHash = params.get("vnp_SecureHash");
            if (secureHash == null || secureHash.isEmpty()) {
                response.put("RspCode", "99");
                response.put("Message", "Missing secure hash");
                return response;
            }
            
            // Verify checksum
            if (!VnPayUtil.verifyChecksum(params, vnPayConfig.getHashSecret(), secureHash)) {
                log.warn("Invalid checksum from VNPAY IPN");
                response.put("RspCode", "97");
                response.put("Message", "Invalid signature");
                return response;
            }
            
            // Lấy thông tin từ params
            String txnRef = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");
            String transactionStatus = params.get("vnp_TransactionStatus");
            String transactionNo = params.get("vnp_TransactionNo");
            String amountStr = params.get("vnp_Amount");
            
            log.info("Processing IPN for order: {}, ResponseCode: {}, TransactionStatus: {}", 
                    txnRef, responseCode, transactionStatus);
            
            // Tìm đơn hàng theo orderCode hoặc orderId
            Order order = orderRepository.findByOrderCode(txnRef)
                    .orElse(orderRepository.findById(Long.parseLong(txnRef)).orElse(null));
            
            if (order == null) {
                log.warn("Order not found: {}", txnRef);
                response.put("RspCode", "01");
                response.put("Message", "Order not found");
                return response;
            }
            
            // Kiểm tra số tiền (VNPAY trả về số tiền × 100)
            long vnpayAmount = Long.parseLong(amountStr);
            long orderAmount = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();
            
            if (vnpayAmount != orderAmount) {
                log.warn("Amount mismatch. Order: {}, VNPAY: {}", orderAmount, vnpayAmount);
                response.put("RspCode", "04");
                response.put("Message", "invalid amount");
                return response;
            }
            
            // Kiểm tra trạng thái đơn hàng (chỉ cập nhật nếu đang PENDING)
            if (order.getStatus() != Order.OrderStatus.PENDING) {
                log.info("Order already processed: {}", txnRef);
                response.put("RspCode", "02");
                response.put("Message", "Order already confirmed");
                return response;
            }
            
            // Cập nhật trạng thái đơn hàng
            if (VnPayConfig.RESPONSE_SUCCESS.equals(responseCode) 
                    && VnPayConfig.TRANSACTION_SUCCESS.equals(transactionStatus)) {
                // Thanh toán thành công
                order.setStatus(Order.OrderStatus.CONFIRMED);
                // Lưu mã giao dịch VNPAY vào note (có thể thêm field riêng sau)
                String note = order.getNote() != null ? order.getNote() : "";
                order.setNote(note + "\n[VNPAY] TransactionNo: " + transactionNo);
                orderRepository.save(order);
                
                log.info("Order {} payment confirmed via VNPAY. TransactionNo: {}", txnRef, transactionNo);
                response.put("RspCode", "00");
                response.put("Message", "Confirm Success");
            } else {
                // Thanh toán thất bại
                order.setStatus(Order.OrderStatus.CANCELLED);
                String note = order.getNote() != null ? order.getNote() : "";
                order.setNote(note + "\n[VNPAY] Payment failed. ResponseCode: " + responseCode);
                orderRepository.save(order);
                
                log.warn("Order {} payment failed. ResponseCode: {}", txnRef, responseCode);
                response.put("RspCode", "00"); // Vẫn trả về 00 để VNPAY không retry
                response.put("Message", "Payment failed");
            }
            
        } catch (Exception e) {
            log.error("Error processing IPN", e);
            response.put("RspCode", "99");
            response.put("Message", "Unknow error");
        }
        
        return response;
    }

    /**
     * Xử lý Return URL từ VNPAY (hiển thị kết quả cho khách hàng)
     */
    public Map<String, Object> processReturnResponse(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        
        log.info("Processing return URL with params: {}", params);
        
        try {
            // Verify checksum
            String secureHash = params.get("vnp_SecureHash");
            if (secureHash == null) {
                log.warn("Missing vnp_SecureHash in return URL");
                result.put("success", false);
                result.put("message", "Thiếu thông tin xác thực từ VNPAY");
                return result;
            }
            
            boolean isValidChecksum = VnPayUtil.verifyChecksum(params, vnPayConfig.getHashSecret(), secureHash);
            if (!isValidChecksum) {
                log.warn("Invalid checksum in return URL. Hash data: {}", buildHashData(params));
                log.warn("Received secureHash: {}", secureHash);
                result.put("success", false);
                result.put("message", "Chữ ký không hợp lệ. Vui lòng liên hệ hỗ trợ.");
                return result;
            }
            
            // Lấy thông tin
            String txnRef = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");
            String transactionStatus = params.get("vnp_TransactionStatus");
            String transactionNo = params.get("vnp_TransactionNo");
            String amountStr = params.get("vnp_Amount");
            
            log.info("Return URL - TxnRef: {}, ResponseCode: {}, TransactionStatus: {}, TransactionNo: {}", 
                    txnRef, responseCode, transactionStatus, transactionNo);
            
            // Tìm đơn hàng - thử tìm theo orderCode trước, sau đó theo ID
            Order order = null;
            if (txnRef != null && !txnRef.isEmpty()) {
                // Thử tìm theo orderCode
                order = orderRepository.findByOrderCode(txnRef).orElse(null);
                
                // Nếu không tìm thấy, thử parse thành Long và tìm theo ID
                if (order == null) {
                    try {
                        Long orderId = Long.parseLong(txnRef);
                        order = orderRepository.findById(orderId).orElse(null);
                    } catch (NumberFormatException e) {
                        log.warn("Cannot parse txnRef as orderId: {}", txnRef);
                    }
                }
            }
            
            if (order == null) {
                log.error("Order not found for txnRef: {}", txnRef);
                result.put("success", false);
                result.put("message", "Không tìm thấy đơn hàng với mã: " + txnRef);
                result.put("txnRef", txnRef);
                return result;
            }
            
            log.info("Order found: id={}, orderCode={}, status={}, paymentMethod={}", 
                    order.getId(), order.getOrderCode(), order.getStatus(), order.getPaymentMethod());
            
            // Kiểm tra kết quả
            boolean isSuccess = VnPayConfig.RESPONSE_SUCCESS.equals(responseCode) 
                    && VnPayConfig.TRANSACTION_SUCCESS.equals(transactionStatus);
            
            // Tạo message chi tiết
            String message;
            if (isSuccess) {
                message = "Thanh toán thành công";
            } else {
                // Lấy message lỗi cụ thể từ responseCode
                message = getVnPayErrorMessage(responseCode);
            }
            
            result.put("success", isSuccess);
            result.put("orderId", order.getId());
            result.put("orderCode", order.getOrderCode());
            result.put("amount", order.getTotalAmount());
            result.put("responseCode", responseCode);
            result.put("transactionStatus", transactionStatus);
            result.put("transactionNo", transactionNo);
            result.put("message", message);
            
            log.info("Return URL processed successfully. Success: {}, Order: {}, ResponseCode: {}", 
                    isSuccess, order.getId(), responseCode);
            
        } catch (Exception e) {
            log.error("Error processing return URL", e);
            result.put("success", false);
            result.put("message", "Có lỗi xảy ra khi xử lý kết quả thanh toán: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        return result;
    }
    
    /**
     * Build hash data từ params để debug checksum
     */
    private String buildHashData(Map<String, String> params) {
        Map<String, String> paramsToHash = new HashMap<>(params);
        paramsToHash.remove("vnp_SecureHash");
        paramsToHash.remove("vnp_SecureHashType");
        
        TreeMap<String, String> sorted = new TreeMap<>(paramsToHash);
        StringBuilder hashData = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                if (i > 0) hashData.append("&");
                hashData.append(entry.getKey()).append("=").append(entry.getValue());
                i++;
            }
        }
        return hashData.toString();
    }
    
    /**
     * Lấy message lỗi từ responseCode của VNPAY
     */
    private String getVnPayErrorMessage(String responseCode) {
        if (responseCode == null) {
            return "Không nhận được mã phản hồi từ VNPAY";
        }
        
        switch (responseCode) {
            case "00":
                return "Thanh toán thành công";
            case "07":
                return "Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)";
            case "09":
                return "Thẻ/Tài khoản chưa đăng ký dịch vụ InternetBanking";
            case "10":
                return "Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11":
                return "Đã hết hạn chờ thanh toán";
            case "12":
                return "Thẻ/Tài khoản bị khóa";
            case "13":
                return "Nhập sai mật khẩu xác thực giao dịch (OTP)";
            case "24":
                return "Khách hàng hủy giao dịch";
            case "51":
                return "Tài khoản không đủ số dư để thực hiện giao dịch";
            case "65":
                return "Tài khoản đã vượt quá hạn mức giao dịch trong ngày";
            case "75":
                return "Ngân hàng thanh toán đang bảo trì";
            case "79":
                return "Nhập sai mật khẩu thanh toán quá số lần quy định";
            default:
                return "Thanh toán thất bại. Mã lỗi: " + responseCode;
        }
    }
}
