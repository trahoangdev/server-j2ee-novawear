package com.example.novawear.repository;

import com.example.novawear.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductIdAndApprovedTrue(Long productId, Pageable pageable);

    Page<Review> findByProductId(Long productId, Pageable pageable);

    Page<Review> findAllByOrderByIdDesc(Pageable pageable);
}
