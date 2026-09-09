# Cấu hình thời gian, phiên đăng nhập và phân quyền RBAC

Tài liệu này mô tả cấu hình mới của SparkMinds Library sau khi chuẩn hóa mọi
thời gian sống về **đơn vị giây**, tách credentials sang `.env`, bổ sung
`SUPER_ADMIN` và chuyển từ kiểm tra role cứng sang permission/action.

## 1. Cấu hình thời gian tập trung

Mọi TTL nằm tại một khối duy nhất trong `application.yml`:

```yaml
app:
  time-to-live:
    access-token-seconds: ${ACCESS_TOKEN_SECONDS:900}
    refresh-token-seconds: ${REFRESH_TOKEN_SECONDS:604800}
    email-verification-seconds: ${EMAIL_VERIFICATION_SECONDS:86400}
    password-reset-seconds: ${PASSWORD_RESET_SECONDS:1800}
    email-change-verification-seconds: ${EMAIL_CHANGE_VERIFICATION_SECONDS:600}
    social-login-code-seconds: ${SOCIAL_LOGIN_CODE_SECONDS:120}
    borrowing-seconds: ${BORROWING_SECONDS:1209600}
```

| Thuộc tính | Mặc định (giây) | Tương đương |
|---|---:|---:|
| Access token | 900 | 15 phút |
| Refresh token | 604800 | 7 ngày |
| Xác minh email đăng ký | 86400 | 24 giờ |
| Đặt lại mật khẩu | 1800 | 30 phút |
| Mã đổi email | 600 | 10 phút |
| Mã đăng nhập Google một lần | 120 | 2 phút |
| Thời hạn mượn sách | 1209600 | 14 ngày |

Code Java bind khối trên bằng `TimeToLiveProperties`. Service chỉ dùng
`Duration` được tạo từ số giây, không còn gọi trực tiếp `plusDays`,
`plusHours` hay `plusMinutes`.

Muốn thử access token chỉ sống 60 giây:

```powershell
$env:ACCESS_TOKEN_SECONDS = "60"
.\mvnw.cmd spring-boot:run
```

Biến môi trường có độ ưu tiên cao hơn giá trị mặc định trong `application.yml`.

## 2. Credentials trong `.env`

`application.yml` import file `.env` ở thư mục gốc:

```yaml
spring:
  config:
    import: optional:file:.env[.properties]
```

Thiết lập lần đầu:

```powershell
Copy-Item .env.example .env
notepad .env
```

Các giá trị bắt buộc cần kiểm tra:

```properties
DB_URL=jdbc:postgresql://127.0.0.1:5433/library_db
DB_USERNAME=library_user
DB_PASSWORD=your-database-password
ADMIN_USERNAME=admin
ADMIN_PASSWORD=your-admin-password
ADMIN_EMAIL=admin@example.com
JWT_SECRET=your-base64-secret
```

Tạo JWT secret an toàn trong PowerShell:

```powershell
[Convert]::ToBase64String(
    [Security.Cryptography.RandomNumberGenerator]::GetBytes(48)
)
```

`.env` đã nằm trong `.gitignore`. Chỉ `.env.example` được phép commit và file
mẫu không được chứa secret thật. `compose.yaml` cũng đọc thông tin PostgreSQL
từ `.env`, không còn hard-code password.

## 3. Role được triển khai

- `SUPER_ADMIN`: quyền cao nhất, quản lý role/permission và có toàn bộ quyền.
- `ADMIN`: vận hành thư viện: sách, thành viên, lịch sử mượn/trả, cấu hình.
- `USER`: tra cứu, lưu, mượn/trả sách của chính mình và cập nhật hồ sơ.

Tài khoản bootstrap `admin/admin` trong môi trường demo nhận cả `ADMIN` và
`SUPER_ADMIN` để tương thích giao diện cũ. Production phải đổi mật khẩu này
trong `.env` trước lần khởi tạo database.

Hệ thống chủ động giữ đúng ba role trên. Khi cần phạm vi khác nhau, Super Admin
bật/tắt permission trực tiếp trên từng user thay vì tạo thêm role mới.

## 4. Permission/action được triển khai

Permission có cấu trúc:

```text
NAME = RESOURCE_ACTION
Ví dụ: BOOK_CREATE = resource BOOK + action CREATE
```

