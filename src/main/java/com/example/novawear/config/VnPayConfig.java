package com.example.novawear.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Data
public class VnPayConfig {

    private String tmnCode;
    private String hashSecret;
    private String paymentUrl;
    private String returnUrl;
    private String ipnUrl;

    // VNPAY Constants
    public static final String VERSION = "2.1.0";
    public static final String COMMAND = "pay";
    public static final String CURRENCY = "VND";
    public static final String LOCALE_VN = "vn";
    public static final String LOCALE_EN = "en";
    public static final String ORDER_TYPE = "other";
    
    // Response codes
    public static final String RESPONSE_SUCCESS = "00";
    public static final String TRANSACTION_SUCCESS = "00";
}
