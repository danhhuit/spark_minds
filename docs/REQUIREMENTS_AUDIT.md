# Báo cáo kiểm toán yêu cầu SparkMinds Library

Ngày kiểm tra: 25/08/2026  
Phạm vi: toàn bộ mã nguồn trong `src/main`, migration trong
`src/main/resources/db/changelog`, giao diện web và integration test trong
`src/test`.

## 1. Kết luận nhanh

- 22 nhóm yêu cầu đạt.
- 7 nhóm đạt một phần và nên hoàn thiện thêm.
- Không có chức năng nghiệp vụ cốt lõi nào hoàn toàn chưa được triển khai.
- Bộ test đầy đủ đã chạy lại sau khi bổ sung kiểm tra i18n:
  75/75 test đạt, không có failure, error hoặc skipped test.
- Giao diện admin đã được kiểm tra trực tiếp bằng Chrome ở cả tiếng Việt và
  tiếng Anh qua 6 trang: Trang chủ, Kho sách, Mượn & trả, Thành viên,
  Hệ thống và Tài khoản.

Ký hiệu:

- **ĐẠT**: có đủ API/logic/phân quyền hoặc validation/transaction/test phù hợp.
- **MỘT PHẦN**: đã hoạt động nhưng còn chênh lệch so với cách diễn đạt sát
  nghĩa của đề hoặc còn thiếu một lớp bảo vệ/UX.

## 2. Ma trận yêu cầu

| # | Yêu cầu | Trạng thái | Bằng chứng chính |
|---:|---|---|---|
| 1 | Spring Boot + JPA + web/API | **ĐẠT** | `pom.xml`, các package controller/service/repository/entity, `static/index.html` |
| 2 | Admin mặc định `admin/admin` | **ĐẠT** | `application.yml`, `AdminDataInitializer.java` |
| 3 | Spring Security + JWT | **ĐẠT** | `SecurityConfig.java`, `JwtConfig.java`, `JwtTokenService.java` |
| 4 | Logout chặn lại access token đã logout | **ĐẠT** | `AuthService.logout`, `RevokedTokenService`, `RevokedTokenValidator` |
| 5 | Đăng ký email/pass và xác minh email trước login | **ĐẠT** | `RegisterRequest`, `RegistrationService`, `MailService` |
| 6 | Phân quyền tách biệt Admin/User | **MỘT PHẦN** | API admin được khóa tốt; API mượn/trả chưa có `hasRole('USER')` |
| 7 | Search sách + phân trang tối đa 10 + Specification | **ĐẠT** | `BookController`, `BookService`, `BookSpecification` |
| 8 | REST CRUD sách + validation | **ĐẠT** | `BookController`, DTO create/update, `BookService` |
| 9 | Import CSV dưới 5 MB, chỉ CSV, rollback | **ĐẠT** | `BookImportController`, `BookCsvImportService`, multipart config |
| 10 | Search member 4–5+ điều kiện, khoảng ngày, chọn sách | **MỘT PHẦN** | Backend có 11 điều kiện; UI sách vẫn là input text, ngày dùng ISO |
| 11 | Tạo/update/deactivate member | **ĐẠT** | `MemberController`, `MemberService`, DTO validation |
| 12 | Mượn sách, kiểm tra tồn kho, mỗi người một sách | **ĐẠT** | `BorrowingService.borrowBook` |
| 13 | Trả sách và hoàn kho | **ĐẠT** | `BorrowingService.returnBook` |
| 14 | Reset password | **ĐẠT** | `PasswordService`, password reset token và mail |
| 15 | Change password | **ĐẠT** | `PasswordService.changePassword` |
| 16 | Change email bằng mã xác minh email mới | **ĐẠT** | `EmailChangeService`, request/verify API |
| 17 | Maintenance mode chặn toàn bộ API | **MỘT PHẦN** | Có chặn 503; login, social exchange và config được miễn có chủ ý |
| 18 | One-to-One, One-to-Many, Many-to-Many | **ĐẠT** | `UserAccount`, `MemberProfile`, `Book`, `Author`, `Category` |
| 19 | Lombok bắt buộc | **ĐẠT** | Dùng `@Getter`, `@Setter`, `@RequiredArgsConstructor`, `@Log4j2`... |
| 20 | CRUD có transaction/rollback | **ĐẠT** | Các service nghiệp vụ dùng `@Transactional` |
| 21 | Log4j request/response, console/file | **MỘT PHẦN** | Đủ metadata và AOP; không log body request/response |
| 22 | Handle exception chuẩn trước client | **MỘT PHẦN** | Có nhiều handler cụ thể nhưng chưa có catch-all |
| 23 | AOP | **ĐẠT** | `ControllerLoggingAspect` |
| 24 | Refresh token tự tạo token mới | **ĐẠT** | Backend rotate token; frontend tự retry khi 401 |
| 25 | JUnit cho API | **ĐẠT** | 13 nhóm integration test, bao phủ API chính |
| 26 | Liquibase migration | **ĐẠT** | 16 changeset trong master changelog |
| 27 | API documentation | **ĐẠT** | Springdoc/OpenAPI + Swagger UI |
| 28 | Clean code, convention, comment | **MỘT PHẦN** | Cấu trúc package tốt; còn import/format chưa đồng nhất |
| 29 | Giao diện web | **ĐẠT** | SPA responsive trong `static`, có màn hình theo role |
| 30 | Chuyển ngữ VI/EN toàn giao diện | **ĐẠT** | Catalog tĩnh, thông báo API, chuỗi động, attribute và locale formatter |

