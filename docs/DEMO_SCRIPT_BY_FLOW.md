# Kịch bản demo SparkMinds Library theo luồng

Tài liệu này dùng để demo đồ án trước giảng viên hoặc hướng dẫn người mới đọc
source code. Kịch bản bám theo phần **3. Kiểm tra chi tiết theo luồng** trong
[REQUIREMENTS_AUDIT.md](REQUIREMENTS_AUDIT.md).

Thời lượng gợi ý: **35–45 phút**. Nếu chỉ demo chức năng chính, dùng các luồng
1–8 trong khoảng 20 phút; các luồng còn lại dùng để giải thích kiến trúc, bảo
mật và chất lượng mã nguồn.

## 1. Chuẩn bị trước khi demo

### 1.1. Khởi động hệ thống

Mở PowerShell tại thư mục project:

```powershell
cd D:\sparkminds\src
docker compose up -d

$demoBytes = New-Object byte[] 32
$demoRandom = [Security.Cryptography.RandomNumberGenerator]::Create()
$demoRandom.GetBytes($demoBytes)
$demoRandom.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($demoBytes)

# Dùng Google thật nếu đã cấu hình; nếu chưa có, dùng giá trị local để app chạy.
$env:GOOGLE_CLIENT_ID = "google-disabled-local"
$env:GOOGLE_CLIENT_SECRET = "google-disabled-local"

.\mvnw.cmd spring-boot:run
```

Mở sẵn các tab:

| Mục đích | Địa chỉ |
|---|---|
| Giao diện web | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Mailpit local | `http://localhost:8025` |
| Health check | `http://localhost:8080/actuator/health` |

Tài khoản admin demo:

```text
username: admin
password: admin
```

### 1.2. Dữ liệu nên chuẩn bị

- Một email mới để đăng ký, ví dụ `demo.user@example.com`.
- Một sách đang còn trong kho, ví dụ **Clean Code** hoặc **Effective Java**.
- File `books-import.csv` để import thành công.
- File `books-import-invalid.csv` để chứng minh validation/rollback.
- Mở terminal log để quan sát request ID, HTTP status và AOP logging.

### 1.3. Câu mở đầu gợi ý

> Em xin trình bày SparkMinds Library, ứng dụng quản lý thư viện được xây dựng
> bằng Spring Boot, JPA, PostgreSQL và giao diện web tích hợp sẵn. Hệ thống có
> hai vai trò chính là Admin và User, hỗ trợ quản lý sách, thành viên,
> mượn–trả, xác thực email, JWT, refresh token, import CSV và maintenance
> mode. Trong phần demo, em sẽ đi từ luồng người dùng đến luồng quản trị, sau
> đó giải thích các thư mục source code tương ứng.

## 2. Bản đồ thư mục cần giới thiệu trước

Đây là phần nên trình bày trong 2–3 phút trước khi đi vào demo.

| Thư mục | Vai trò | Xuất hiện trong luồng nào |
|---|---|---|
| `auth/` | Login, register, verify email, reset/change password, refresh/logout, Google OAuth | 3.1–3.4 |
| `book/` | Sách, tác giả, danh mục, search Specification, import CSV | 3.5 |
| `borrowing/` | Mượn, trả và lịch sử giao dịch | 3.7 |
| `member/` | User account, role, member profile và quản lý thành viên | 3.1, 3.3, 3.6, 3.9 |
| `profile/` | User tự cập nhật hồ sơ | 3.4 |
| `savedbook/` | Chức năng lưu/bỏ lưu sách | Luồng User mở rộng |
| `security/` | Spring Security, JWT encoder/decoder, user details, token revoke validation | 3.1–3.2 |
| `systemconfig/` | Maintenance mode, filter chặn API và cấu hình hệ thống | 3.8 |
| `mail/` | Gửi email xác minh/reset/change email | 3.3–3.4 |
| `common/` | Response chuẩn, exception, validation, HTTP log và AOP log | Xuyên suốt |
| `config/` | Admin mặc định, JWT config, password encoder, OpenAPI | 3.1, 3.14 |
| `aspect/` | Aspect dùng cho cross-cutting concern nếu có | Xuyên suốt |
| `resources/db/changelog/` | Liquibase migration và seed data | 3.14 |
| `resources/static/` | HTML/CSS/JavaScript, đa ngôn ngữ, ảnh bìa | Mọi demo giao diện |
| `src/test/` | Integration test bằng JUnit, MockMvc, Testcontainers | 3.13 |

### Lời trình bày gợi ý

> Source được chia theo nghiệp vụ thay vì gom toàn bộ controller hoặc entity vào
> một nơi. Mỗi nghiệp vụ như sách, thành viên hay mượn–trả có controller, DTO,
> service, repository và entity riêng. Controller nhận request, service chứa
> business logic và transaction, repository giao tiếp JPA, còn DTO giúp không
> trả trực tiếp entity ra API.

## 3. Kịch bản theo luồng

---

## 3.1. Luồng khởi tạo Admin, login, JWT và phân quyền

### Mục tiêu demo

Chứng minh hệ thống tự tạo admin, login bằng JWT và giới hạn chức năng quản trị
cho `ROLE_ADMIN`.

### Thao tác trên giao diện

1. Mở `http://localhost:8080`.
2. Đăng nhập bằng `admin/admin`.
3. Sau khi login, chỉ ra menu Admin có thêm **Thành viên** và **Hệ thống**.
4. Mở **Kho sách**, chỉ ra các nút **Thêm sách**, **Nhập CSV**, chỉnh sửa và
   ngừng hoạt động.
