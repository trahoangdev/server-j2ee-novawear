package com.example.novawear.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatsDto {

    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Instant from;
    private Instant to;
    private List<RevenueByDayDto> byDay;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueByDayDto {
        private String date;
        private BigDecimal revenue;
        private Long orderCount;
    }
}