## 3. Kiểm tra chi tiết theo luồng

### 3.1. Login admin, JWT và phân quyền

Luồng:

1. `AdminDataInitializer` đọc cấu hình `app.admin.*`.
2. Nếu chưa có username admin, hệ thống tạo tài khoản, mã hóa password,
   gán role `ADMIN`, bật account và đánh dấu email đã xác minh.
3. `POST /api/auth/login` nhận `LoginRequest`.
4. `AuthService` gọi `AuthenticationManager`, tạo JWT access token và
   refresh token.
5. `JwtAuthenticationConverter` đọc claim `roles`.
6. `SecurityConfig` khóa `/api/admin/**` bằng role `ADMIN`.

File liên quan:

- `src/main/resources/application.yml`
- `src/main/java/com/sparkminds/library/config/AdminDataInitializer.java`
- `src/main/java/com/sparkminds/library/auth/controller/AuthController.java`
- `src/main/java/com/sparkminds/library/auth/service/AuthService.java`
- `src/main/java/com/sparkminds/library/security/SecurityConfig.java`
- `src/main/java/com/sparkminds/library/security/jwt/JwtTokenService.java`
- `src/main/java/com/sparkminds/library/security/service/CustomUserDetailsService.java`

Điểm còn thiếu nếu chấm phân quyền thật chặt:

- `BorrowingController`, `ProfileController` và `SavedBookController` chỉ yêu
  cầu authenticated, chưa yêu cầu riêng `ROLE_USER`.
- Vì vậy ADMIN vẫn có thể gọi một số API vốn được mô tả là của User, dù giao
  diện admin không hiện nút mượn sách.
- Nên thêm `@PreAuthorize("hasRole('USER')")` cho thao tác mượn/lưu sách nếu
  đề bắt buộc hai role không được dùng chéo.

### 3.2. Logout và refresh token

Logout không chỉ xóa token phía trình duyệt:

1. `POST /api/auth/logout` nhận JWT hiện tại và refresh token.
2. Refresh token được đánh dấu revoked nếu thuộc đúng user.
3. `jti` của access token được ghi vào bảng `revoked_tokens`.
4. `RevokedTokenValidator` kiểm tra bảng này mỗi khi JWT được xác thực.
5. Token access đã logout sẽ không dùng lại được.