5. Mở Swagger, gọi một endpoint `/api/admin/members` không có token để cho
   thấy HTTP 401.
6. Nhấn **Authorize**, nhập access token từ API login, gọi lại endpoint để
   thấy kết quả thành công.

### Lời nói gợi ý

> Khi khởi động lần đầu, hệ thống kiểm tra tài khoản admin. Nếu chưa có, nó tự
> tạo admin với password đã được mã hóa và role ADMIN. Khi login thành công,
> backend phát hành access token và refresh token. Access token chứa claim
> roles; Spring Security đọc claim này để quyết định endpoint nào được gọi.
> Các API `/api/admin/**` chỉ được phép với role ADMIN.

### Luồng kỹ thuật bên trong

```text
application.yml
  -> AdminDataInitializer tạo admin nếu chưa tồn tại
  -> POST /api/auth/login
  -> AuthController nhận LoginRequest
  -> AuthService gọi AuthenticationManager
  -> CustomUserDetailsService tải UserAccount + roles
  -> JwtTokenService tạo access token có uid, sub, roles, jti
  -> RefreshTokenService tạo refresh token hash trong database
  -> Frontend lưu token trong sessionStorage
  -> SecurityConfig/JwtAuthenticationConverter đọc roles ở request sau
```

### Thư mục và file cần mở

| File | Chức năng cần giải thích |
|---|---|
| `resources/application.yml` | Cấu hình `app.admin.*`, JWT và datasource |
| `config/AdminDataInitializer.java` | Tự tạo admin, gán role, bật account |
| `auth/controller/AuthController.java` | Endpoint `/api/auth/login` |
| `auth/dto/request/LoginRequest.java` | Validate username/email và password |
| `auth/service/AuthService.java` | Authenticate, phát hành token response |
| `security/SecurityConfig.java` | Quy tắc permit/authenticated/hasRole ADMIN |
| `security/jwt/JwtTokenService.java` | Tạo JWT và claim |
| `security/service/CustomUserDetailsService.java` | Lấy user từ DB để Spring Security xác thực |
| `member/entity/UserAccount.java` và `Role.java` | Quan hệ tài khoản–role |

### Bằng chứng nên chỉ ra

- Menu Admin khác menu User.
- Swagger trả 401 trước khi Authorize và thành công sau Authorize.
- Log terminal có `HTTP_REQUEST`, `FUNCTION_CALL`, `FUNCTION_RETURN`.
- JWT không trả password; response chỉ gồm token và thời hạn token.

### Lưu ý trung thực khi bảo vệ

API admin đã khóa đúng. Tuy nhiên các API borrow/save/profile hiện chỉ yêu cầu
authenticated, chưa gắn riêng `ROLE_USER`; đây là điểm được ghi **MỘT PHẦN**
trong audit và có thể nêu là hạng mục hoàn thiện tiếp.

---

## 3.2. Luồng refresh token và logout chặn token cũ

### Mục tiêu demo

Chứng minh logout không chỉ xóa token ở trình duyệt và refresh token có cơ chế
rotate.

### Thao tác demo dễ hiểu

1. Đăng nhập bằng admin tại Swagger hoặc giao diện.
2. Lấy access token và refresh token từ response login.
3. Gọi `GET /api/auth/me` với access token để chứng minh token hợp lệ.
4. Gọi `POST /api/auth/logout` với JWT hiện tại và refresh token.
5. Gọi lại `/api/auth/me` với access token cũ; kết quả phải bị từ chối.
6. Đăng nhập lại để lấy cặp token mới.
7. Gọi `POST /api/auth/refresh` với refresh token mới; so sánh refresh token
   trả về khác token cũ.
8. Gọi refresh lần hai bằng refresh token cũ để chứng minh token cũ đã revoke.

### Lời nói gợi ý

> Logout trong hệ thống này không chỉ là xóa session ở frontend. Access token
> có `jti` được lưu vào bảng blacklist `revoked_tokens`; mỗi request JWT đều
> kiểm tra bảng này. Refresh token cũng được lưu dạng hash và bị revoke. Khi
> refresh thành công, token cũ bị thu hồi và token mới được sinh ra, gọi là
> rotating refresh token.

### Luồng kỹ thuật bên trong

```text
Logout
  -> AuthService.logout
  -> RefreshTokenService.revokeIfPresent
  -> RevokedTokenService.revoke(jwt.jti)
  -> revoked_tokens + refresh_tokens được cập nhật

Request dùng JWT cũ
  -> JWT decoder
  -> RevokedTokenValidator.existsByJti
  -> token bị từ chối

Refresh
  -> RefreshTokenService.rotate
  -> validate hash, expiry, account state
  -> revoke refresh token cũ
  -> tạo refresh token mới + access token mới
```

### Thư mục và file cần mở

- `auth/service/AuthService.java`: login, refresh và logout.
- `auth/service/RefreshTokenService.java`: hash, create, rotate, revoke.
- `auth/service/RevokedTokenService.java`: lưu `jti` bị thu hồi.
- `auth/entity/RefreshToken.java`, `RevokedToken.java`: cấu trúc bảng token.
- `security/jwt/RevokedTokenValidator.java`: chặn JWT đã logout.
- `resources/db/changelog/changes/005-create-auth-token-tables.yaml`.
- `resources/static/assets/js/app.js`: frontend tự gọi refresh khi gặp 401.

