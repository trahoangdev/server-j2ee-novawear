package com.example.novawear.repository;

import com.example.novawear.entity.ProductBundle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductBundleRepository extends JpaRepository<ProductBundle, Long> {

    Page<ProductBundle> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<ProductBundle> findByActiveTrueOrderByCreatedAtDesc();
}
