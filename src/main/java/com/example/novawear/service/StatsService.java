package com.example.novawear.service;

import com.example.novawear.dto.RevenueStatsDto;
import com.example.novawear.entity.Order;
import com.example.novawear.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public RevenueStatsDto getRevenueStats(Instant from, Instant to) {
        List<Order.OrderStatus> completed = List.of(Order.OrderStatus.DELIVERED, Order.OrderStatus.SHIPPED, Order.OrderStatus.CONFIRMED, Order.OrderStatus.PROCESSING);
        BigDecimal total = orderRepository.sumTotalAmountByStatusInAndOrderDateBetween(completed, from, to);
        long count = orderRepository.countByOrderDateBetween(from, to);
        List<RevenueStatsDto.RevenueByDayDto> byDay = new ArrayList<>();
        for (LocalDate d = from.atZone(ZoneOffset.UTC).toLocalDate(); !d.isAfter(to.atZone(ZoneOffset.UTC).toLocalDate()); d = d.plusDays(1)) {
            Instant dayStart = d.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant dayEnd = d.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            BigDecimal dayRevenue = orderRepository.sumTotalAmountByStatusInAndOrderDateBetween(completed, dayStart, dayEnd);
            long dayCount = orderRepository.countByOrderDateBetween(dayStart, dayEnd);
            byDay.add(new RevenueStatsDto.RevenueByDayDto(d.toString(), dayRevenue != null ? dayRevenue : BigDecimal.ZERO, dayCount));
        }
        return new RevenueStatsDto(
                total != null ? total : BigDecimal.ZERO,
                count,
                from,
                to,
                byDay
        );
    }

    @Transactional(readOnly = true)
    public RevenueStatsDto getRevenueStatsLast30Days() {
        Instant to = Instant.now();
        Instant from = to.minus(30, ChronoUnit.DAYS);
        return getRevenueStats(from, to);
    }
}