### Bằng chứng nên chỉ ra

- Request access token cũ bị 401 sau logout.
- Refresh token cũ không dùng được sau khi rotate.
- Nếu mở DevTools > Application > Session Storage, chỉ ra access/refresh token
  được thay đổi sau refresh; không hiển thị token trên màn hình khi demo công
  khai.

---

## 3.3. Luồng đăng ký và xác minh email

### Mục tiêu demo

Chứng minh account mới không thể login trước khi xác minh email.

### Thao tác trên giao diện

1. Logout admin hoặc mở tab ẩn danh.
2. Chuyển sang **Đăng ký ngay**.
3. Nhập email mới, ví dụ `demo.user@example.com`.
4. Nhập password hợp lệ: tối thiểu 8 ký tự, có chữ hoa, chữ thường, số và ký
   tự đặc biệt, ví dụ `Demo@12345`.
5. Nhấn **Tạo tài khoản**.
6. Thử login ngay; hệ thống phải báo account chưa được xác minh/không được
   phép login.
7. Mở Mailpit `http://localhost:8025`, mở email và nhấn link verify.
8. Login lại bằng email/password vừa tạo.
9. Sau login, mở **Tài khoản** để cập nhật số điện thoại và ngày sinh.

### Lời nói gợi ý

> Khi đăng ký, mật khẩu không lưu dạng text mà được mã hóa. Account mới có
> `enabled=false` và `emailVerified=false`, vì vậy không thể login. Hệ thống
> chỉ lưu hash của verification token trong database và gửi token gốc qua
> email. Khi người dùng click link, token được kiểm tra hạn dùng và one-time
> usage, sau đó account mới được bật.

### Luồng kỹ thuật bên trong

```text
POST /api/auth/register
  -> RegisterRequest validate email/password
  -> RegistrationService kiểm tra email trùng
  -> BCrypt hash password
  -> tạo UserAccount + MemberProfile + role USER
  -> enabled=false, emailVerified=false
  -> tạo EmailVerificationToken hash
  -> MailService gửi link

GET /api/auth/verify-email?token=...
  -> so hash token, kiểm tra expiry/used
  -> emailVerified=true, enabled=true
  -> token.used=true
```

### Thư mục và file cần mở

| File/thư mục | Chức năng |
|---|---|
| `auth/dto/request/RegisterRequest.java` | Rule validate email và password |
| `common/validation/ValidPassword.java`, `PasswordValidator.java` | Custom validation password |
| `auth/service/RegistrationService.java` | Tạo account, profile, token, gửi mail |
| `auth/entity/EmailVerificationToken.java` | Token verification có expiry/used |
| `auth/repository/EmailVerificationTokenRepository.java` | Truy vấn token hash |
| `mail/service/MailService.java` | Gửi email xác minh/reset/change email |
| `member/entity/UserAccount.java`, `MemberProfile.java` | Account và hồ sơ member |
| `db/changelog/changes/007-create-email-verification-tokens.yaml` | Migration bảng token |

### Bằng chứng nên chỉ ra

- Mailpit nhận được email.
- Login trước verify thất bại; login sau verify thành công.
- Validation từ UI/API khi email sai hoặc password yếu.
- Trong DB chỉ có token hash, không có password rõ.

---

## 3.4. Luồng quên mật khẩu, đổi mật khẩu và đổi email

### Mục tiêu demo

Chứng minh ba luồng account security khác nhau đều có validation và token/code
có hạn.

### Phần A — Quên và reset password

#### Thao tác

1. Tại login, nhấn **Quên mật khẩu?**.
2. Nhập email đã đăng ký.
3. Mở Mailpit, nhấn link reset password.
4. Nhập password mới hợp lệ, khác password cũ.
5. Login bằng password mới.

#### Lời nói gợi ý

> API forgot-password luôn trả thông báo chung, không tiết lộ email đó có tồn
> tại hay không. Reset token có hạn, dùng một lần và được lưu dạng hash. Sau
> khi reset thành công, refresh token cũ bị vô hiệu để bảo vệ account.

### Phần B — Đổi password khi đã login

#### Thao tác

1. Mở **Tài khoản**.
2. Nhập mật khẩu hiện tại và mật khẩu mới.
3. Thử nhập mật khẩu mới trùng mật khẩu cũ để chứng minh validation.
4. Nhập password mới khác, hợp lệ và xác nhận đổi.
5. Hệ thống yêu cầu login lại.

### Phần C — Đổi email bằng mã xác minh

#### Thao tác

1. Tại **Tài khoản**, nhập email mới hợp lệ.
2. Nhấn **Gửi mã xác minh**.
3. Giao diện chỉ hiển thị ô mã sau khi gửi mail thành công.
4. Lấy mã 6 số trong Mailpit, nhập mã và xác nhận.
5. Hệ thống đổi email và yêu cầu login lại bằng email mới.

### Thư mục và file cần mở

- `auth/controller/AuthController.java`: forgot/reset/change password,
  change email request/verify.
- `auth/service/PasswordService.java`: reset và change password.
- `auth/service/EmailChangeService.java`: gửi/kiểm tra mã 6 số.
- `auth/entity/PasswordResetToken.java`.
- `auth/entity/EmailChangeVerification.java`.
- `auth/dto/request/`: DTO và validation input.
- `db/changelog/changes/008-create-password-reset-tokens.yaml`.
- `db/changelog/changes/009-create-email-change-verifications.yaml`.
- `profile/`: trang và service hồ sơ người dùng.

