# NovaWear – API Documentation

Backend NovaWear cung cấp API docs qua **OpenAPI 3** và **Swagger UI**.

## Truy cập khi server đang chạy

| Mục | URL |
|-----|-----|
| **Swagger UI** (giao diện test API) | http://localhost:8080/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8080/v3/api-docs |
| **OpenAPI YAML** | http://localhost:8080/v3/api-docs.yaml |

## Cách dùng Swagger UI

1. Chạy backend: `./mvnw spring-boot:run` (trong thư mục `server-novawear`).
2. Mở trình duyệt: **http://localhost:8080/swagger-ui.html**.
3. **Authorize (JWT):**
   - Gọi **Auth** → **POST /api/auth/login** với body `{"username":"admin","password":"admin123"}`.
   - Copy giá trị `token` trong response.
   - Bấm nút **Authorize** (khóa ở góc phải), dán token vào ô (chỉ cần token, không cần gõ "Bearer").
   - Bấm **Authorize** rồi **Close**.
4. Các request tiếp theo sẽ tự gửi header `Authorization: Bearer <token>` khi bạn **Try it out** và **Execute**.

## Nội dung API

- **Auth** — Đăng nhập, đăng ký, lấy thông tin user.
- **Categories** — Danh mục (public).
- **Products** — Sản phẩm, tìm kiếm, nổi bật (public).
- **Cart** — Giỏ hàng (cần token).
- **Orders** — Đơn hàng, checkout (cần token).
- **Reviews** — Đánh giá sản phẩm (public GET, cần token để tạo).
- **Admin** — CRUD danh mục, sản phẩm, đơn hàng, user, review, thống kê (cần token ADMIN).

## Công nghệ

- **springdoc-openapi** (Swagger UI + OpenAPI 3).
- Cấu hình: `server-novawear/src/main/resources/application.yml` (mục `springdoc`).
- Bean mô tả API & JWT: `com.example.novawear.config.OpenApiConfig`.
