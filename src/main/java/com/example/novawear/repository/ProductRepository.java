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
public interface ProductRepository extends JpaRepository<Product, Long>,
                org.springframework.data.jpa.repository.JpaSpecificationExecutor<Product> {

        Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

        @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        Page<Product> searchByName(@Param("keyword") String keyword, Pageable pageable);

        List<Product> findTop8ByOrderByIdDesc();

        /** Sản phẩm nổi bật (featured = true), tối đa 8, ưu tiên id mới */
        List<Product> findTop8ByFeaturedTrueOrderByIdDesc();

        /** Sản phẩm bán chạy (bestseller = true), tối đa 8 */
        List<Product> findTop8ByBestsellerTrueOrderByIdDesc();

        @Query("SELECT p FROM Product p WHERE (:onSale is null or (p.salePrice is not null and p.salePrice < p.price)) and (:bestseller is null or p.bestseller = :bestseller) and (:isNew is null or p.isNew = :isNew)")
        Page<Product> findAllFiltered(@Param("onSale") Boolean onSale, @Param("bestseller") Boolean bestseller,
                        @Param("isNew") Boolean isNew, Pageable pageable);

        /** Tìm sản phẩm theo slug */
        java.util.Optional<Product> findBySlug(String slug);

        Page<Product> findByStockLessThan(Integer stock, Pageable pageable);

        /**
         * Sản phẩm liên quan: cùng category, loại trừ sản phẩm hiện tại
         */
        @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.id != :productId")
        List<Product> findRelatedByCategory(@Param("categoryId") Long categoryId, @Param("productId") Long productId,
                        Pageable pageable);

        /**
         * Sản phẩm tương tự: cùng khoảng giá (+-30%), loại trừ sản phẩm hiện tại
         */
        @Query("SELECT p FROM Product p WHERE p.id != :productId " +
                        "AND p.price BETWEEN :minPrice AND :maxPrice " +
                        "ORDER BY ABS(p.price - :targetPrice)")
        List<Product> findSimilarByPrice(@Param("productId") Long productId,
                        @Param("targetPrice") java.math.BigDecimal targetPrice,
                        @Param("minPrice") java.math.BigDecimal minPrice,
                        @Param("maxPrice") java.math.BigDecimal maxPrice,
                        Pageable pageable);

        /**
         * Lọc sản phẩm theo giới tính
         */
        Page<Product> findByGender(com.example.novawear.entity.Product.Gender gender, Pageable pageable);

        /**
         * Lọc sản phẩm theo giới tính và category
         */
        Page<Product> findByGenderAndCategoryId(com.example.novawear.entity.Product.Gender gender, Long categoryId,
                        Pageable pageable);
}