### Bằng chứng nên chỉ ra

- Password mới không được trùng password cũ.
- Mã xác minh sai/hết hạn bị từ chối.
- Account phải login lại sau change password/change email.
- Password field có biểu tượng mắt, nhưng password không thể được xem lại.

---

## 3.5. Luồng quản lý sách: search, CRUD, phân trang và CSV

### Mục tiêu demo

Chứng minh Admin có thể tìm kiếm, tạo/sửa/ngừng hoạt động sách và import CSV
an toàn.

### Phần A — Search và phân trang

#### Thao tác

1. Login admin, mở **Kho sách**.
2. Search theo tên: `Clean Code`.
3. Lọc theo danh mục, nhà xuất bản và tình trạng còn sách.
4. Xóa lọc để thấy danh sách đầy đủ.
5. Chuyển trang bằng các số `1, 2, 3...`.
6. Mở trang chi tiết một sách để chỉ ra mô tả, tác giả, ISBN, số lượng, ảnh
   bìa và trạng thái.

#### Lời nói gợi ý

> Search không viết SQL thủ công trong controller. Request được chuyển vào
> `BookSearchRequest`; `BookSpecification` tạo điều kiện động; service dùng
> allow-list cho sort field và khóa size tối đa 10 record ở cả controller lẫn
> service.

### Phần B — Tạo và cập nhật sách

#### Thao tác

1. Nhấn **Thêm sách**.
2. Thử bỏ trống ISBN/tên sách để thấy validation.
3. Nhập dữ liệu hợp lệ, ví dụ ISBN chưa có, danh mục, tác giả và số lượng.
4. Lưu, tìm lại sách vừa tạo.
5. Mở **Chỉnh sửa**, thay mô tả hoặc số lượng.
6. Nếu sách đang được mượn, thử giảm tổng số lượng thấp hơn số đã mượn để
   chứng minh business rule.
7. Thử ngừng hoạt động sách đang được mượn để chứng minh hệ thống từ chối.

### Phần C — Import CSV và rollback

#### Thao tác

1. Nhấn **Nhập CSV**, chọn `books-import.csv`.
2. Import thành công, kiểm tra số sách được thêm.
3. Chọn `books-import-invalid.csv`.
4. Quan sát lỗi dòng/header/ISBN; sau đó search để xác nhận file lỗi không
   tạo một phần dữ liệu nào.

### Luồng kỹ thuật bên trong

```text
GET /api/books
  -> BookSearchRequest
  -> BookSpecification
  -> BookRepository.findAll(specification, pageable)
  -> BookMapper -> PageResponse

POST/PUT/DELETE /api/books
  -> @Valid DTO
  -> BookService @Transactional
  -> ISBN/category/quantity/business rule validation
  -> BookRepository

POST /api/books/import
  -> MultipartFile
  -> BookCsvImportService @Transactional(rollbackFor=Exception.class)
  -> validate file/header/row
  -> BookService.create từng bản ghi
  -> lỗi một dòng = rollback toàn bộ
```

### Thư mục và file cần mở

| File/thư mục | Chức năng |
|---|---|
| `book/controller/BookController.java` | Search, details, CRUD, category/author lookup |
| `book/controller/BookImportController.java` | Multipart import endpoint |
| `book/dto/request/BookSearchRequest.java` | Điều kiện search |
| `book/dto/request/CreateBookRequest.java`, `UpdateBookRequest.java` | Validation create/update |
| `book/service/BookService.java` | Business rule, transaction, soft delete |
| `book/service/BookCsvImportService.java` | Validate/import/rollback CSV |
| `book/specification/BookSpecification.java` | Predicate search động |
| `book/entity/Book.java`, `Author.java`, `Category.java` | Mô hình dữ liệu sách |
| `book/mapper/BookMapper.java` | Entity sang response DTO |
| `resources/static/assets/images/books/` | Ảnh bìa theo ISBN |
| `books-import.csv`, `books-import-invalid.csv` | File demo |

### Bằng chứng nên chỉ ra

- Mỗi trang tối đa 10 records.
- ISBN trùng bị báo lỗi.
- CSV >5 MB hoặc không phải `.csv` bị từ chối.
- File CSV lỗi rollback toàn bộ.
- Log cho import chỉ log metadata file, không log dữ liệu nhạy cảm.

---

## 3.6. Luồng quản lý thành viên

### Mục tiêu demo

Chứng minh Admin có thể search đa điều kiện, tạo, sửa và vô hiệu hóa member.

### Thao tác trên giao diện

1. Login admin, mở **Thành viên**.
2. Search bằng từ khóa tên/email/mã thành viên.
3. Thử kết hợp ít nhất 5 điều kiện:
   - tên gần đúng;
   - tên sách đã mượn;
   - ngày sinh từ/đến;
   - xác minh email;
   - tài khoản khóa/hoạt động.
4. Nhấn **Thêm thành viên**, tạo account với email/password/họ tên.
5. Chỉnh sửa số điện thoại, ngày sinh, trạng thái account.
6. Vô hiệu hóa một member test và thử login bằng member đó.

### Lời nói gợi ý

