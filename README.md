# NovaWear – Backend J2EE (Spring Boot)

Backend cho đồ án môn J2EE – hệ thống thương mại điện tử **NovaWear**: REST API với Spring Boot, MySQL, JWT, quản trị và trang người dùng.

## Yêu cầu

- **Java 21**
- **Maven** (hoặc dùng `./mvnw`)
- **MySQL 8** (chạy local, port 3306)

## Cấu hình

- **MySQL**: mặc định `localhost:3306`, database `novawear`, user `root` / password `root`.
- Sửa trong `src/main/resources/application.yml` nếu cần:
  - `spring.datasource.url`, `username`, `password`
  - `app.jwt.secret` (nên đổi trong production)
  - `app.cors.allowed-origins` (thêm origin React nếu khác `http://localhost:3000`)

## Chạy ứng dụng

```bash
./mvnw spring-boot:run
```

API gốc: **http://localhost:8080**

## API Documentation (Swagger / OpenAPI)

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) — giao diện xem và gọi thử API.
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) — spec OpenAPI 3.

Trong Swagger UI, bấm **Authorize** và nhập JWT token (trả về từ `POST /api/auth/login`) để gọi các API cần đăng nhập.

## Tài khoản mặc định (dev)

Sau lần chạy đầu, có sẵn user admin:

- **Username:** `admin`
- **Password:** `admin123`

*(Đổi mật khẩu hoặc tắt seed trong production.)*

## API chính (theo PRD)

| Method | Endpoint | Mô tả | Role |
|--------|----------|--------|------|
| POST | `/api/auth/login` | Đăng nhập, trả JWT | Public |
| POST | `/api/auth/register` | Đăng ký | Public |
| GET | `/api/auth/me` | Thông tin user đăng nhập | USER/ADMIN |
| GET | `/api/categories` | Danh sách danh mục | Public |
| GET | `/api/products` | Danh sách sản phẩm (phân trang, categoryId, search) | Public |
| GET | `/api/products/featured` | Sản phẩm nổi bật | Public |
| GET | `/api/products/{id}` | Chi tiết sản phẩm | Public |
| GET | `/api/cart` | Xem giỏ hàng | USER |
| POST | `/api/cart/add` | Thêm vào giỏ | USER |
| PUT | `/api/cart/items/{productId}?quantity=` | Cập nhật số lượng | USER |
| DELETE | `/api/cart/items/{productId}` | Xóa khỏi giỏ | USER |
| POST | `/api/orders/checkout` | Thanh toán (body: items, paymentMethod) | USER |
| GET | `/api/orders` | Đơn hàng của user | USER |
| GET | `/api/reviews/product/{productId}` | Đánh giá đã duyệt của sản phẩm | Public |
| POST | `/api/reviews/product/{productId}` | Gửi đánh giá | USER |
| GET | `/api/admin/categories` | CRUD danh mục | ADMIN |
| POST/PUT/DELETE | `/api/admin/categories`, `/api/admin/categories/{id}` | | ADMIN |
| GET | `/api/admin/products/{id}` | CRUD sản phẩm | ADMIN |
| POST/PUT/DELETE | `/api/admin/products`, `/api/admin/products/{id}` | | ADMIN |
| GET | `/api/admin/orders` | Danh sách đơn (filter status) | ADMIN |
| PATCH | `/api/admin/orders/{id}/status?status=` | Cập nhật trạng thái đơn | ADMIN |
| GET | `/api/admin/users` | Danh sách khách hàng | ADMIN |
| PATCH | `/api/admin/users/{id}/active?active=` | Bật/tắt user | ADMIN |
| GET | `/api/admin/reviews` | Danh sách bình luận | ADMIN |
| PATCH | `/api/admin/reviews/{id}/approve?approved=` | Duyệt bình luận | ADMIN |
| GET | `/api/admin/stats/revenue` | Thống kê doanh thu (query: from, to) | ADMIN |

**JWT:** Gửi header `Authorization: Bearer <token>` cho các API yêu cầu đăng nhập.

## Cấu trúc dự án

- **entity** – User, Category, Product, Order, OrderDetail, Review
- **repository** – JPA
- **dto** – Request/Response
- **service** – Auth, Category, Product, Order, Review, Cart, Stats, User
- **controller** – Auth, Category, Product, Cart, Order, Review; **admin** – Category, Product, Order, User, Review, Stats
- **config** – Security (JWT), CORS, Seed admin
- **security** – JwtUtil, JwtAuthenticationFilter, UserDetailsServiceImpl
- **exception** – GlobalExceptionHandler

## Database

- Schema do **Flyway** tạo: `src/main/resources/db/migration/V1__init_schema.sql`
- JPA: `ddl-auto: validate` (không tạo/sửa bảng khi chạy)

## Test

```bash
./mvnw test
```

Profile `test` dùng H2 in-memory, không cần MySQL.

## Công nghệ

- Spring Boot 4, Spring Security (JWT), Spring Data JPA
- MySQL, Flyway
- Lombok, Validation (Jakarta)
- jjwt (JWT)
