package com.example.novawear.service;

import com.example.novawear.dto.ReviewCreateRequest;
import com.example.novawear.dto.ReviewDto;
import com.example.novawear.dto.ReviewUpdateRequest;
import com.example.novawear.entity.Product;
import com.example.novawear.entity.Review;
import com.example.novawear.entity.User;
import com.example.novawear.repository.ProductRepository;
import com.example.novawear.repository.ReviewRepository;
import com.example.novawear.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ReviewDto> findByProductIdApproved(Long productId, Pageable pageable) {
        return reviewRepository.findByProductIdAndApprovedTrue(productId, pageable).stream()
                .map(ReviewDto::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ReviewDto> findByProductId(Long productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable).map(ReviewDto::from);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDto> findAll(Pageable pageable) {
        return reviewRepository.findAllByOrderByIdDesc(pageable).map(ReviewDto::from);
    }

    @Transactional(readOnly = true)
    public ReviewDto getById(Long id) {
        Review r = reviewRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Review not found: " + id));
        return ReviewDto.from(r);
    }

    @Transactional
    public ReviewDto createByAdmin(ReviewCreateRequest req) {
        User user = userRepository.findById(req.getUserId()).orElseThrow(() -> new IllegalArgumentException("User not found: " + req.getUserId()));
        Product product = productRepository.findById(req.getProductId()).orElseThrow(() -> new IllegalArgumentException("Product not found: " + req.getProductId()));
        Review r = Review.builder()
                .product(product)
                .user(user)
                .rating(req.getRating())
                .comment(req.getComment() != null ? req.getComment() : "")
                .approved(Boolean.TRUE.equals(req.getApproved()))
                .build();
        r = reviewRepository.save(r);
        ReviewDto result = ReviewDto.from(r);
        result.setUsername(user.getUsername());
        return result;
    }

    @Transactional
    public ReviewDto update(Long id, ReviewUpdateRequest req) {
        Review r = reviewRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Review not found: " + id));
        if (req.getRating() != null) r.setRating(req.getRating());
        if (req.getComment() != null) r.setComment(req.getComment());
        if (req.getApproved() != null) r.setApproved(req.getApproved());
        r = reviewRepository.save(r);
        return ReviewDto.from(r);
    }

    @Transactional
    public ReviewDto create(String username, Long productId, ReviewDto dto) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        Review r = Review.builder()
                .product(product)
                .user(user)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .approved(false)
                .build();
        r = reviewRepository.save(r);
        ReviewDto result = ReviewDto.from(r);
        result.setUsername(user.getUsername());
        return result;
    }

    @Transactional
    public ReviewDto approve(Long id, boolean approved) {
        Review r = reviewRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Review not found: " + id));
        r.setApproved(approved);
        r = reviewRepository.save(r);
        return ReviewDto.from(r);
    }

    @Transactional
    public void delete(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new IllegalArgumentException("Review not found: " + id);
        }
        reviewRepository.deleteById(id);
    }
}