> Màn hình này có nhiều hơn 5 điều kiện search. Backend dùng
> `MemberSpecification` để join member với user, role, borrowing và book rồi
> tạo điều kiện động. CRUD member đặt ở API `/api/admin/members`, vì vậy chỉ
> admin gọi được.

### Luồng kỹ thuật bên trong

```text
GET /api/admin/members?fullName=...&bookTitle=...&dateOfBirthFrom=...
  -> MemberSearchRequest
  -> MemberSpecification join UserAccount, Role, Borrowing, Book
  -> MemberService.search @Transactional(readOnly=true)
  -> PageResponse<MemberResponse>

POST/PUT/DELETE /api/admin/members
  -> @Valid CreateMemberRequest/UpdateMemberRequest
  -> MemberService @Transactional
  -> UserAccount + MemberProfile được tạo/cập nhật/vô hiệu hóa
```

### Thư mục và file cần mở

- `member/controller/MemberController.java`: REST API admin.
- `member/dto/request/MemberSearchRequest.java`: 12 điều kiện search.
- `member/dto/request/CreateMemberRequest.java`, `UpdateMemberRequest.java`:
  validation.
- `member/service/MemberService.java`: create/update/deactivate/search.
- `member/specification/MemberSpecification.java`: join và predicate.
- `member/entity/MemberProfile.java`, `UserAccount.java`, `Role.java`:
  quan hệ dữ liệu.
- `member/mapper/MemberMapper.java`: response không lộ password.
- `static/assets/js/app.js`, hàm `renderMembersPage`: màn hình member.

### Điểm cần trình bày minh bạch

- Backend hỗ trợ `bookId`, nhưng giao diện hiện tìm theo `bookTitle` bằng input,
  chưa phải combobox chọn sách.
- Date input dùng chuẩn browser `yyyy-MM-dd`, còn đề bài ghi `yyyy/MM/dd`.
- Đây là hai điểm **MỘT PHẦN** trong audit; logic search backend vẫn đầy đủ.

---

## 3.7. Luồng mượn và trả sách

### Mục tiêu demo

Chứng minh rule tồn kho, một lượt mượn đang hoạt động, thời gian trả và hoàn
tồn kho.

### Thao tác demo

1. Login bằng User đã verify email.
2. Vào **Kho sách**, chọn sách còn tồn kho, nhấn **Mượn sách**.
3. Quan sát toast hiển thị hạn trả.
4. Vào **Sách của tôi**, chỉ ra sách, ngày mượn, hạn trả, trạng thái.
5. Thử mượn thêm một cuốn khác; hệ thống báo mỗi user chỉ có một lượt mượn
   đang hoạt động.
6. Logout User, login Admin.
7. Mở **Mượn & trả**, chỉ ra cột người mượn, ngày mượn, hạn trả, ngày trả,
   trạng thái.
8. Login lại User hoặc để Admin hỗ trợ trả, nhấn **Trả sách**.
9. Kiểm tra số lượng sách trong kho tăng trở lại và `returnedAt` xuất hiện.
10. Thử trả lần hai để thấy lỗi business rule.

### Lời nói gợi ý

> Luồng mượn và trả dùng transaction và pessimistic lock để tránh hai request
> cùng trừ hoặc cộng tồn kho sai. Trước khi mượn, service kiểm tra trạng thái
> account, xác minh email, lock account, sách active, tồn kho và số lượt đang
> mượn. Khi trả, chỉ chủ lượt mượn hoặc admin được trả; hệ thống ghi thời điểm
> trả rồi cộng lại tồn kho.

### Luồng kỹ thuật bên trong

```text
POST /api/borrowings
  -> BorrowBookRequest validate bookId
  -> BorrowingService.borrowBook @Transactional
  -> lock MemberProfile + Book
  -> validate user/account/active borrowing/inventory
  -> create Borrowing(BORROWED, borrowedAt, dueAt)
  -> availableQuantity - 1

POST /api/borrowings/{id}/return
  -> BorrowingService.returnBook @Transactional
  -> lock Borrowing + Book
  -> validate owner/admin + not returned
  -> returnedAt = now, status=RETURNED
  -> availableQuantity + 1
```

### Thư mục và file cần mở

| File | Chức năng |
|---|---|
| `borrowing/controller/BorrowingController.java` | Mượn, trả, lịch sử cá nhân |
| `borrowing/controller/AdminBorrowingController.java` | Lịch sử toàn bộ cho admin |
| `borrowing/dto/request/BorrowBookRequest.java` | Validate `bookId` |
| `borrowing/service/BorrowingService.java` | Toàn bộ rule và transaction |
| `borrowing/repository/BorrowingRepository.java` | Query lịch sử và lock query |
| `borrowing/entity/Borrowing.java` | `borrowedAt`, `dueAt`, `returnedAt`, status |
| `borrowing/entity/BorrowingStatus.java` | `BORROWED`, `RETURNED` |
| `borrowing/mapper/BorrowingMapper.java` | Response cho giao diện/API |
| `book/entity/Book.java` | `totalQuantity`, `availableQuantity` |

### Bằng chứng nên chỉ ra

- Tồn kho giảm khi mượn, tăng khi trả.
- Ngày giờ hiển thị đến giây.
- Không thể mượn cuốn thứ hai khi còn một lượt active.
- Không thể trả lần hai.
- Lịch sử admin có thông tin người mượn.

---

## 3.8. Luồng maintenance mode

### Mục tiêu demo

Chứng minh Admin có thể tạm dừng nghiệp vụ và vẫn có đường để khôi phục hệ
thống.