Các nhóm hiện có:

- Sách: `BOOK_READ`, `BOOK_CREATE`, `BOOK_UPDATE`, `BOOK_DELETE`,
  `BOOK_IMPORT`.
- Thành viên: `MEMBER_READ`, `MEMBER_CREATE`, `MEMBER_UPDATE`,
  `MEMBER_DELETE`.
- Mượn/trả: `BORROWING_BORROW`, `BORROWING_RETURN_OWN`,
  `BORROWING_RETURN_ANY`, `BORROWING_READ_OWN`, `BORROWING_READ_ALL`.
- Hồ sơ: `PROFILE_READ`, `PROFILE_UPDATE`.
- Sách đã lưu: `SAVED_BOOK_READ`, `SAVED_BOOK_WRITE`.
- Cấu hình: `SYSTEM_CONFIG_READ`, `SYSTEM_CONFIG_UPDATE`.
- Quản trị phân quyền: `ACCESS_CONTROL_MANAGE`.

Quyền hiệu lực của một user được tính theo thứ tự:

```text
effective permissions
  = permissions của tất cả roles
  + permissions gán trực tiếp cho user
  - permissions bị tắt riêng cho user
```

Danh sách tắt riêng có độ ưu tiên cao nhất. Vì vậy Super Admin có thể tắt
`BOOK_READ` dù quyền đó vốn thuộc role `USER`, hoặc bật riêng `BOOK_IMPORT`
mà không cần biến user thành Admin.

## 5. API dành cho Super Admin

Tất cả API dưới đây yêu cầu `ACCESS_CONTROL_MANAGE`:

| Method | API | Chức năng |
|---|---|---|
| GET | `/api/super-admin/access-control/permissions` | Danh sách permission |
| GET | `/api/super-admin/access-control/roles` | Role và permission hiện tại |
| PUT | `/api/super-admin/access-control/roles/{role}/permissions` | Thay toàn bộ quyền của role |
| GET | `/api/super-admin/access-control/users/{userId}` | Xem quyền user |
| PUT | `/api/super-admin/access-control/users/{userId}/permissions` | Gán quyền trực tiếp |
| PATCH | `/api/super-admin/access-control/users/{userId}/permissions/{permission}` | Bật/tắt một quyền |
| PUT | `/api/super-admin/access-control/users/{userId}/roles` | Gán role |

Ví dụ tắt quyền đọc sách của user:

```json
{
  "enabled": false
}
```

Gửi body trên tới:

```text
PATCH /api/super-admin/access-control/users/{userId}/permissions/BOOK_READ
```

Hệ thống ngăn xóa `ACCESS_CONTROL_MANAGE` khỏi `SUPER_ADMIN` và ngăn hạ role
của Super Admin cuối cùng, tránh tự khóa toàn bộ chức năng quản trị.

## 6. Flow giữ phiên đăng nhập

### 6.1 Đăng nhập

1. Browser gửi username/email và password tới `POST /api/auth/login`.
2. Spring Security đọc user, kiểm tra BCrypt password, trạng thái
   `enabled`, `emailVerified` và `accountNonLocked`.
3. `CustomUserPrincipal` hợp nhất role, permission của role và permission gán
   trực tiếp.
4. Server phát access token JWT. JWT chứa `uid`, `email`, `roles`,
   `permissions`, `authorities`, `iat`, `exp` và `jti`.
5. Server tạo refresh token ngẫu nhiên. Browser nhận token thô; database chỉ
   lưu SHA-256 hash, thời điểm hết hạn và trạng thái revoked.
6. Frontend ghi cả hai token trong một object JSON duy nhất tại
   `localStorage["sparkLibrary.authSession"]`.
7. Sự kiện `storage` và `BroadcastChannel` thông báo cho các tab còn lại để
   nhận phiên đăng nhập ngay, không cần đăng nhập lần nữa.

Ứng dụng dùng `localStorage` để đáp ứng yêu cầu phiên đăng nhập dùng chung giữa
các tab. Đây là lựa chọn phù hợp cho bản demo, nhưng token vẫn có thể bị đọc
nếu trang xảy ra XSS. Production nên chuyển refresh token sang cookie
`HttpOnly + Secure + SameSite`, giữ access token ngắn hạn trong memory.

