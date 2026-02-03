# Hướng dẫn Setup Database – NovaWear J2EE

Tài liệu hướng dẫn cài đặt và cấu hình cơ sở dữ liệu MySQL cho dự án NovaWear (backend Spring Boot).

---

## 1. Yêu cầu

- **MySQL** phiên bản **8.0** trở lên (hoặc MariaDB 10.3+ tương thích).
- MySQL Server chạy và lắng nghe cổng mặc định **3306**.

---

## 2. Cài đặt MySQL

### Windows

1. Tải MySQL Installer: [https://dev.mysql.com/downloads/installer/](https://dev.mysql.com/downloads/installer/)
2. Chạy installer, chọn **MySQL Server** và **MySQL Workbench** (tùy chọn).
3. Trong bước cấu hình:
   - Chọn **Standalone**.
   - Cấu hình **root** password (ví dụ: `root`) và ghi nhớ để khai báo trong ứng dụng.
   - Port mặc định: **3306**.

### macOS (Homebrew)

```bash
brew install mysql
brew services start mysql
# Đặt mật khẩu cho root (nếu cần):
mysql_secure_installation
```

### Linux (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
sudo mysql_secure_installation
```

---

## 3. Tạo database và user (tùy chọn)

Ứng dụng có thể **tự tạo database** nhờ tham số `createDatabaseIfNotExist=true` trong URL. Nếu muốn tạo sẵn và dùng user riêng:

### 3.1. Đăng nhập MySQL

```bash
mysql -u root -p
```

### 3.2. Tạo database

```sql
CREATE DATABASE novawear
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### 3.3. Tạo user riêng (khuyến nghị cho môi trường dev/production)

```sql
CREATE USER 'novawear'@'localhost' IDENTIFIED BY 'mat_khau_cua_ban';

GRANT ALL PRIVILEGES ON novawear.* TO 'novawear'@'localhost';

FLUSH PRIVILEGES;

EXIT;
```

Sau đó cấu hình lại `application.yml` (xem mục 4) với username/password của user này.

---

## 4. Cấu hình kết nối trong ứng dụng

File cấu hình: **`server-novawear/src/main/resources/application.yml`**.

Mặc định:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/novawear?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
```

**Chỉnh sửa khi cần:**

| Mục | Mô tả | Ví dụ |
|-----|--------|--------|
| **Host / Port** | Nếu MySQL chạy ở máy/port khác | `jdbc:mysql://192.168.1.10:3307/novawear?...` |
| **Tên database** | Thay `novawear` trong URL | `novawear_dev`, `novawear_prod` |
| **username** | User MySQL | `root` hoặc `novawear` |
| **password** | Mật khẩu user | Đổi `root` thành mật khẩu thật |

**Ví dụ dùng user `novawear`:**

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/novawear?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: novawear
    password: mat_khau_cua_ban
    driver-class-name: com.mysql.cj.jdbc.Driver
```

---

## 5. Schema database (Flyway)

Ứng dụng dùng **Flyway** để tự chạy migration khi khởi động. Schema nằm tại:

**`server-novawear/src/main/resources/db/migration/V1__init_schema.sql`**

Các bảng được tạo:

| Bảng | Mô tả |
|------|--------|
| **users** | Tài khoản (admin, user): username, password, email, role, active |
| **categories** | Danh mục sản phẩm: name, description |
| **products** | Sản phẩm: name, price, description, image_url, category_id, stock |
| **orders** | Đơn hàng: user_id, total_amount, status, order_date |
| **order_details** | Chi tiết đơn: order_id, product_id, quantity, price |
| **reviews** | Đánh giá: product_id, user_id, rating, comment, approved, created_at |

**Lưu ý:**

- **JPA** được cấu hình `ddl-auto: validate` → không tạo/sửa bảng; schema do Flyway quản lý.
- Lần chạy đầu tiên Flyway sẽ chạy `V1__init_schema.sql` và tạo tất cả bảng.
- Không cần chạy file SQL thủ công trừ khi bạn muốn kiểm tra hoặc tạo DB tay.

---

## 6. Kiểm tra kết nối

### 6.1. Từ dòng lệnh MySQL

```bash
mysql -u root -p -e "SHOW DATABASES;"
# Sau khi app chạy ít nhất một lần:
mysql -u root -p novawear -e "SHOW TABLES;"
```

### 6.2. Từ ứng dụng Spring Boot

1. Chạy backend:
   ```bash
   cd server-novawear
   ./mvnw spring-boot:run
   ```
2. Nếu log không báo lỗi kết nối và có dòng dạng `Started NovawearApplication` → kết nối database thành công.
3. Có thể gọi health (nếu bật actuator):  
   `GET http://localhost:8080/actuator/health`

---

## 7. Xử lý lỗi thường gặp

| Lỗi | Nguyên nhân | Cách xử lý |
|-----|-------------|------------|
| **Access denied for user 'root'@'localhost'** | Sai mật khẩu hoặc user chưa được cấp quyền | Kiểm tra lại `username`/`password` trong `application.yml`; đổi mật khẩu root hoặc tạo user mới và cấp quyền trên DB `novawear`. |
| **Unknown database 'novawear'** | Database chưa tồn tại và URL không có `createDatabaseIfNotExist=true` | Thêm `createDatabaseIfNotExist=true` vào URL hoặc tạo database thủ công (mục 3.2). |
| **Communications link failure** | MySQL chưa chạy hoặc sai host/port | Khởi động MySQL; kiểm tra host (localhost) và port (3306) trong URL. |
| **Public Key Retrieval is not allowed** | MySQL 8 yêu cầu xác thực và client chưa lấy public key | Thêm `allowPublicKeyRetrieval=true` vào URL (đã có trong cấu hình mặc định). |

---

## 8. Tóm tắt nhanh

1. Cài **MySQL 8** và khởi động service.
2. (Tùy chọn) Tạo database `novawear` và user riêng.
3. Chỉnh **`server-novawear/src/main/resources/application.yml`** cho đúng host, port, database, username, password.
4. Chạy **`./mvnw spring-boot:run`** trong thư mục `server-novawear` → Flyway tạo bảng, ứng dụng kết nối và chạy bình thường.

Nếu cần thay đổi schema (thêm bảng, cột), thêm file migration mới trong `db/migration` (ví dụ `V2__ten_migration.sql`) theo quy tắc đặt tên của Flyway.