### Thao tác demo

1. Login admin, mở **Hệ thống**.
2. Nhập thông báo, ví dụ: `Hệ thống bảo trì trong 10 phút.`
3. Bật công tắc **Chế độ bảo trì**, nhấn **Lưu cấu hình**.
4. Mở tab User hoặc Swagger, gọi API sách/member/borrowings.
5. Chỉ ra HTTP 503 và thông báo maintenance.
6. Quay lại tab admin, mở **Hệ thống**, tắt công tắc và lưu.
7. Gọi lại API sách để chứng minh hệ thống hoạt động bình thường.

### Lời nói gợi ý

> Maintenance mode được thực hiện bằng filter, nên không cần thêm điều kiện vào
> từng controller. Mọi business API đi qua filter sẽ nhận HTTP 503 nếu chế độ
> bảo trì đang bật. Login, social exchange và system config được miễn có chủ ý,
> nếu không admin sẽ không thể đăng nhập hoặc tắt maintenance.

### Luồng kỹ thuật bên trong

```text
PUT /api/admin/system-config/maintenance
  -> SystemConfigController
  -> SystemConfigService.update @Transactional
  -> bảng system_config

Request /api/**
  -> MaintenanceModeFilter
  -> isMaintenanceMode?
  -> true: trả ApiErrorResponse HTTP 503
  -> false: filterChain.doFilter
```

### Thư mục và file cần mở

- `systemconfig/controller/SystemConfigController.java`.
- `systemconfig/service/SystemConfigService.java`.
- `systemconfig/filter/MaintenanceModeFilter.java`.
- `systemconfig/entity/SystemConfig.java`.
- `systemconfig/repository/SystemConfigRepository.java`.
- `systemconfig/dto/`: request/response.
- `db/changelog/changes/012-create-system-config.yaml`.
- `security/SecurityConfig.java`: vị trí filter trong chain.

### Điểm cần nói rõ

Theo câu chữ “chặn tất cả API”, luồng này được đánh dấu **MỘT PHẦN** vì có các
exception vận hành cần thiết. Đây là lựa chọn thiết kế chủ động, không phải lỗi
ngẫu nhiên.

---

## 3.9. Luồng saved books và profile User

### Mục tiêu demo

Cho thấy ngoài yêu cầu CRUD, ứng dụng có trải nghiệm người dùng đầy đủ: lưu
sách và cập nhật hồ sơ.

### Thao tác

1. Login User, mở chi tiết một cuốn sách.
2. Nhấn **Lưu**; nút đổi thành **Đã lưu**.
3. Mở **Sách của tôi** hoặc khu vực danh sách đã lưu để kiểm tra.
4. Nhấn bỏ lưu.
5. Mở **Tài khoản**, cập nhật username, họ tên, phone, date of birth, address.
6. Lưu hồ sơ và kiểm tra thông tin ở sidebar cập nhật.

### Lời nói gợi ý

> Saved books tách riêng khỏi borrowing. Việc lưu sách không làm thay đổi tồn
> kho. Hồ sơ member tách khỏi UserAccount theo quan hệ one-to-one; điều này
> giúp phần security account và thông tin cá nhân có trách nhiệm rõ ràng.

### Thư mục và file cần mở

- `savedbook/controller/SavedBookController.java`.
- `savedbook/service/SavedBookService.java`.
- `savedbook/entity/SavedBook.java`.
- `savedbook/repository/SavedBookRepository.java`.
- `profile/controller/ProfileController.java`.
- `profile/service/ProfileService.java`.
- `profile/dto/`: validate và response profile.
- `member/entity/UserAccount.java`, `MemberProfile.java`.
- `db/changelog/changes/013-create-saved-books.yaml`.

---

## 3.10. Luồng quan hệ Hibernate và transaction

### Mục tiêu demo

Giải thích thiết kế database có quan hệ JPA và lý do transaction quan trọng.

### Cách demo

Mở code entity theo thứ tự dưới đây, vẽ nhanh sơ đồ trên slide/giấy hoặc dùng
database viewer.

```text
UserAccount 1 --- 1 MemberProfile
UserAccount * --- * Role
MemberProfile 1 --- * Borrowing * --- 1 Book
Category 1 --- * Book
Book * --- * Author
UserAccount 1 --- * RefreshToken / RevokedToken / VerificationToken
```

### Lời nói gợi ý

> Hệ thống không lưu tất cả thông tin trong một bảng. UserAccount tập trung vào
> login, password hash, trạng thái và role. MemberProfile chứa thông tin cá
> nhân. Book có nhiều author và một category. Borrowing là bảng nghiệp vụ nối
> member với book, đồng thời lưu trạng thái và thời gian. Các nghiệp vụ có thể
> thay đổi nhiều bảng như mượn sách đều phải đặt trong transaction để một lỗi
> bất kỳ sẽ rollback toàn bộ.

### File cần mở

- `member/entity/UserAccount.java`.
- `member/entity/MemberProfile.java`.
- `member/entity/Role.java`.
- `book/entity/Book.java`, `Author.java`, `Category.java`.
- `borrowing/entity/Borrowing.java`.
- các entity token trong `auth/entity/`.
- `book/service/BookService.java`, `BorrowingService.java`,
  `MemberService.java`: ví dụ `@Transactional`.

### Điểm cần nhấn mạnh