Refresh:

1. `RefreshTokenService.rotate` chỉ lấy token chưa revoked.
2. Kiểm tra hạn dùng và trạng thái account/email/lock.
3. Thu hồi refresh token cũ và sinh refresh token thay thế.
4. Frontend phát hiện HTTP 401, gọi `/api/auth/refresh`, lưu cặp token mới,
   rồi tự gửi lại request ban đầu một lần.

File liên quan:

- `AuthService.java`
- `RefreshTokenService.java`
- `RevokedTokenService.java`
- `RevokedTokenValidator.java`
- `RefreshToken.java`
- `RevokedToken.java`
- `static/assets/js/app.js`
- migration `005-create-auth-token-tables.yaml`

### 3.3. Đăng ký và xác minh email

`RegisterRequest` kiểm tra:

- email bắt buộc, đúng định dạng, tối đa 255 ký tự;
- password bắt buộc và phải có chữ hoa, chữ thường, số, ký tự đặc biệt;
- password dài 8–72 ký tự.

`RegistrationService`:

- từ chối email trùng;
- hash password bằng `PasswordEncoder`;
- tạo account ở trạng thái `enabled=false`, `emailVerified=false`;
- tạo member profile;
- tạo token xác minh dạng hash;
- gửi link xác minh qua `MailService`;
- chỉ bật account sau khi token hợp lệ được sử dụng.

File liên quan:

- `RegisterRequest.java`
- `RegistrationService.java`
- `EmailVerificationToken.java`
- `EmailVerificationTokenRepository.java`
- `MailService.java`
- `007-create-email-verification-tokens.yaml`
- `AccountLifecycleIntegrationTest.java`

### 3.4. Reset/change password và change email

Reset password:

- forgot-password không làm lộ email có tồn tại hay không;
- token reset có hạn, chỉ dùng một lần và lưu dạng hash;
- password mới không được trùng password cũ;
- sau reset, các refresh token cũ bị vô hiệu.

Change password:

- yêu cầu JWT;
- kiểm tra password hiện tại;
- validate password mới;
- buộc đăng nhập lại sau khi đổi.

Change email:

- email mới phải khác email hiện tại và không trùng;
- sinh mã 6 chữ số gửi tới email mới;
- mã có thời hạn và giới hạn số lần thử;
- chỉ thay email khi nhập đúng mã;
- yêu cầu đăng nhập lại bằng email mới.

File liên quan:

- `PasswordService.java`
- `EmailChangeService.java`
- các DTO trong `auth/dto/request`
- `PasswordResetToken.java`
- `EmailChangeVerification.java`
- migration 008 và 009
- `AccountLifecycleIntegrationTest.java`

### 3.5. Quản lý sách

Search:

- hỗ trợ keyword, ISBN, title, publisher, category, author, khoảng ngày xuất
  bản, trạng thái active và available;
- dùng `JpaSpecificationExecutor`/`BookSpecification`;
- controller có `@Max(10)`;
- service tiếp tục dùng `Math.min(size, 10)` để phòng thủ hai lớp;
- sort field có allow-list, không truyền thẳng field tùy ý vào JPA.

CRUD:

- POST, PUT, DELETE theo REST;
- POST/PUT/DELETE chỉ ADMIN;
- DTO validate ISBN, title, description, publisher, ngày xuất bản,
  total quantity, category và authors;
- ISBN không được trùng;
- không thể giảm total quantity thấp hơn số bản đang mượn;
- delete là soft delete và từ chối nếu đang có bản được mượn.

CSV:

- endpoint multipart chỉ ADMIN;
- kiểm tra file tồn tại, tối đa 5 MB và phần mở rộng `.csv`;
- kiểm tra đủ header;
- kiểm tra dòng trống, duplicate ISBN trong file và lỗi từng dòng;
- `@Transactional(rollbackFor = Exception.class)` bảo đảm lỗi một dòng sẽ
  rollback toàn bộ import.

