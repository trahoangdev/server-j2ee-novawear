package com.example.novawear.repository;

import com.example.novawear.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchByName(@Param("keyword") String keyword, Pageable pageable);

    List<Product> findTop8ByOrderByIdDesc();

    /** Sản phẩm nổi bật (featured = true), tối đa 8, ưu tiên id mới */
    List<Product> findTop8ByFeaturedTrueOrderByIdDesc();

    /** Sản phẩm bán chạy (bestseller = true), tối đa 8 */
    List<Product> findTop8ByBestsellerTrueOrderByIdDesc();

    @Query("SELECT p FROM Product p WHERE (:onSale is null or (p.salePrice is not null and p.salePrice < p.price)) and (:bestseller is null or p.bestseller = :bestseller) and (:isNew is null or p.isNew = :isNew)")
    Page<Product> findAllFiltered(@Param("onSale") Boolean onSale, @Param("bestseller") Boolean bestseller, @Param("isNew") Boolean isNew, Pageable pageable);

    /** Tìm sản phẩm theo slug */
    java.util.Optional<Product> findBySlug(String slug);
}