### 6.2 Gọi API bình thường

1. Frontend đọc access token từ object phiên trong `localStorage`.
2. Token được gửi qua header `Authorization: Bearer <token>`.
3. Server kiểm tra chữ ký, issuer, thời điểm hết hạn và `jti` có nằm trong
   bảng revoked token hay không.
4. Spring Security lấy `authorities` từ JWT.
5. `@PreAuthorize` kiểm tra permission của API trước khi chạy service.

### 6.3 Access token hết hạn

1. API trả HTTP 401.
2. Hàm `api()` trên frontend phát hiện 401 và chỉ thử refresh một lần.
3. Frontend gửi refresh token tới `POST /api/auth/refresh`.
4. Server tìm SHA-256 hash trong database, kiểm tra chưa revoked, chưa hết hạn
   và user vẫn hợp lệ.
5. Refresh token cũ bị revoke; server phát access token và refresh token mới.
   Cơ chế này gọi là refresh-token rotation.
6. Frontend thay nguyên object phiên trong `localStorage` và gọi lại request
   ban đầu.
7. Nếu refresh thất bại, frontend xóa token và đưa người dùng về màn hình đăng
   nhập, không tạo vòng lặp refresh.

Nếu nhiều tab cùng gặp 401, frontend dùng Web Locks API
(`navigator.locks`) để chỉ một tab thực hiện refresh-token rotation. Tab còn
lại sử dụng cặp token mới đã được lưu, tránh việc hai tab dùng cùng refresh
token cũ và làm phiên đăng nhập bị thu hồi nhầm.

### 6.4 Khi Super Admin thay đổi quyền

Mỗi user có `authorizationVersion`; JWT mang claim `authv`. Khi Super Admin
bật/tắt permission, hệ thống tăng version của user. Access token cũ lập tức
không còn hợp lệ và trả 401. Frontend dùng refresh token lấy JWT mới với
permission mới, sau đó request bị cấm sẽ trả 403. User không cần đăng xuất thủ
công và không phải chờ access token hết hạn.

### 6.5 Logout

1. Frontend gửi access token và refresh token tới `POST /api/auth/logout`.
2. Refresh token tương ứng bị đánh dấu `revoked`.
3. `jti` của access token được ghi vào bảng revoked token tới đúng thời điểm
   JWT hết hạn.
4. Frontend luôn xóa object phiên khỏi `localStorage`, kể cả request logout gặp
   lỗi.
5. Các tab khác nhận sự kiện logout và lập tức trở về màn hình đăng nhập.
6. Dùng lại access token hoặc refresh token cũ đều bị từ chối.

### 6.6 Đồng bộ thao tác giữa nhiều tab

- Login/logout và token mới được đồng bộ bằng một object phiên duy nhất, tránh
  trạng thái chỉ ghi được access token nhưng chưa ghi refresh token.
- Các thao tác `POST`, `PUT`, `PATCH`, `DELETE` phát sự kiện `DATA_CHANGED`.
- Tab còn lại tải lại user/permission và màn hình hiện tại sau một khoảng
  debounce ngắn.
- Khi Super Admin thay đổi permission của một user đang mở ở tab khác,
  `authorizationVersion` làm JWT cũ trả 401; tab đó refresh JWT rồi render lại
  menu và nút theo permission mới.

### 6.7 Google OAuth

HTTP session tạm thời chỉ tồn tại trong quá trình redirect Google OAuth vì
`SessionCreationPolicy.IF_REQUIRED`. Sau callback thành công, server tạo mã
social dùng một lần, hủy HTTP session, redirect về frontend; frontend đổi mã
đó lấy cặp JWT/refresh token. Từ thời điểm này flow giống đăng nhập email.

## 7. Thay đổi quyền và cache token

Khuyến nghị production:

- Access token ngắn, khoảng 300–900 giây.
- Refresh token dài hơn, khoảng 604800–2592000 giây.
- Không đưa refresh token vào URL hoặc log.
- Với frontend production, ưu tiên refresh token trong cookie
  `HttpOnly + Secure + SameSite` thay vì cho JavaScript truy cập.
- Khi khóa user hoặc thay đổi quyền nhạy cảm, revoke mọi refresh token và tăng
  `tokenVersion` của user để vô hiệu hóa đồng loạt access token cũ.