File liên quan:

- `BookController.java`
- `BookImportController.java`
- `BookService.java`
- `BookCsvImportService.java`
- `BookSpecification.java`
- DTO create/update/search
- `BookControllerIntegrationTest.java`
- `BookCsvImportIntegrationTest.java`

### 3.6. Quản lý member

Backend hiện có các tiêu chí:

1. keyword tổng hợp;
2. fullName search-like;
3. email;
4. membershipCode;
5. bookId;
6. bookTitle search-like;
7. dateOfBirthFrom;
8. dateOfBirthTo;
9. enabled;
10. emailVerified;
11. accountNonLocked;
12. role.

`MemberSpecification` join sang user, roles, borrowings và books; khoảng ngày
dùng `greaterThanOrEqualTo`/`lessThanOrEqualTo`. Service kiểm tra from không
được sau to. Phân trang tối đa 10 và chỉ ADMIN được truy cập.

Điểm còn thiếu ở web:

- Backend hỗ trợ `bookId`, nhưng form hiện dùng
  `<input name="bookTitle">`, chưa load danh sách sách vào combobox như đề.
- `input type="date"` gửi định dạng ISO `yyyy-MM-dd`; đề ghi `yyyy/MM/dd`.
  Trình duyệt đã ngăn phần lớn ngày sai nhưng chưa đúng format hiển thị literal
  của đề.

File liên quan:

- `MemberController.java`
- `MemberService.java`
- `MemberSearchRequest.java`
- `MemberSpecification.java`
- `CreateMemberRequest.java`
- `UpdateMemberRequest.java`
- `MemberControllerIntegrationTest.java`
- phần `renderMembersPage` trong `static/assets/js/app.js`

### 3.7. Mượn và trả sách

Mượn:

- lock member và book khi cập nhật;
- kiểm tra account enabled, email verified, account non-locked;
- kiểm tra user chưa có lượt mượn đang hoạt động;
- kiểm tra sách active và `availableQuantity > 0`;
- tạo thời gian mượn/hạn trả;
- trừ tồn kho trong cùng transaction.

Quy tắc hiện tại là mỗi member chỉ được mượn **một cuốn đang hoạt động tại một
thời điểm**, chặt hơn câu “mỗi người chỉ được mượn 1 loại sách”.

Trả:

- chỉ owner hoặc ADMIN được trả;
- không thể trả lần hai;
- lock lượt mượn/book;
- ghi `returnedAt`;
- cộng lại tồn kho trong cùng transaction.

API response và giao diện lịch sử đều có:

- người mượn;
- ngày mượn;
- hạn trả;
- ngày trả;
- giờ, phút, giây qua `formatDateTime`.

File liên quan:

- `BorrowingController.java`
- `AdminBorrowingController.java`
- `BorrowingService.java`
- `BorrowingRepository.java`
- `Borrowing.java`
- `BorrowingResponse.java`
- `BorrowingMapper.java`
- `BorrowingControllerIntegrationTest.java`

### 3.8. Maintenance mode

`SystemConfigController` có:

- GET cấu hình hiện tại;
- PUT bật/tắt maintenance và cập nhật message;
- chỉ ADMIN.

`MaintenanceModeFilter` trả HTTP 503 cho API khi maintenance bật.

Khác biệt có chủ ý so với câu “chặn tất cả API”:

- `/api/auth/login` được phép để admin đăng nhập;
- `/api/auth/social/exchange` được miễn;
- `/api/admin/system-config/**` được phép để admin tắt maintenance;
- static web không bị chặn.

Đây là thiết kế có thể vận hành được. Nếu chặn tuyệt đối tất cả API thì admin
không thể đăng nhập/tắt bảo trì bằng chính ứng dụng.

File liên quan:

