package com.example.novawear.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class VnPayUtil {

    /**
     * Build payment URL với đầy đủ tham số và checksum
     */
    public static String buildPaymentUrl(Map<String, String> params, String secretKey) {
        // Sắp xếp params theo alphabet
        TreeMap<String, String> sortedParams = new TreeMap<>(params);
        
        // Build query string
        StringBuilder query = new StringBuilder();
        StringBuilder hashData = new StringBuilder();
        
        int i = 0;
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            if (value != null && !value.isEmpty()) {
                if (i > 0) {
                    query.append("&");
                    hashData.append("&");
                }
                
                String encodedKey = URLEncoder.encode(key, StandardCharsets.US_ASCII);
                String encodedValue = URLEncoder.encode(value, StandardCharsets.US_ASCII);
                
                query.append(encodedKey).append("=").append(encodedValue);
                hashData.append(encodedKey).append("=").append(encodedValue);
                i++;
            }
        }
        
        // Tạo checksum SHA512
        String secureHash = hmacSHA512(secretKey, hashData.toString());
        if (secureHash == null) {
            throw new RuntimeException("Failed to generate VNPAY checksum");
        }
        query.append("&vnp_SecureHash=").append(secureHash);
        
        return query.toString();
    }

    /**
     * Verify checksum từ response của VNPAY
     */
    public static boolean verifyChecksum(Map<String, String> params, String secretKey, String secureHash) {
        // Loại bỏ vnp_SecureHash và vnp_SecureHashType
        Map<String, String> paramsToVerify = new HashMap<>(params);
        paramsToVerify.remove("vnp_SecureHash");
        paramsToVerify.remove("vnp_SecureHashType");
        
        // Sắp xếp theo alphabet
        TreeMap<String, String> sortedParams = new TreeMap<>(paramsToVerify);
        
        // Build hash data
        StringBuilder hashData = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            if (value != null && !value.isEmpty()) {
                if (i > 0) {
                    hashData.append("&");
                }
                
                String encodedKey = URLEncoder.encode(key, StandardCharsets.US_ASCII);
                String encodedValue = URLEncoder.encode(value, StandardCharsets.US_ASCII);
                
                hashData.append(encodedKey).append("=").append(encodedValue);
                i++;
            }
        }
        
        // Verify checksum
        String calculatedHash = hmacSHA512(secretKey, hashData.toString());
        return calculatedHash.equals(secureHash);
    }

    /**
     * HMAC SHA512
     */
    private static String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKeySpec);
            byte[] hashBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Error generating HMAC SHA512", e);
            return null;
        }
    }

    /**
     * Lấy IP address từ request
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // Nếu có nhiều IP, lấy IP đầu tiên
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        
        return ipAddress;
    }

    /**
     * Format date theo định dạng VNPAY: yyyyMMddHHmmss (GMT+7)
     */
    public static String formatDate(ZonedDateTime dateTime) {
        try {
            if (dateTime == null) {
                throw new IllegalArgumentException("DateTime cannot be null");
            }
            ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
            ZonedDateTime vietnamTime = dateTime.withZoneSameInstant(vietnamZone);
            return vietnamTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        } catch (Exception e) {
            log.error("Error formatting date", e);
            throw new RuntimeException("Failed to format date: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo thời gian hết hạn (mặc định +15 phút)
     */
    public static String createExpireDate(int minutesToAdd) {
        try {
            ZonedDateTime expireTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusMinutes(minutesToAdd);
            return formatDate(expireTime);
        } catch (Exception e) {
            log.error("Error creating expire date", e);
            throw new RuntimeException("Failed to create expire date: " + e.getMessage(), e);
        }
    }
}
