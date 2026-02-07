package com.example.novawear.repository;

import com.example.novawear.dto.TopProductDto;
import com.example.novawear.entity.Order.OrderStatus;
import com.example.novawear.entity.OrderDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    @Query("SELECT new com.example.novawear.dto.TopProductDto(d.product.id, d.product.name, d.product.imageUrl, SUM(CAST(d.quantity AS long))) "
            +
            "FROM OrderDetail d JOIN d.order o " +
            "WHERE o.status IN :statuses AND o.orderDate BETWEEN :from AND :to " +
            "GROUP BY d.product.id, d.product.name, d.product.imageUrl " +
            "ORDER BY SUM(d.quantity) DESC")
    List<TopProductDto> findTopSellingProducts(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable);
}