- `SystemConfigController.java`
- `SystemConfigService.java`
- `MaintenanceModeFilter.java`
- `SystemConfig.java`
- `SystemConfigControllerIntegrationTest.java`
- migration 012

### 3.9. Quan hệ Hibernate

- One-to-One: `UserAccount.profile` ↔ `MemberProfile.user`.
- One-to-Many: member ↔ borrowings, book ↔ borrowings,
  category ↔ books.
- Many-to-Many: user ↔ roles, book ↔ authors.
- Many-to-One: borrowing → member/book và các bảng token → user.

Yêu cầu về đủ ba dạng relationship đã đạt.

### 3.10. Transaction

Các thao tác create/update/delete/import/borrow/return/auth/account đều nằm
trong service có `@Transactional`. Query dùng `readOnly=true` khi phù hợp.
CSV dùng `rollbackFor=Exception.class`.

Không đặt transaction ở controller, đúng phân lớp service.

### 3.11. Log4j2 và AOP

Đã có:

- `log4j2-spring.xml`;
- console appender;
- rolling file `logs/library-management.log`;
- archive theo ngày/kích thước;
- request ID bằng `ThreadContext`;
- `HttpLoggingFilter` log method, path, remote address, status, duration;
- `ControllerLoggingAspect` log function call/return/error;
- che password/token/PII trong arguments.

Đánh dấu một phần vì đề có thể được hiểu là phải log body request và response.
Hiện code cố ý chỉ log loại dữ liệu và metadata để tránh lộ password, token,
email và dữ liệu cá nhân. Đây là lựa chọn an toàn hơn, nhưng nên giải thích
trong phần bảo vệ đồ án.

### 3.12. Exception handling

`GlobalExceptionHandler` đã xử lý:

- authentication;
- resource conflict/not found;
- verification/reset/refresh/change-email/social token;
- password mismatch/reuse;
- business exception;
- DTO validation;
- constraint violation;
- CSV import;
- upload quá dung lượng.

Khoảng trống:

- chưa có `@ExceptionHandler(Exception.class)` làm lớp cuối;
- lỗi mail/runtime ngoài danh sách có thể trả 500 mặc định;
- `AccessDeniedException` chưa được chuẩn hóa thành cùng `ApiErrorResponse`;
- format/indentation của file chưa theo convention thống nhất.

Đây là hạng mục nên sửa ưu tiên cao vì người dùng từng gặp thông báo 500 chung
khi cấu hình SMTP sai.

### 3.13. JUnit

Các nhóm test:

- auth/login/register: `AuthControllerIntegrationTest`;
- verify/reset/change email/change password:
  `AccountLifecycleIntegrationTest`;
- logout/revoke/refresh: `TokenLifecycleIntegrationTest`;
- CRUD/search/role sách: `BookControllerIntegrationTest`;
- CSV: `BookCsvImportIntegrationTest`;
- seed 50 sách: `CatalogSeedIntegrationTest`;
- borrow/return/rule/concurrency: `BorrowingControllerIntegrationTest`;
- member: `MemberControllerIntegrationTest`;
- profile: `ProfileControllerIntegrationTest`;
- saved books: `SavedBookControllerIntegrationTest`;
- maintenance: `SystemConfigControllerIntegrationTest`;
- OpenAPI: `DocumentationIntegrationTest`;
- static web/Google redirect/i18n catalog:
  `FrontendIntegrationTest`.

Khoảng trống không thuộc đề gốc nhưng liên quan tính năng mới:

- chưa có integration test hoàn chỉnh cho Google callback → one-time social
  code → JWT exchange;
- test Google hiện xác nhận endpoint redirect tới Google.

### 3.14. Liquibase và API docs

Liquibase master gồm 16 changeset:

- roles, accounts, profile;
- auth token, email verify, password reset, email change;
- book/category/author và borrowings;
- system config, saved books;
- seed 50 sách;
- Google OAuth identity và social login code.

