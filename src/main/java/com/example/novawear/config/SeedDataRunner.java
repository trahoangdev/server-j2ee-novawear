package com.example.novawear.config;

import com.example.novawear.entity.*;
import com.example.novawear.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeedDataRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final com.example.novawear.repository.BannerRepository bannerRepository;
    private final com.example.novawear.repository.VoucherRepository voucherRepository;
    private final com.example.novawear.repository.SubscriberRepository subscriberRepository;
    private final com.example.novawear.repository.FlashSaleRepository flashSaleRepository;
    private final PasswordEncoder passwordEncoder;

    private String generateSlug(String name) {
        if (name == null || name.isBlank()) return null;
        String slug = name.trim()
                .toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return slug.isEmpty() ? null : slug;
    }

    private String generateOrderCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Seed: checking database...");

        // Seed banners if none exist
        if (bannerRepository.count() == 0) {
            log.info("Seed: inserting banners...");
            String imgBase = "https://images.unsplash.com/photo-";
            List<Banner> banners = Arrays.asList(
                    Banner.builder()
                            .title("Bộ Sưu Tập\nXuân Hè 2024")
                            .subtitle("Khám phá những xu hướng thời trang mới nhất")
                            .imageUrl(imgBase + "1483985988355-763728e1935b?w=1600&q=80")
                            .linkUrl("/shop")
                            .ctaText("Khám Phá Ngay")
                            .badgeText("NEW")
                            .sortOrder(0)
                            .active(true)
                            .build(),
                    Banner.builder()
                            .title("Thanh Lịch\nMỗi Ngày")
                            .subtitle("Phong cách công sở hiện đại, tinh tế")
                            .imageUrl(imgBase + "1490481651871-ab68de25d43d?w=1600&q=80")
                            .linkUrl("/shop?category=tops")
                            .ctaText("Xem Bộ Sưu Tập")
                            .sortOrder(1)
                            .active(true)
                            .build(),
                    Banner.builder()
                            .title("Giảm Giá\nĐến 50%")
                            .subtitle("Ưu đãi đặc biệt cho thành viên mới")
                            .imageUrl(imgBase + "1469334031218-e382a71b716b?w=1600&q=80")
                            .linkUrl("/shop?sale=true")
                            .ctaText("Mua Ngay")
                            .badgeText("SALE")
                            .sortOrder(2)
                            .active(true)
                            .build()
            );
            bannerRepository.saveAll(banners);
            log.info("Seed: {} banners inserted", banners.size());
        }

        // Seed users
        User admin = userRepository.findByUsername("admin").orElseGet(() -> {
            User u = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@novawear.com")
                    .fullName("Quản Trị Viên")
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
                    .fullName("Nguyễn Thị Lan")
                    .phone("0901234567")
                    .address("123 Đường Lê Lợi, Quận 1, TP.HCM")
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
                    .fullName("Trần Văn Nam")
                    .phone("0912345678")
                    .address("45 Đường Nguyễn Huệ, Quận 1, TP.HCM")
                    .role(User.Role.USER)
                    .active(true)
                    .build();
            return userRepository.save(u);
        });

        User customer3 = userRepository.findByUsername("lehong").orElseGet(() -> {
            User u = User.builder()
                    .username("lehong")
                    .password(passwordEncoder.encode("123456"))
                    .email("lehong@gmail.com")
                    .fullName("Lê Thị Hồng")
                    .phone("0933123456")
                    .address("78 Đường Pasteur, Quận 3, TP.HCM")
                    .role(User.Role.USER)
                    .active(true)
                    .build();
            return userRepository.save(u);
        });

        // Skip catalog seeding if already exists
        if (categoryRepository.count() > 0) {
            log.info("Seed: database already seeded (categories={}), skip catalog/orders/reviews.",
                    categoryRepository.count());
            return;
        }

        log.info("Seed: inserting full catalog...");
        try {

            // ===================== CATEGORIES =====================
            Category catAo = categoryRepository.save(Category.builder()
                    .name("Áo")
                    .description("Áo thun, sơ mi, blazer, áo polo - Đa dạng phong cách từ basic đến sang trọng")
                    .imageUrl("https://images.unsplash.com/photo-1562157873-818bc0726f68?w=800&q=80")
                    .build());
            Category catQuan = categoryRepository.save(Category.builder()
                    .name("Quần")
                    .description("Quần short, jean, palazzo, quần ống rộng - Thoải mái cả ngày dài")
                    .imageUrl("https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=800&q=80")
                    .build());
            Category catVay = categoryRepository.save(Category.builder()
                    .name("Váy")
                    .description("Váy midi, maxi, công sở - Nữ tính và thanh lịch")
                    .imageUrl("https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=800&q=80")
                    .build());
            Category catPhuKien = categoryRepository.save(Category.builder()
                    .name("Phụ Kiện")
                    .description("Túi, ví, thắt lưng, trang sức - Điểm nhấn hoàn hảo")
                    .imageUrl("https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=800&q=80")
                    .build());
            Category catGiay = categoryRepository.save(Category.builder()
                    .name("Giày")
                    .description("Sandal, giày thể thao, giày cao gót - Phong cách mọi lúc mọi nơi")
                    .imageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80")
                    .build());
            Category catTui = categoryRepository.save(Category.builder()
                    .name("Túi Xách")
                    .description("Túi đeo chéo, túi xách tay, ba lô - Tiện dụng và thời trang")
                    .imageUrl("https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&q=80")
                    .build());

            // ===================== PRODUCTS =====================
            String imgBase = "https://images.unsplash.com/photo-";

            // Áo
            Product p1 = productRepository.save(Product.builder()
                    .name("Áo Blazer Dáng Rộng Premium")
                    .slug(generateSlug("Áo Blazer Dáng Rộng Premium"))
                    .price(new BigDecimal("1890000"))
                    .description("Áo blazer thiết kế hiện đại với dáng rộng thoải mái. Chất liệu wool blend cao cấp, lót lụa mịn màng. Phù hợp đi làm công sở hoặc dạo phố.")
                    .imageUrl(imgBase + "1594938298603-c8148c4dae35?w=800&q=80")
                    .images("[\"https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=800&q=80\",\"https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=800&q=80\"]")
                    .category(catAo)
                    .stock(45)
                    .featured(true)
                    .bestseller(true)
                    .sizes("[\"S\",\"M\",\"L\",\"XL\"]")
                    .colors("[{\"name\":\"Đen\",\"hex\":\"#1a1a1a\"},{\"name\":\"Be\",\"hex\":\"#d4b896\"},{\"name\":\"Xám\",\"hex\":\"#6b7280\"}]")
                    .gender(Product.Gender.UNISEX)
                    .build());

            Product p2 = productRepository.save(Product.builder()
                    .name("Áo Thun Basic Cotton 100%")
                    .slug(generateSlug("Áo Thun Basic Cotton 100%"))
                    .price(new BigDecimal("299000"))
                    .salePrice(new BigDecimal("199000"))
                    .description("Áo thun cotton 100% mềm mại, thoáng khí. Form regular fit phù hợp mọi dáng người. Màu sắc trung tính dễ phối.")
                    .imageUrl(imgBase + "1521572163474-6864f9cf17ab?w=800&q=80")
                    .category(catAo)
                    .stock(120)
                    .featured(true)
                    .sizes("[\"XS\",\"S\",\"M\",\"L\",\"XL\",\"XXL\"]")
                    .colors("[{\"name\":\"Trắng\",\"hex\":\"#ffffff\"},{\"name\":\"Đen\",\"hex\":\"#1a1a1a\"},{\"name\":\"Xám\",\"hex\":\"#6b7280\"},{\"name\":\"Navy\",\"hex\":\"#1e3a5f\"}]")
                    .gender(Product.Gender.UNISEX)
                    .build());

            Product p3 = productRepository.save(Product.builder()
                    .name("Áo Sơ Mi Lụa Công Sở")
                    .slug(generateSlug("Áo Sơ Mi Lụa Công Sở"))
                    .price(new BigDecimal("590000"))
                    .description("Áo sơ mi chất lụa mềm mại, không nhăn. Thiết kế tinh tế phù hợp phong cách công sở.")
                    .imageUrl(imgBase + "1596755094514-f87e34085b2c?w=800&q=80")
                    .category(catAo)
                    .stock(65)
                    .isNew(true)
                    .sizes("[\"S\",\"M\",\"L\",\"XL\"]")
                    .colors("[{\"name\":\"Trắng\",\"hex\":\"#ffffff\"},{\"name\":\"Hồng nhạt\",\"hex\":\"#f9a8d4\"},{\"name\":\"Xanh nhạt\",\"hex\":\"#93c5fd\"}]")
                    .gender(Product.Gender.FEMALE)
                    .build());

            Product p4 = productRepository.save(Product.builder()
                    .name("Áo Polo Phong Cách Sporty")
                    .slug(generateSlug("Áo Polo Phong Cách Sporty"))
                    .price(new BigDecimal("450000"))
                    .description("Áo polo phong cách sporty, chất liệu pique cotton thoáng mát. Phù hợp cả đi chơi lẫn mặc nhà.")
                    .imageUrl(imgBase + "1576566588024-1a90f2a640e8?w=800&q=80")
                    .category(catAo)
                    .stock(80)
                    .sizes("[\"S\",\"M\",\"L\",\"XL\"]")
                    .colors("[{\"name\":\"Xanh lá\",\"hex\":\"#22c55e\"},{\"name\":\"Đỏ\",\"hex\":\"#ef4444\"},{\"name\":\"Xanh dương\",\"hex\":\"#3b82f6\"}]")
                    .gender(Product.Gender.MALE)
                    .build());

            Product p5 = productRepository.save(Product.builder()
                    .name("Áo Croptop Phối Ren")
                    .slug(generateSlug("Áo Croptop Phối Ren"))
                    .price(new BigDecimal("350000"))
                    .salePrice(new BigDecimal("250000"))
                    .description("Áo croptop thiết kế phối ren tinh tế, form ngắn trendy. Phong cách năng động, trẻ trung.")
                    .imageUrl(imgBase + "1515886657613-9f3515b0c78f?w=800&q=80")
                    .category(catAo)
                    .stock(55)
                    .isNew(true)
                    .sizes("[\"XS\",\"S\",\"M\",\"L\"]")
                    .colors("[{\"name\":\"Trắng\",\"hex\":\"#ffffff\"},{\"name\":\"Đen\",\"hex\":\"#1a1a1a\"}]")
                    .gender(Product.Gender.FEMALE)
                    .build());

            // Váy
            Product p6 = productRepository.save(Product.builder()
                    .name("Váy Midi Hoa Nhí Vintage")
                    .slug(generateSlug("Váy Midi Hoa Nhí Vintage"))
                    .price(new BigDecimal("890000"))
                    .description("Váy midi họa tiết hoa nhí phong cách vintage, chất vải chiffon mềm thoáng. Đai eo co giãn, túi hai bên.")
                    .imageUrl(imgBase + "1572804013309-59a88b7e92f1?w=800&q=80")
                    .category(catVay)
                    .stock(32)
                    .featured(true)
                    .isNew(true)
                    .sizes("[\"XS\",\"S\",\"M\",\"L\"]")
                    .colors("[{\"name\":\"Hồng phấn\",\"hex\":\"#f9a8d4\"},{\"name\":\"Xanh mint\",\"hex\":\"#6ee7b7\"},{\"name\":\"Vàng nhạt\",\"hex\":\"#fde68a\"}]")
                    .gender(Product.Gender.FEMALE)
                    .build());

            Product p7 = productRepository.save(Product.builder()
                    .name("Váy Maxi Đi Biển")
                    .slug(generateSlug("Váy Maxi Đi Biển"))
                    .price(new BigDecimal("750000"))
                    .description("Váy maxi dài thoáng mát, hoàn hảo cho mùa hè và đi biển. Chất liệu voan nhẹ.")
                    .imageUrl(imgBase + "1490481651871-ab68de25d43d?w=800&q=80")
                    .category(catVay)
                    .stock(28)
                    .sizes("[\"S\",\"M\",\"L\"]")
                    .colors("[{\"name\":\"Trắng\",\"hex\":\"#ffffff\"},{\"name\":\"Xanh biển\",\"hex\":\"#0ea5e9\"}]")
                    .gender(Product.Gender.FEMALE)
                    .build());

            Product p8 = productRepository.save(Product.builder()
                    .name("Váy Chữ A Công Sở")
                    .slug(generateSlug("Váy Chữ A Công Sở"))
                    .price(new BigDecimal("650000"))
                    .description("Váy chữ A dáng công sở thanh lịch, không quá cứng nhắc. Phối đồ dễ dàng với áo sơ mi hoặc blazer.")
                    .imageUrl(imgBase + "1583496661160-fb5218afa9a0?w=800&q=80")
                    .category(catVay)
                    .stock(40)
                    .featured(true)
                    .sizes("[\"S\",\"M\",\"L\",\"XL\"]")
                    .colors("[{\"name\":\"Đen\",\"hex\":\"#1a1a1a\"},{\"name\":\"Navy\",\"hex\":\"#1e3a5f\"},{\"name\":\"Xám\",\"hex\":\"#6b7280\"}]")
                    .gender(Product.Gender.FEMALE)
                    .build());

            // Quần
            Product p9 = productRepository.save(Product.builder()
                    .name("Quần Palazzo Ống Rộng")
                    .slug(generateSlug("Quần Palazzo Ống Rộng"))
                    .price(new BigDecimal("750000"))
                    .description("Quần ống rộng thanh lịch, chất liệu linen-cottons êm mát. High-waist giúp tôn dáng.")
                    .imageUrl(imgBase + "1506629082955-511b1aa562c8?w=800&q=80")
                    .category(catQuan)
                    .stock(58)
                    .featured(true)
                    .bestseller(true)
                    .sizes("[\"S\",\"M\",\"L\",\"XL\"]")
                    .colors("[{\"name\":\"Trắng\",\"hex\":\"#ffffff\"},{\"name\":\"Be\",\"hex\":\"#d4b896\"},{\"name\":\"Đen\",\"hex\":\"#1a1a1a\"}]")
                    .gender(Product.Gender.FEMALE)
                    .build());

            Product p10 = productRepository.save(Product.builder()
                    .name("Quần Jean Slim Fit")
                    .slug(generateSlug("Quần Jean Slim Fit"))
                    .price(new BigDecimal("650000"))
                    .description("Quần jean slim fit co giãn nhẹ, thoải mái vận động. Wash nhẹ hiện đại, không quá bóng.")
                    .imageUrl(imgBase + "1594633312681-425c7b97ccd1?w=800&q=80")
                    .category(catQuan)
                    .stock(55)
                    .sizes("[\"28\",\"29\",\"30\",\"31\",\"32\",\"33\",\"34\"]")
                    .colors("[{\"name\":\"Xanh nhạt\",\"hex\":\"#93c5fd\"},{\"name\":\"Xanh đậm\",\"hex\":\"#1e3a5f\"}]")
                    .gender(Product.Gender.UNISEX)
                    .build());

            Product p11 = productRepository.save(Product.builder()
                    .name("Quần Short Tuyết")
                    .slug(generateSlug("Quần Short Tuyết"))
                    .price(new BigDecimal("380000"))
                    .salePrice(new BigDecimal("290000"))
                    .description("Quần short Bermuda chất tuyết mát lạnh, dáng rộng vừa phải. Phong cách nghỉ mát sang trọng.")
                    .imageUrl(imgBase + "1551488831-00ddcb6c6bd7?w=800&q=80")
                    .category(catQuan)
                    .stock(70)
                    .sizes("[\"S\",\"M\",\"L\",\"XL\"]")
                    .colors("[{\"name\":\"Trắng\",\"hex\":\"#ffffff\"},{\"name\":\"Be\",\"hex\":\"#d4b896\"},{\"name\":\"Đen\",\"hex\":\"#1a1a1a\"}]")
                    .gender(Product.Gender.UNISEX)
                    .build());

            Product p12 = productRepository.save(Product.builder()
                    .name("Quần Tây Slim Nam")
                    .slug(generateSlug("Quần Tây Slim Nam"))
                    .price(new BigDecimal("550000"))
                    .description("Quần tây nam dáng slim hiện đại, chất liệu wool blend co giãn. Phù hợp phong cách công sở.")
                    .imageUrl(imgBase + "1509631179647-0177331693ae?w=800&q=80")
                    .category(catQuan)
                    .stock(48)
                    .sizes("[\"28\",\"29\",\"30\",\"31\",\"32\",\"33\",\"34\",\"36\"]")
                    .colors("[{\"name\":\"Đen\",\"hex\":\"#1a1a1a\"},{\"name\":\"Xám đậm\",\"hex\":\"#374151\"},{\"name\":\"Navy\",\"hex\":\"#1e3a5f\"}]")
                    .gender(Product.Gender.MALE)
                    .build());

            // Túi xách
            Product p13 = productRepository.save(Product.builder()
                    .name("Túi Xách Mini Đeo Chéo")
                    .slug(generateSlug("Túi Xách Mini Đeo Chéo"))
                    .price(new BigDecimal("1290000"))
                    .description("Túi mini đeo chéo tiện dụng, thiết kế sang trọng. Chất da tổng hợp cao cấp, nhiều ngăn.")
                    .imageUrl(imgBase + "1548036328-c9fa89d128fa?w=800&q=80")
                    .category(catTui)
                    .stock(23)
                    .bestseller(true)
                    .featured(true)
                    .colors("[{\"name\":\"Đen\",\"hex\":\"#1a1a1a\"},{\"name\":\"Nâu\",\"hex\":\"#92400e\"},{\"name\":\"Đỏ\",\"hex\":\"#b91c1c\"}]")
                    .gender(Product.Gender.FEMALE)
                    .build());

            Product p14 = productRepository.save(Product.builder()
                    .name("Túi Đeo Vai Canvas")
                    .slug(generateSlug("Túi Đeo Vai Canvas"))
                    .price(new BigDecimal("890000"))
                    .description("Túi đeo vai chất canvas bền đẹp, phong cách casual. Ngăn rộng đựng laptop 14 inch.")
                    .imageUrl(imgBase + "1553062407-98eeb64c6a62?w=800&q=80")
                    .category(catTui)
                    .stock(30)
                    .isNew(true)
                    .colors("[{\"name\":\"Be\",\"hex\":\"#d4b896\"},{\"name\":\"Xám\",\"hex\":\"#6b7280\"},{\"name\":\"Đen\",\"hex\":\"#1a1a1a\"}]")
                    .gender(Product.Gender.UNISEX)
                    .build());

            Product p15 = productRepository.save(Product.builder()
                    .name("Ba Lô Laptop Minimalist")
                    .slug(generateSlug("Ba Lô Laptop Minimalist"))
                    .price(new BigDecimal("1590000"))
                    .description("Ba lô minimalist chống nước, ngăn laptop 15 inch. Phong cách tối giản Hàn Quốc.")
                    .imageUrl(imgBase + "1553062407-98eeb64c6a62?w=800&q=80")
                    .category(catTui)
                    .stock(20)
                    .sizes("[\"ONE SIZE\"]")
                    .colors("[{\"name\":\"Đen\",\"hex\":\"#1a1a1a\"},{\"name\":\"Xám\",\"hex\":\"#6b7280\"}]")
                    .gender(Product.Gender.UNISEX)
                    .build());

            // Giày
            Product p16 = productRepository.save(Product.builder()
                    .name("Giày Sandal Quai Ngang Minimalist")
                    .slug(generateSlug("Giày Sandal Quai Ngang Minimalist"))
                    .price(new BigDecimal("590000"))
                    .description("Sandal quai ngang minimalist, đế EVA êm chân. Phong cách tối giản dễ phối đồ.")
                    .imageUrl(imgBase + "1542291026-7eec264c27ff?w=800&q=80")
                    .category(catGiay)
                    .stock(40)
                    .sizes("[\"36\",\"37\",\"38\",\"39\",\"40\",\"41\"]")
                    .colors("[{\"name\":\"Đen\",\"hex\":\"#1a1a1a\"},{\"name\":\"Trắng\",\"hex\":\"#ffffff\"},{\"name\":\"Nude\",\"hex\":\"#d4b896\"}]")
                    .gender(Product.Gender.UNISEX)
                    .build());

            Product p17 = productRepository.save(Product.builder()
                    .name("Giày Thể Thao SneakerChunky")
                    .slug(generateSlug("Giày Thể Thao SneakerChunky"))
                    .price(new BigDecimal("1350000"))
                    .salePrice(new BigDecimal("999000"))
                    .description("Sneaker chunky đế cao trendy, phong cách Y2K đang hot. Êm chân, nhẹ, dễ phối.")
                    .imageUrl(imgBase + "1542291026-7eec264c27ff?w=800&q=80")
                    .category(catGiay)
                    .stock(35)
                    .featured(true)
                    .bestseller(true)
                    .sizes("[\"36\",\"37\",\"38\",\"39\",\"40\"]")
                    .colors("[{\"name\":\"Trắng\",\"hex\":\"#ffffff\"},{\"name\":\"Đen\",\"hex\":\"#1a1a1a\"},{\"name\":\"Be\",\"hex\":\"#d4b896\"}]")
                    .gender(Product.Gender.FEMALE)
                    .build());

            Product p18 = productRepository.save(Product.builder()
                    .name("Giày Cao Gót Mũi Nhọn")
                    .slug(generateSlug("Giày Cao Gót Mũi Nhọn"))
                    .price(new BigDecimal("850000"))
                    .description("Giày cao gót mũi nhọn thiết kế thanh lịch, đế 7cm vừa phải. Phong cách công sở sang trọng.")
                    .imageUrl(imgBase + "1515347619252-60a6bf4fff4f?w=800&q=80")
                    .category(catGiay)
                    .stock(25)
                    .sizes("[\"35\",\"36\",\"37\",\"38\",\"39\"]")
                    .colors("[{\"name\":\"Đen\",\"hex\":\"#1a1a1a\"},{\"name\":\"Nude\",\"hex\":\"#d4b896\"},{\"name\":\"Đỏ\",\"hex\":\"#b91c1c\"}]")
                    .gender(Product.Gender.FEMALE)
                    .build());

            // Phụ kiện
            Product p19 = productRepository.save(Product.builder()
                    .name("Ví Da Bò Nam Cao Cấp")
                    .slug(generateSlug("Ví Da Bò Nam Cao Cấp"))
                    .price(new BigDecimal("450000"))
                    .description("Ví da bò thật, nhiều ngăn thẻ và tiền. Dáng gập nhỏ gọn, sang trọng.")
                    .imageUrl(imgBase + "1611923134239-b9be5b4d1b2b?w=800&q=80")
                    .category(catPhuKien)
                    .stock(30)
                    .sizes("[\"ONE SIZE\"]")
                    .colors("[{\"name\":\"Nâu đậm\",\"hex\":\"#78350f\"},{\"name\":\"Đen\",\"hex\":\"#1a1a1a\"}]")
                    .gender(Product.Gender.MALE)
                    .build());

            Product p20 = productRepository.save(Product.builder()
                    .name("Thắt Lưng Da Nam")
                    .slug(generateSlug("Thắt Lưng Da Nam"))
                    .price(new BigDecimal("350000"))
                    .description("Thắt lưng da bò chính hãng, khóa kim loại sang trọng. Phong cách công sở lịch lãm.")
                    .imageUrl(imgBase + "1624222247344-550fb60583dc?w=800&q=80")
                    .category(catPhuKien)
                    .stock(40)
                    .sizes("[\"38\",\"40\",\"42\",\"44\"]")
                    .colors("[{\"name\":\"Đen\",\"hex\":\"#1a1a1a\"},{\"name\":\"Nâu\",\"hex\":\"#78350f\"}]")
                    .gender(Product.Gender.MALE)
                    .build());

            Product p21 = productRepository.save(Product.builder()
                    .name("Kính Mát Oversized")
                    .slug(generateSlug("Kính Mát Oversized"))
                    .price(new BigDecimal("550000"))
                    .description("Kính mát oversized phong cách retro, gọng nhựa bền nhẹ. Chống tia UV 400.")
                    .imageUrl(imgBase + "1577803945446-47f1ad97c5fe?w=800&q=80")
                    .category(catPhuKien)
                    .stock(35)
                    .sizes("[\"ONE SIZE\"]")
                    .colors("[{\"name\":\"Đen\",\"hex\":\"#1a1a1a\"},{\"name\":\"Nâu\",\"hex\":\"#78350f\"}]")
                    .gender(Product.Gender.UNISEX)
                    .build());

            Product p22 = productRepository.save(Product.builder()
                    .name("Bông Tai Vàng Ngọc Trai")
                    .slug(generateSlug("Bông Tai Vàng Ngọc Trai"))
                    .price(new BigDecimal("280000"))
                    .description("Bông tai mạ vàng họa tiết ngọc trai, thiết kế nhẹ nhàng nữ tính.")
                    .imageUrl(imgBase + "1515562141207-7a88fb7ce338?w=800&q=80")
                    .category(catPhuKien)
                    .stock(50)
                    .isNew(true)
                    .sizes("[\"ONE SIZE\"]")
                    .colors("[{\"name\":\"Vàng\",\"hex\":\"#eab308\"},{\"name\":\"Bạc\",\"hex\":\"#d1d5db\"}]")
                    .gender(Product.Gender.FEMALE)
                    .build());

            // ===================== VOUCHERS =====================
            Instant now = Instant.now();
            Instant day30 = now.plus(30, ChronoUnit.DAYS);
            Instant day15 = now.plus(15, ChronoUnit.DAYS);

            voucherRepository.save(Voucher.builder()
                    .code("WELCOME10")
                    .description("Giảm 10% cho đơn hàng đầu tiên")
                    .discountType(Voucher.DiscountType.PERCENT)
                    .discountValue(new BigDecimal("10"))
                    .maxDiscount(new BigDecimal("100000"))
                    .minOrderValue(new BigDecimal("200000"))
                    .startDate(now)
                    .endDate(day30)
                    .usageLimit(100)
                    .usageLimitPerUser(1)
                    .active(true)
                    .build());

            voucherRepository.save(Voucher.builder()
                    .code("SUMMER20")
                    .description("Giảm 20% mùa hè - Không giới hạn")
                    .discountType(Voucher.DiscountType.PERCENT)
                    .discountValue(new BigDecimal("20"))
                    .maxDiscount(new BigDecimal("300000"))
                    .minOrderValue(new BigDecimal("500000"))
                    .startDate(now)
                    .endDate(day30)
                    .usageLimit(null)
                    .active(true)
                    .build());

            voucherRepository.save(Voucher.builder()
                    .code("FREESHIP")
                    .description("Miễn phí vận chuyển cho đơn từ 300K")
                    .discountType(Voucher.DiscountType.FIXED)
                    .discountValue(new BigDecimal("30000"))
                    .minOrderValue(new BigDecimal("300000"))
                    .startDate(now)
                    .endDate(day15)
                    .usageLimit(200)
                    .active(true)
                    .build());

            voucherRepository.save(Voucher.builder()
                    .code("VIP500K")
                    .description("Giảm 500K cho đơn từ 2 triệu - Khách VIP")
                    .discountType(Voucher.DiscountType.FIXED)
                    .discountValue(new BigDecimal("500000"))
                    .minOrderValue(new BigDecimal("2000000"))
                    .startDate(now)
                    .endDate(day30)
                    .usageLimit(50)
                    .active(true)
                    .build());

            // ===================== FLASH SALES =====================
            Instant flashEnd = now.plus(3, ChronoUnit.DAYS);
            FlashSale flash = flashSaleRepository.save(FlashSale.builder()
                    .name("Flash Sale Cuối Tuần")
                    .startTime(now)
                    .endTime(flashEnd)
                    .discountPercent(30)
                    .active(true)
                    .build());

            flashSaleRepository.save(flash); // save again to get ID

            // ===================== SUBSCRIBERS =====================
            subscriberRepository.save(Subscriber.builder()
                    .email("lan.nguyen@gmail.com")
                    .active(true)
                    .build());
            subscriberRepository.save(Subscriber.builder()
                    .email("nam.tran@yahoo.com")
                    .active(true)
                    .build());
            subscriberRepository.save(Subscriber.builder()
                    .email("hong.le@gmail.com")
                    .active(true)
                    .build());

            // ===================== ORDERS =====================
            Instant day3 = now.minus(3, ChronoUnit.DAYS);
            Instant day7 = now.minus(7, ChronoUnit.DAYS);
            Instant orderDay15 = now.minus(15, ChronoUnit.DAYS);
            Instant day20 = now.minus(20, ChronoUnit.DAYS);

            Order order1 = orderRepository.save(Order.builder()
                    .orderCode(generateOrderCode())
                    .user(customer1)
                    .totalAmount(new BigDecimal("2780000"))
                    .status(Order.OrderStatus.DELIVERED)
                    .orderDate(orderDay15)
                    .recipientName("Nguyễn Thị Lan")
                    .address("123 Đường Lê Lợi, Quận 1, TP.HCM")
                    .phone("0901234567")
                    .paymentMethod("COD")
                    .build());
            order1.getOrderDetails().add(OrderDetail.builder().order(order1).product(p1).quantity(1).price(p1.getPrice()).build());
            order1.getOrderDetails().add(OrderDetail.builder().order(order1).product(p6).quantity(1).price(p6.getPrice()).build());
            orderRepository.save(order1);

            Order order2 = orderRepository.save(Order.builder()
                    .orderCode(generateOrderCode())
                    .user(customer2)
                    .totalAmount(new BigDecimal("1880000"))
                    .status(Order.OrderStatus.SHIPPED)
                    .orderDate(day7)
                    .recipientName("Trần Văn Nam")
                    .address("45 Đường Nguyễn Huệ, Quận 1, TP.HCM")
                    .phone("0912345678")
                    .paymentMethod("VNPAY")
                    .trackingNumber("VJ123456789")
                    .carrier("ViettelPost")
                    .build());
            order2.getOrderDetails().add(OrderDetail.builder().order(order2).product(p13).quantity(1).price(p13.getPrice()).build());
            order2.getOrderDetails().add(OrderDetail.builder().order(order2).product(p2).quantity(2).price(p2.getSalePrice()).build());
            orderRepository.save(order2);

            Order order3 = orderRepository.save(Order.builder()
                    .orderCode(generateOrderCode())
                    .user(customer1)
                    .totalAmount(new BigDecimal("590000"))
                    .status(Order.OrderStatus.CONFIRMED)
                    .orderDate(day3)
                    .recipientName("Nguyễn Thị Lan")
                    .address("123 Đường Lê Lợi, Quận 1, TP.HCM")
                    .phone("0901234567")
                    .paymentMethod("COD")
                    .build());
            order3.getOrderDetails().add(OrderDetail.builder().order(order3).product(p16).quantity(1).price(p16.getPrice()).build());
            orderRepository.save(order3);

            Order order4 = orderRepository.save(Order.builder()
                    .orderCode(generateOrderCode())
                    .user(customer3)
                    .totalAmount(new BigDecimal("940000"))
                    .status(Order.OrderStatus.DELIVERED)
                    .orderDate(day7)
                    .recipientName("Lê Thị Hồng")
                    .address("78 Đường Pasteur, Quận 3, TP.HCM")
                    .phone("0933123456")
                    .paymentMethod("COD")
                    .build());
            order4.getOrderDetails().add(OrderDetail.builder().order(order4).product(p9).quantity(1).price(p9.getPrice()).build());
            order4.getOrderDetails().add(OrderDetail.builder().order(order4).product(p19).quantity(1).price(p19.getPrice()).build());
            orderRepository.save(order4);

            Order order5 = orderRepository.save(Order.builder()
                    .orderCode(generateOrderCode())
                    .user(customer2)
                    .totalAmount(new BigDecimal("1890000"))
                    .status(Order.OrderStatus.PENDING)
                    .orderDate(day3)
                    .recipientName("Trần Văn Nam")
                    .address("45 Đường Nguyễn Huệ, Quận 1, TP.HCM")
                    .phone("0912345678")
                    .paymentMethod("VNPAY")
                    .build());
            order5.getOrderDetails().add(OrderDetail.builder().order(order5).product(p17).quantity(1).price(p17.getSalePrice()).build());
            orderRepository.save(order5);

            Order order6 = orderRepository.save(Order.builder()
                    .orderCode(generateOrderCode())
                    .user(customer3)
                    .totalAmount(new BigDecimal("1350000"))
                    .status(Order.OrderStatus.PROCESSING)
                    .orderDate(day20)
                    .recipientName("Lê Thị Hồng")
                    .address("78 Đường Pasteur, Quận 3, TP.HCM")
                    .phone("0933123456")
                    .paymentMethod("COD")
                    .build());
            order6.getOrderDetails().add(OrderDetail.builder().order(order6).product(p3).quantity(1).price(p3.getPrice()).build());
            order6.getOrderDetails().add(OrderDetail.builder().order(order6).product(p11).quantity(1).price(p11.getSalePrice()).build());
            orderRepository.save(order6);

            // ===================== REVIEWS =====================
            reviewRepository.save(Review.builder().product(p1).user(customer1).rating(5).comment("Áo blazer đẹp vải tốt, mặc lên rất sang.").approved(true).build());
            reviewRepository.save(Review.builder().product(p1).user(customer2).rating(4).comment("Mặc vừa, giao hàng nhanh. Đóng gói cẩn thận.").approved(true).build());
            reviewRepository.save(Review.builder().product(p6).user(customer1).rating(5).comment("Váy rất đẹp, đúng hình, chất vải mềm.").approved(true).build());
            reviewRepository.save(Review.builder().product(p9).user(customer3).rating(4).comment("Quần ống rộng thoải mái, tôn dáng tốt.").approved(true).build());
            reviewRepository.save(Review.builder().product(p3).user(customer3).rating(4).comment("Áo sơ mi đẹp, lụa mềm không nhăn.").approved(true).build());
            reviewRepository.save(Review.builder().product(p13).user(customer1).rating(5).comment("Túi xinh, chất lượng tốt, đúng mẫu.").approved(true).build());
            reviewRepository.save(Review.builder().product(p2).user(customer2).rating(5).comment("Áo basic mặc rất thoải mái, cotton mềm.").approved(true).build());
            reviewRepository.save(Review.builder().product(p16).user(customer3).rating(4).comment("Sandal đẹp, đế êm chân, giao nhanh.").approved(true).build());
            reviewRepository.save(Review.builder().product(p17).user(customer1).rating(5).comment("Sneaker chunky xinh lắm, giảm giá nên mua được hời.").approved(true).build());
            reviewRepository.save(Review.builder().product(p19).user(customer2).rating(5).comment("Ví da đẹp, nhiều ngăn tiện dụng.").approved(true).build());

            log.info("Seed: done. Users={}, Categories={}, Products={}, Orders={}, Reviews={}, Vouchers={}, Subscribers={}, Banners={}",
                    userRepository.count(), categoryRepository.count(), productRepository.count(),
                    orderRepository.count(), reviewRepository.count(), voucherRepository.count(),
                    subscriberRepository.count(), bannerRepository.count());

        } catch (Exception e) {
            log.error("Seed: failed to insert data", e);
            throw e;
        }
    }
}
