package com.example.novawear.config;

import com.example.novawear.entity.*;
import com.example.novawear.repository.*;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("seed")
public class SeedDataRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final com.example.novawear.repository.BannerRepository bannerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Seed: checking database...");

        // Seed banners nếu chưa có (chạy độc lập với categories)
        if (bannerRepository.count() == 0) {
            log.info("Seed: inserting default banners...");
            String imgBase = "https://images.unsplash.com/photo-";
            List<Banner> defaultBanners = Arrays.asList(
                    Banner.builder().title("Bộ Sưu Tập\nXuân Hè 2024").subtitle("Khám phá những xu hướng thời trang mới nhất")
                            .imageUrl(imgBase + "1483985988355-763728e1935b?w=1600&q=80").linkUrl("/shop").ctaText("Khám Phá Ngay").sortOrder(0).active(true).build(),
                    Banner.builder().title("Thanh Lịch\nMỗi Ngày").subtitle("Phong cách công sở hiện đại, tinh tế")
                            .imageUrl(imgBase + "1490481651871-ab68de25d43d?w=1600&q=80").linkUrl("/shop?category=tops").ctaText("Xem Bộ Sưu Tập").sortOrder(1).active(true).build(),
                    Banner.builder().title("Giảm Giá\nĐến 50%").subtitle("Ưu đãi đặc biệt cho thành viên mới")
                            .imageUrl(imgBase + "1469334031218-e382a71b716b?w=1600&q=80").linkUrl("/shop?sale=true").ctaText("Mua Ngay").sortOrder(2).active(true).build()
            );
            bannerRepository.saveAll(defaultBanners);
            log.info("Seed: banners inserted count={}", defaultBanners.size());
        }

        // Seed users nếu chưa có (admin/customer từ lần chạy trước vẫn chỉ có 1 user)
        User admin = userRepository.findByUsername("admin").orElseGet(() -> {
            User u = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@novawear.com")
                    .role(User.Role.ADMIN)
                    .active(true)
                    .build();
            return userRepository.save(u);
        });

        User customer1 = userRepository.findByUsername("customer").orElseGet(() -> {
            User u = User.builder()
                    .username("customer")
                    .password(passwordEncoder.encode("customer123"))
                    .email("customer@example.com")
                    .role(User.Role.USER)
                    .active(true)
                    .build();
            return userRepository.save(u);
        });

        User customer2 = userRepository.findByUsername("nguyenvan").orElseGet(() -> {
            User u = User.builder()
                    .username("nguyenvan")
                    .password(passwordEncoder.encode("123456"))
                    .email("nguyenvan@gmail.com")
                    .role(User.Role.USER)
                    .active(true)
                    .build();
            return userRepository.save(u);
        });

        // Seed categories + products + orders + reviews chỉ khi chưa có danh mục
        if (categoryRepository.count() > 0) {
            log.info("Seed: categories already exist (count={}), skip catalog/orders/reviews.", categoryRepository.count());
            return;
        }

        log.info("Seed: inserting categories, products, orders, reviews...");
        try {

        // --- Categories ---
        Category catAo = categoryRepository.save(Category.builder().name("Áo").description("Áo thun, sơ mi, blazer").build());
        Category catQuan = categoryRepository.save(Category.builder().name("Quần").description("Quần short, jean, palazzo").build());
        Category catVay = categoryRepository.save(Category.builder().name("Váy").description("Váy midi, maxi, công sở").build());
        Category catPhuKien = categoryRepository.save(Category.builder().name("Phụ kiện").description("Túi, ví, thắt lưng").build());
        Category catGiay = categoryRepository.save(Category.builder().name("Giày").description("Sandal, giày thể thao").build());
        Category catTui = categoryRepository.save(Category.builder().name("Túi xách").description("Túi đeo chéo, túi xách").build());

        // --- Products ---
        String imgBase = "https://images.unsplash.com/photo-";
        Product p1 = productRepository.save(Product.builder()
                .name("Áo Blazer Dáng Rộng Premium")
                .price(new BigDecimal("1890000"))
                .description("Áo blazer thiết kế hiện đại, chất liệu cao cấp.")
                .imageUrl(imgBase + "1594938298603-c8148c4dae35?w=800&q=80")
                .category(catAo)
                .stock(45)
                .build());
        Product p2 = productRepository.save(Product.builder()
                .name("Váy Midi Hoa Nhí Vintage")
                .price(new BigDecimal("890000"))
                .description("Váy midi họa tiết hoa nhí, chất vải mềm thoáng.")
                .imageUrl(imgBase + "1572804013309-59a88b7e92f1?w=800&q=80")
                .category(catVay)
                .stock(32)
                .build());
        Product p3 = productRepository.save(Product.builder()
                .name("Quần Palazzo Ống Rộng")
                .price(new BigDecimal("750000"))
                .description("Quần ống rộng thanh lịch, chất liệu cao cấp.")
                .imageUrl(imgBase + "1506629082955-511b1aa562c8?w=800&q=80")
                .category(catQuan)
                .stock(58)
                .build());
        Product p4 = productRepository.save(Product.builder()
                .name("Túi Xách Mini Đeo Chéo")
                .price(new BigDecimal("1290000"))
                .description("Túi mini tiện dụng, thiết kế sang trọng.")
                .imageUrl(imgBase + "1548036328-c9fa89d128fa?w=800&q=80")
                .category(catTui)
                .stock(23)
                .build());
        Product p5 = productRepository.save(Product.builder()
                .name("Giày Sandal Quai Ngang")
                .price(new BigDecimal("590000"))
                .description("Sandal quai ngang minimalist, đế êm.")
                .imageUrl(imgBase + "1542291026-7eec264c27ff?w=800&q=80")
                .category(catGiay)
                .stock(40)
                .build());
        Product p6 = productRepository.save(Product.builder()
                .name("Áo Thun Basic Cotton")
                .price(new BigDecimal("299000"))
                .description("Áo thun cotton 100%, form regular.")
                .imageUrl(imgBase + "1521572163474-6864f9cf17ab?w=800&q=80")
                .category(catAo)
                .stock(120)
                .build());
        Product p7 = productRepository.save(Product.builder()
                .name("Quần Jean Slim Fit")
                .price(new BigDecimal("650000"))
                .description("Quần jean slim fit co giãn nhẹ.")
                .imageUrl(imgBase + "1594633312681-425c7b97ccd1?w=800&q=80")
                .category(catQuan)
                .stock(55)
                .build());
        Product p8 = productRepository.save(Product.builder()
                .name("Ví Da Nam Cao Cấp")
                .price(new BigDecimal("450000"))
                .description("Ví da bò thật, nhiều ngăn.")
                .imageUrl(imgBase + "1611923134239-b9be5b4d1b2b?w=800&q=80")
                .category(catPhuKien)
                .stock(30)
                .build());

        // --- Orders (một vài đơn trong quá khứ để có thống kê) ---
        Instant now = Instant.now();
        Instant day3 = now.minus(3, ChronoUnit.DAYS);
        Instant day7 = now.minus(7, ChronoUnit.DAYS);
        Instant day15 = now.minus(15, ChronoUnit.DAYS);

        Order order1 = Order.builder()
                .user(customer1)
                .totalAmount(new BigDecimal("2780000"))
                .status(Order.OrderStatus.DELIVERED)
                .orderDate(day15)
                .build();
        OrderDetail od1a = OrderDetail.builder().order(order1).product(p1).quantity(1).price(p1.getPrice()).build();
        OrderDetail od1b = OrderDetail.builder().order(order1).product(p2).quantity(1).price(p2.getPrice()).build();
        order1.getOrderDetails().add(od1a);
        order1.getOrderDetails().add(od1b);
        orderRepository.save(order1);

        Order order2 = Order.builder()
                .user(customer2)
                .totalAmount(new BigDecimal("1880000"))
                .status(Order.OrderStatus.SHIPPED)
                .orderDate(day7)
                .build();
        OrderDetail od2a = OrderDetail.builder().order(order2).product(p4).quantity(1).price(p4.getPrice()).build();
        OrderDetail od2b = OrderDetail.builder().order(order2).product(p6).quantity(2).price(p6.getPrice()).build();
        order2.getOrderDetails().add(od2a);
        order2.getOrderDetails().add(od2b);
        orderRepository.save(order2);

        Order order3 = Order.builder()
                .user(customer1)
                .totalAmount(new BigDecimal("590000"))
                .status(Order.OrderStatus.CONFIRMED)
                .orderDate(day3)
                .build();
        OrderDetail od3a = OrderDetail.builder().order(order3).product(p5).quantity(1).price(p5.getPrice()).build();
        order3.getOrderDetails().add(od3a);
        orderRepository.save(order3);

        Order order4 = Order.builder()
                .user(customer2)
                .totalAmount(new BigDecimal("940000"))
                .status(Order.OrderStatus.DELIVERED)
                .orderDate(day7)
                .build();
        OrderDetail od4a = OrderDetail.builder().order(order4).product(p3).quantity(1).price(p3.getPrice()).build();
        OrderDetail od4b = OrderDetail.builder().order(order4).product(p8).quantity(1).price(p8.getPrice()).build();
        order4.getOrderDetails().add(od4a);
        order4.getOrderDetails().add(od4b);
        orderRepository.save(order4);

        // --- Reviews ---
        reviewRepository.save(Review.builder().product(p1).user(customer1).rating(5).comment("Áo đẹp, vải tốt.").approved(true).build());
        reviewRepository.save(Review.builder().product(p1).user(customer2).rating(4).comment("Mặc vừa, giao hàng nhanh.").approved(true).build());
        reviewRepository.save(Review.builder().product(p2).user(customer1).rating(5).comment("Váy rất đẹp, đúng hình.").approved(true).build());
        reviewRepository.save(Review.builder().product(p3).user(customer2).rating(4).comment("Quần đẹp nhưng hơi dài.").approved(false).build());
        reviewRepository.save(Review.builder().product(p4).user(customer1).rating(5).comment("Túi xinh, chất lượng tốt.").approved(true).build());
        reviewRepository.save(Review.builder().product(p6).user(customer2).rating(5).comment("Áo basic mặc rất thoải mái.").approved(true).build());

        log.info("Seed: done. Users={}, Categories={}, Products={}, Orders={}, Reviews={}",
                userRepository.count(), categoryRepository.count(), productRepository.count(),
                orderRepository.count(), reviewRepository.count());
        } catch (Exception e) {
            log.error("Seed: failed to insert data", e);
            throw e;
        }
    }
}