OpenAPI:

- `OpenApiConfig` khai báo bearer JWT;
- controllers có tag/operation/security requirement;
- Swagger UI public tại `/swagger-ui.html`;
- JSON spec tại `/v3/api-docs`;
- `DocumentationIntegrationTest` kiểm tra tài liệu được sinh.

Swagger là tài liệu kỹ thuật tiếng Anh, không đổi theo lựa chọn ngôn ngữ của
SPA. Phần này không phải nội dung giao diện ứng dụng.

## 4. Kiểm tra chuyển ngữ VI/EN

### Phạm vi đã xử lý

`static/assets/js/app.js` hiện có bốn lớp:

1. `EN_TEXT`: copy tiếng Việt tĩnh → tiếng Anh;
2. `VI_TEXT`: reverse map để đổi ngược không cần reload;
3. `API_TEXT_VI`: thông báo/validation backend tiếng Anh → tiếng Việt;
4. pattern động cho số lượng, phân trang, tên resource, CSV, due date,
   tiêu đề ảnh bìa và confirm.

`translateTree` xử lý:

- text node mới và cũ;
- `placeholder`;
- `title`;
- `aria-label`;
- modal/view được render động qua `MutationObserver`.

`t()` được dùng cho:

- navigation và page title;
- modal, toast, empty/error state;
- loading button;
- field error từ API;
- category lấy từ database;
- trạng thái sách/member/borrowing;
- thông báo maintenance;
- date/time qua locale `vi-VN` hoặc `en-US`.

Các validation backend đã được bổ sung đầy đủ cho:

- auth/profile/member;
- book create/update;
- CSV;
- pagination;
- social login;
- resource/business error động.

### Kiểm tra trực tiếp

Chrome headless đã đăng nhập `admin/admin`, đổi qua VI và EN, sau đó mở:

- dashboard;
- books;
- borrowings;
- members;
- system;
- account.

Không phát hiện cụm UI tiếng Anh trong chế độ VI hoặc cụm UI tiếng Việt trong
chế độ EN theo bộ từ khóa kiểm tra.

Trang auth cũng được kiểm tra ở cả hai locale.

Ngoại lệ đúng thiết kế:

- “Tiếng Việt” và “English” trong dropdown giữ tên bản địa để người dùng luôn
  nhận biết được ngôn ngữ cần chọn;
- tên sách, tên tác giả, tên người dùng và nội dung do người dùng/database nhập
  không được tự động dịch;
- ISBN, email, role code và thuật ngữ kỹ thuật không phải UI copy.

## 5. Thứ tự nên hoàn thiện tiếp

### Ưu tiên cao

1. Thêm handler cuối cho exception và chuẩn hóa access denied/SMTP error.
2. Nếu giảng viên chấm role tuyệt đối, khóa API borrow/save/profile theo
   `ROLE_USER` hoặc viết rõ policy ADMIN được phép hỗ trợ trả sách.

### Ưu tiên vừa

3. Đổi ô “Tên sách đã mượn” thành combobox lấy từ
   `/api/books?size=10...` hoặc tạo lookup endpoint không phân trang.
4. Quyết định format ngày theo đề `yyyy/MM/dd` hay giữ native date
   `yyyy-MM-dd`; nếu theo đề literal thì dùng text input + formatter/parser.
5. Bổ sung Google end-to-end integration test.

### Chất lượng

6. Chạy formatter toàn bộ Java, dọn duplicate/unused import.
7. Quy định rõ logging chỉ metadata và masked payload trong tài liệu bảo mật.
8. Có thể tách catalog i18n khỏi file `app.js` thành `vi.js`/`en.js` hoặc JSON
   nếu số lượng màn hình tiếp tục tăng.

## 6. Lệnh xác minh

```powershell
node --check src/main/resources/static/assets/js/app.js
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Sau khi ứng dụng chạy:

```text
http://localhost:8080/
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```
