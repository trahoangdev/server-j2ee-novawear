package com.example.novawear.repository;

import com.example.novawear.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    List<Banner> findAllByActiveTrueOrderBySortOrderAsc();
}