- CRUD không đặt transaction ở controller; transaction nằm ở service.
- Search dùng `@Transactional(readOnly = true)` khi phù hợp.
- CSV dùng `rollbackFor = Exception.class` để tránh import nửa chừng.
- Borrow/return dùng lock để giảm rủi ro race condition.

---

## 3.11. Luồng logging, AOP và exception handling

### Mục tiêu demo

Chứng minh ứng dụng có khả năng quan sát và trả lỗi chuẩn cho client.

### Thao tác demo

1. Mở terminal đang chạy app.
2. Từ giao diện hoặc Swagger gọi search books thành công.
3. Chỉ ra các dòng `HTTP_REQUEST`, `FUNCTION_CALL`, `FUNCTION_RETURN`,
   `HTTP_RESPONSE` có request ID và duration.
4. Tạo một lỗi validation, ví dụ thêm sách thiếu ISBN hoặc import CSV sai.
5. Quan sát frontend hiển thị thông báo rõ; Swagger/API trả `ApiErrorResponse`
   có status, message, path và `fieldErrors`.
6. Mở file `logs/library-management.log` để chứng minh log file.

### Lời nói gợi ý

> Logging được tách khỏi business service bằng filter và AOP. HTTP filter ghi
> nhận request/response metadata; aspect ghi nhận controller call, kết quả và
> exception. Các request auth được mask để password/token không xuất hiện
> trong log. Global exception handler chuyển phần lớn lỗi nghiệp vụ và
> validation thành format JSON thống nhất.

### Thư mục và file cần mở

| File | Chức năng |
|---|---|
| `resources/log4j2-spring.xml` | Console và rolling file appender |
| `common/logging/HttpLoggingFilter.java` | HTTP request/response metadata |
| `common/logging/ControllerLoggingAspect.java` | AOP function call/return/error |
| `common/exception/GlobalExceptionHandler.java` | Map exception sang API error |
| `common/api/ApiErrorResponse.java` | Schema lỗi chuẩn |
| `common/exception/` | Business/resource/token exception |
| `common/validation/` | Validation password và DTO rule |

### Điểm cần nói minh bạch

- Log không lưu body đầy đủ nhằm tránh lộ password, token và PII.
- Chưa có generic `@ExceptionHandler(Exception.class)` làm lớp cuối; đây là
  hạng mục hoàn thiện tiếp cho lỗi SMTP/runtime ngoài dự kiến.

---

## 3.12. Luồng đa ngôn ngữ VI/EN

### Mục tiêu demo

Chứng minh giao diện được chuyển ngữ cả nội dung tĩnh lẫn nội dung động.

### Thao tác demo

1. Tại trang login, đổi sang English.
2. Chỉ ra login/register/forgot password đổi ngôn ngữ ngay.
3. Login admin, lần lượt mở Trang chủ, Kho sách, Mượn & trả, Thành viên,
   Hệ thống, Tài khoản.
4. Đổi EN/VI giữa các trang.
5. Thử tạo validation error để chứng minh thông báo backend cũng được dịch.
6. Chỉ ra ngày/giờ thay đổi format theo locale.

### Lời nói gợi ý

> Đa ngôn ngữ không chỉ là đổi text trong HTML. App dùng catalog text tĩnh,
> reverse map, map thông báo API và pattern cho chuỗi động như số lượng, phân
> trang, due date. MutationObserver đảm bảo modal hoặc nội dung mới render vẫn
> được dịch. Dữ liệu nghiệp vụ như tên sách, tác giả và email được giữ nguyên,
> không dịch máy.

### File cần mở

- `resources/static/index.html`: shell login/app và dropdown ngôn ngữ.
- `resources/static/assets/js/app.js`:
  - `EN_TEXT`, `VI_TEXT`;
  - `API_TEXT_VI`;
  - `translateTree`;
  - `MutationObserver`;
  - `formatDate`, `formatDateTime`.
- `resources/static/assets/css/app.css`, `library.css`: style giao diện.
- `test/.../FrontendIntegrationTest.java`: regression test frontend/i18n.

---

## 3.13. Luồng Google OAuth

### Mục tiêu demo

Chứng minh login Google không đưa JWT trực tiếp vào URL.

### Thao tác demo

> Chỉ thực hiện khi `GOOGLE_CLIENT_ID` và `GOOGLE_CLIENT_SECRET` là credentials
> thật, callback URL Google Console đã cấu hình đúng.

1. Logout khỏi ứng dụng.
2. Nhấn **Tiếp tục với Google**.
3. Chọn tài khoản Google đã là Test User.
4. Sau callback, ứng dụng tự login và đi vào Trang chủ.
5. Mở URL trên thanh địa chỉ để chỉ ra không có JWT; chỉ có one-time
   `socialCode` và frontend exchange code này lấy token.

### Lời nói gợi ý

> Google chỉ xác thực danh tính. Sau callback, backend liên kết Google subject
> với tài khoản library qua OAuthIdentity. Thay vì đặt JWT trong URL, backend
> tạo social code một lần có hạn. Frontend đổi social code lấy JWT qua API,
> tránh rò token trong browser history, log hoặc referrer.

### Thư mục và file cần mở

- `auth/oauth/GoogleOidcUserService.java`.
- `auth/oauth/GoogleAuthenticationSuccessHandler.java`.
- `auth/oauth/GoogleAuthenticationFailureHandler.java`.
- `auth/entity/OAuthIdentity.java`, `SocialLoginCode.java`.
- `auth/service/SocialLoginCodeService.java`.
- `auth/controller/AuthController.java`: `/social/exchange`.
- `security/SecurityConfig.java`: `oauth2Login`.
- migration `015-create-oauth-identities.yaml`,
  `016-create-social-login-codes.yaml`.
