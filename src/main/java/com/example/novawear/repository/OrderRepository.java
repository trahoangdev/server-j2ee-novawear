package com.example.novawear.repository;

import com.example.novawear.entity.Order;
import com.example.novawear.entity.Order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByUserUsername(String username, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status IN :statuses AND o.orderDate BETWEEN :from AND :to")
    BigDecimal sumTotalAmountByStatusInAndOrderDateBetween(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("from") Instant from,
            @Param("to") Instant to);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :from AND :to")
    long countByOrderDateBetween(@Param("from") Instant from, @Param("to") Instant to);
}
