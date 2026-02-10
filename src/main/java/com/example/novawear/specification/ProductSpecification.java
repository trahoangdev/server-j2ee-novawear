package com.example.novawear.specification;

import com.example.novawear.entity.Product;
import com.example.novawear.entity.Review;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

public class ProductSpecification {
    public static Specification<Product> filter(
            List<Long> categoryIds,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            List<String> sizes,
            List<String> colors,
            Double minRating,
            String genderStr,
            String search,
            Boolean onSale,
            Boolean bestseller,
            Boolean isNew,
            Boolean lowStock) {
        return (root, query, cb) -> {
            Predicate p = cb.conjunction();

            // Search
            if (search != null && !search.isBlank()) {
                p = cb.and(p, cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
            }

            // Flags
            if (Boolean.TRUE.equals(onSale)) {
                // Sale price exists and < price
                p = cb.and(p, cb.and(
                        cb.isNotNull(root.get("salePrice")),
                        cb.lessThan(root.get("salePrice"), root.get("price"))));
            }
            if (Boolean.TRUE.equals(bestseller)) {
                p = cb.and(p, cb.equal(root.get("bestseller"), true));
            }
            if (Boolean.TRUE.equals(isNew)) {
                p = cb.and(p, cb.equal(root.get("isNew"), true));
            }

            // Low Stock
            if (Boolean.TRUE.equals(lowStock)) {
                p = cb.and(p, cb.lessThan(root.get("stock"), 10)); // Hardcoded 10 based on controller logic
            }

            // Categories
            if (categoryIds != null && !categoryIds.isEmpty()) {
                p = cb.and(p, root.get("category").get("id").in(categoryIds));
            }

            // Price range - check salePrice first if present, otherwise price
            // Logic: effectivePrice = coalesce(salePrice, price)
            Expression<BigDecimal> effectivePrice = cb.coalesce(root.get("salePrice"), root.get("price"));

            if (minPrice != null) {
                p = cb.and(p, cb.greaterThanOrEqualTo(effectivePrice, minPrice));
            }
            if (maxPrice != null) {
                p = cb.and(p, cb.lessThanOrEqualTo(effectivePrice, maxPrice));
            }

            // Sizes (stored as JSON array string, e.g. ["S","M"])
            if (sizes != null && !sizes.isEmpty()) {
                Predicate sizePredicate = cb.disjunction();
                for (String s : sizes) {
                    // Simple logic: check if JSON string contains "S"
                    // Be careful with substrings like "XL" matching "L".
                    // Robust check for JSON array: LIKE '%"S"%' (with quotes)
                    sizePredicate = cb.or(sizePredicate, cb.like(root.get("sizes"), "%\"" + s + "\"%"));
                }
                p = cb.and(p, sizePredicate);
            }

            // Colors (stored as JSON array of objects, e.g. [{"name":"Red","hex":"..."}])
            if (colors != null && !colors.isEmpty()) {
                Predicate colorPredicate = cb.disjunction();
                for (String c : colors) {
                    // Simple check for name: LIKE '%"name":"Red"%'
                    // This assumes the name field is consistent.
                    // Or simpler: just match the string name if it's unique enough.
                    // Let's rely on standard format: "name":"ColorName"
                    colorPredicate = cb.or(colorPredicate, cb.like(root.get("colors"), "%\"name\":\"" + c + "\"%"));
                }
                p = cb.and(p, colorPredicate);
            }

            // Rating - usage of subquery
            if (minRating != null) {
                Subquery<Double> subquery = query.subquery(Double.class);
                Root<Review> reviewRoot = subquery.from(Review.class);
                subquery.select(cb.avg(reviewRoot.get("rating")));
                subquery.where(cb.equal(reviewRoot.get("product"), root));

                // If avg rating >= minRating
                // Handle null case (no reviews) -> treated as 0 rating or exclude?
                // Usually exclude if rating filter is applied.
                p = cb.and(p,
                        cb.greaterThanOrEqualTo(cb.coalesce(subquery, cb.literal(0.0)), minRating));
            }

            // Gender
            if (genderStr != null && !genderStr.isEmpty()) {
                try {
                    Product.Gender gender = Product.Gender.valueOf(genderStr.toUpperCase());
                    p = cb.and(p, cb.equal(root.get("gender"), gender));
                } catch (IllegalArgumentException e) {
                    // ignore invalid gender
                }
            }

            return p;
        };
    }
}
