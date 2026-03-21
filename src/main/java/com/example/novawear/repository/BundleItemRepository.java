package com.example.novawear.repository;

import com.example.novawear.entity.BundleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BundleItemRepository extends JpaRepository<BundleItem, Long> {
}