- `static/assets/js/app.js`: xử lý `socialCode`.

### Lưu ý

Integration test hiện kiểm tra redirect endpoint Google, chưa mock full callback
Google → social code → JWT. Nêu đây là kế hoạch test end-to-end tiếp theo nếu
được hỏi.

---

## 3.14. Luồng Liquibase, Swagger và JUnit

### Mục tiêu demo

Chứng minh schema database được quản lý bằng migration, API có tài liệu và
chức năng có kiểm thử tự động.

### Phần A — Liquibase

#### Thao tác

1. Chỉ vào `db/changelog/db.changelog-master.yaml`.
2. Giải thích thứ tự 16 changeset từ role/account đến Google OAuth.
3. Nếu cần, mở log startup để chỉ ra `Database is up to date`.
4. Mở migration `014-seed-extended-book-catalog.yaml` để chỉ ra 50 sách mẫu.

#### Lời nói gợi ý

> Liquibase giúp schema database có lịch sử, đồng bộ giữa máy thành viên và
> tránh thao tác tạo bảng thủ công. Khi cần thay đổi database, em tạo changeset
> mới, không sửa changeset đã chạy trên môi trường dùng chung.

### Phần B — Swagger/OpenAPI

#### Thao tác

1. Mở `/swagger-ui.html`.
2. Chọn tag Books, Members, Borrowings, Authentication.
3. Dùng **Authorize** với JWT admin.
4. Gọi thử endpoint GET Books hoặc GET Members.

#### File cần mở

- `config/OpenApiConfig.java`.
- Các controller trong `auth/controller`, `book/controller`,
  `member/controller`, `borrowing/controller`, `systemconfig/controller`.

### Phần C — JUnit/Testcontainers

#### Thao tác

```powershell
.\mvnw.cmd test
```

Sau đó chỉ ra kết quả:

```text
Tests run: 75
Failures: 0
Errors: 0
Skipped: 0
```

#### Lời nói gợi ý

> Test không dùng database thật của ứng dụng. `AbstractIntegrationTest` khởi
> tạo PostgreSQL 17 riêng bằng Testcontainers, vì vậy các test API có môi
> trường database gần production nhưng tách biệt dữ liệu demo.

#### Thư mục và file cần mở

- `src/test/java/com/sparkminds/library/integration/AbstractIntegrationTest.java`.
- `auth/controller/AccountLifecycleIntegrationTest.java`.
- `auth/controller/TokenLifecycleIntegrationTest.java`.
- `book/controller/BookControllerIntegrationTest.java`.
- `book/controller/BookCsvImportIntegrationTest.java`.
- `borrowing/controller/BorrowingControllerIntegrationTest.java`.
- `member/controller/MemberControllerIntegrationTest.java`.
- `systemconfig/controller/SystemConfigControllerIntegrationTest.java`.
- `config/DocumentationIntegrationTest.java`.
- `config/FrontendIntegrationTest.java`.

---

## 4. Kịch bản rút gọn 10 phút

Nếu thời gian demo rất ngắn, trình bày theo thứ tự này:

1. **30 giây**: giới thiệu kiến trúc package và hai role.
2. **1 phút**: login admin, chỉ ra menu role và JWT/Swagger.
3. **2 phút**: kho sách: search, pagination, thêm/sửa, ảnh bìa.
4. **1 phút**: import `books-import.csv`; nêu rollback với file invalid.
5. **1 phút**: quản lý member: search nhiều điều kiện và tạo member.
6. **2 phút**: login User, mượn sách; quay Admin xem lịch sử; trả sách.
7. **1 phút**: maintenance mode trả 503 và admin tắt lại.
8. **1 phút**: Mailpit verify/reset email, i18n, 75 test, Swagger/Liquibase.

## 5. Checklist trước khi trình bày

- [ ] Docker PostgreSQL và Mailpit đang chạy.
- [ ] Ứng dụng mở được tại `http://localhost:8080`.
- [ ] Mailpit mở được tại `http://localhost:8025`.
- [ ] Swagger mở được tại `/swagger-ui.html`.
- [ ] Có một User đã verify email để mượn/trả.
- [ ] Có file `books-import.csv` và `books-import-invalid.csv`.
- [ ] Có ít nhất một sách còn tồn kho.
- [ ] Terminal log đang mở.
- [ ] Không để lộ JWT, Google Client Secret, Gmail App Password khi share màn
  hình.
- [ ] Đã tắt maintenance mode sau khi demo.
- [ ] Chuẩn bị sẵn câu trả lời cho các mục **MỘT PHẦN** trong audit.

## 6. Câu kết thúc gợi ý

> Qua các luồng trên, hệ thống đáp ứng các nghiệp vụ thư viện chính từ xác
> thực, quản lý dữ liệu, mượn–trả đến vận hành hệ thống. Phần source được chia
> theo nghiệp vụ, các thay đổi dữ liệu có transaction, schema có Liquibase,
> API có Swagger và các luồng chính có integration test. Những điểm còn lại
> như role USER khóa tuyệt đối, combobox sách cho search member và catch-all
> exception đã được em kiểm toán rõ để tiếp tục hoàn thiện.
