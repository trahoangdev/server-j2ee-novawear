package com.example.novawear.service;

import com.example.novawear.dto.CheckoutRequest;
import com.example.novawear.dto.OrderDto;
import com.example.novawear.entity.Order;
import com.example.novawear.entity.OrderDetail;
import com.example.novawear.entity.Product;
import com.example.novawear.entity.User;
import com.example.novawear.repository.OrderRepository;
import com.example.novawear.repository.ProductRepository;
import com.example.novawear.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<OrderDto> findByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(OrderDto::from);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> findByUsername(String username, Pageable pageable) {
        return orderRepository.findByUserUsername(username, pageable).map(OrderDto::from);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable).map(OrderDto::from);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> findByStatus(Order.OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable).map(OrderDto::from);
    }

    @Transactional(readOnly = true)
    public OrderDto getById(Long id) {
        Order o = orderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        return OrderDto.from(o);
    }

    @Transactional
    public OrderDto checkout(String username, CheckoutRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart must not be empty");
        }
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<OrderDetail> details = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (var item : request.getItems()) {
            if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity() < 1) {
                throw new IllegalArgumentException("Invalid item: productId and quantity required");
            }
            Product product = productRepository.findById(item.getProductId()).orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProductId()));
            if (product.getPrice() == null) {
                throw new IllegalArgumentException("Product has no price: " + product.getName());
            }
            if (product.getStock() < item.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(lineTotal);
            OrderDetail od = OrderDetail.builder()
                    .product(product)
                    .quantity(item.getQuantity())
                    .price(product.getPrice())
                    .build();
            details.add(od);
            product.setStock(product.getStock() - item.getQuantity());
        }
        Order order = Order.builder()
                .user(user)
                .totalAmount(total)
                .status(Order.OrderStatus.PENDING)
                .build();
        for (OrderDetail od : details) {
            od.setOrder(order);
            order.getOrderDetails().add(od);
        }
        order = orderRepository.save(order);
        Order saved = orderRepository.findById(order.getId()).orElseThrow();
        return OrderDto.from(saved);
    }

    @Transactional
    public OrderDto updateStatus(Long id, Order.OrderStatus status) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        order.setStatus(status);
        order = orderRepository.save(order);
        return OrderDto.from(order);
    }
}
