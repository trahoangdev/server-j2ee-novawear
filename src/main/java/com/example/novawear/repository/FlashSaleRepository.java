package com.example.novawear.repository;

import com.example.novawear.entity.FlashSale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface FlashSaleRepository extends JpaRepository<FlashSale, Long> {

    Page<FlashSale> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT fs FROM FlashSale fs WHERE fs.active = true AND fs.startTime <= :now AND fs.endTime > :now")
    List<FlashSale> findActiveNow(@Param("now") Instant now);
}
