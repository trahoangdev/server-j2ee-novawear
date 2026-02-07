package com.example.novawear.config;

import com.example.novawear.entity.Order;
import com.example.novawear.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCodeMigrationRunner implements ApplicationRunner {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Checking for orders with missing order codes...");
        List<Order> orders = orderRepository.findByOrderCodeIsNull();
        if (orders.isEmpty()) {
            log.info("All orders have valid order codes.");
            return;
        }

        log.info("Found {} orders with missing order codes. Updating...", orders.size());
        for (Order order : orders) {
            // Use padded ID format for existing orders
            String paddedId = String.format("%06d", order.getId());
            order.setOrderCode(paddedId);
        }
        orderRepository.saveAll(orders);
        log.info("Successfully updated {} orders.", orders.size());
    }
}
