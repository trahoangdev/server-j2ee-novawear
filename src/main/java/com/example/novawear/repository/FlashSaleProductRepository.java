package com.example.novawear.repository;

import com.example.novawear.entity.FlashSaleProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashSaleProductRepository extends JpaRepository<FlashSaleProduct, Long> {
}
