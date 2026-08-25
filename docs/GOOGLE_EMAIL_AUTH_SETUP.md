# Setup đăng ký/đăng nhập bằng Email và Google cho bản demo

Tài liệu áp dụng cho dự án tại `D:\sparkminds\src`.

## 1. Phương án miễn phí nên dùng

| Nhu cầu | Dịch vụ | Chi phí demo | Khi nào dùng |
|---|---|---:|---|
| Database | PostgreSQL trong Docker | Miễn phí | Local |
| Nhận email xác minh | Mailpit trong Docker | Miễn phí | Dev/local |
| Gửi email thật | Gmail SMTP + App Password | Miễn phí trong giới hạn Gmail | Demo cho người khác |
| Đăng nhập Google | Google OAuth 2.0/OpenID Connect | Miễn phí cho luồng đăng nhập demo | Local và production |

Khuyến nghị:

1. Hoàn thiện và test Email bằng Mailpit trước.
2. Chỉ chuyển sang Gmail SMTP khi cần gửi email thật.
3. Sau khi Email hoạt động ổn định mới thêm Google Login.

## PHẦN A — EMAIL + MẬT KHẨU

Luồng này đã có trong dự án:

```text
Đăng ký
  -> validate email/mật khẩu
  -> lưu tài khoản disabled
  -> gửi link xác minh
  -> người dùng bấm link
  -> tài khoản enabled
  -> cho phép đăng nhập
  -> phát hành access token + refresh token
```

### Bước A1 — Kiểm tra Docker

Mở Docker Desktop và chờ trạng thái Docker Engine là Running.

Trong PowerShell:

```powershell
cd D:\sparkminds\src
docker version
docker compose up -d postgres mailpit
docker compose ps
```

Kết quả cần có:

```text
postgres   running   0.0.0.0:5433->5432
mailpit    running   0.0.0.0:1025->1025
                     0.0.0.0:8025->8025
```

Các cổng:

- PostgreSQL: `localhost:5433`.
- Mailpit SMTP: `localhost:1025`.
- Mailpit giao diện đọc thư: `http://localhost:8025`.
- Spring Boot: `http://localhost:8080`.

### Bước A2 — Tạo JWT secret

Mỗi lần mở PowerShell mới, set `JWT_SECRET` trước khi chạy ứng dụng:

```powershell
$jwtBytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($jwtBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
```

Không đưa JWT secret thật vào `application.yml` hoặc GitHub.

### Bước A3 — Kiểm tra cổng 8080

```powershell
$listener = Get-NetTCPConnection `
    -LocalPort 8080 `
    -State Listen `
    -ErrorAction SilentlyContinue
```

Nếu `$listener` có dữ liệu thì ứng dụng đã chạy. Không chạy thêm một bản nữa.

Nếu muốn dừng bản đang chạy:

```powershell
if ($listener) {
    Stop-Process -Id $listener.OwningProcess
}
```

### Bước A4 — Chạy Spring Boot

```powershell
cd D:\sparkminds\src
.\mvnw.cmd spring-boot:run
```

Chờ:

```text
Started LibraryManagementApplication
Tomcat started on port 8080
```

Không đóng cửa sổ PowerShell này trong lúc sử dụng ứng dụng.

### Bước A5 — Đăng ký bằng email

Mở:

```text
http://localhost:8080
```

Chọn `Đăng ký ngay`, sau đó nhập:

```text
Email: demo@example.com
Mật khẩu: Demo@12345
```

Quy tắc mật khẩu hiện tại:

- Từ 8 đến 72 ký tự.
- Có chữ hoa.
- Có chữ thường.
- Có chữ số.
- Có một ký tự trong `@$!%*?&`.

API tương ứng:

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "demo@example.com",
  "password": "Demo@12345"
}
```

Test bằng PowerShell:

```powershell
$registerBody = @{
    email = "demo@example.com"
    password = "Demo@12345"
} | ConvertTo-Json

Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/auth/register" `
    -ContentType "application/json" `
    -Body $registerBody
```

### Bước A6 — Xác minh email bằng Mailpit

1. Mở `http://localhost:8025`.
2. Chọn email gửi tới `demo@example.com`.
3. Mở nội dung email.
4. Bấm link xác minh.
5. Trình duyệt quay về Spark Library.
6. Hệ thống gọi `GET /api/auth/verify-email?token=...`.
7. Tài khoản được chuyển thành `enabled=true` và
   `email_verified=true`.

Mailpit không gửi email ra Internet. Nó giữ email trong máy để test, vì vậy
hoàn toàn miễn phí và không cần tài khoản.

### Bước A7 — Đăng nhập bằng email

Nhập:

```text
Tên đăng nhập hoặc email: demo@example.com
Mật khẩu: Demo@12345
```

API:

```http
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "demo@example.com",
  "password": "Demo@12345"
}
```

PowerShell:

```powershell
$loginBody = @{
    usernameOrEmail = "demo@example.com"
    password = "Demo@12345"
} | ConvertTo-Json

$tokens = Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/auth/login" `
    -ContentType "application/json" `
    -Body $loginBody

$tokens.accessToken
$tokens.refreshToken
```

Nếu chưa xác minh email, login phải bị từ chối. Đây là hành vi đúng.

## PHẦN B — GỬI EMAIL THẬT BẰNG GMAIL SMTP

Chỉ thực hiện phần này nếu người test cần nhận email trong Gmail thật.

### Bước B1 — Chuẩn bị tài khoản Gmail demo

Nên tạo một Gmail riêng cho ứng dụng, ví dụ:

```text
spark.library.demo@gmail.com
```

Không dùng Gmail cá nhân chính.

### Bước B2 — Bật xác minh 2 bước

1. Đăng nhập Google Account.
2. Mở `Security`.
3. Chọn `2-Step Verification`.
4. Hoàn tất bật xác minh hai bước.

### Bước B3 — Tạo App Password

1. Mở `https://myaccount.google.com/apppasswords`.
2. Đặt tên ứng dụng `Spark Library Demo`.
3. Tạo App Password.
4. Sao chép mật khẩu 16 ký tự.

App Password không phải mật khẩu Gmail thông thường.

### Bước B4 — Bổ sung cấu hình SMTP

Trong `src/main/resources/application.yml`, phần `spring.mail` nên là:

```yaml
spring:
  mail:
    host: ${MAIL_HOST:localhost}
    port: ${MAIL_PORT:1025}
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}
    properties:
      mail:
        smtp:
          auth: ${MAIL_SMTP_AUTH:false}
          starttls:
            enable: ${MAIL_STARTTLS_ENABLE:false}
            required: ${MAIL_STARTTLS_REQUIRED:false}
```

Cấu hình này vẫn chạy được với Mailpit khi không set biến Gmail.

### Bước B5 — Set biến môi trường Gmail

Trong PowerShell chạy ứng dụng:

```powershell
$env:MAIL_HOST = "smtp.gmail.com"
$env:MAIL_PORT = "587"
$env:MAIL_USERNAME = "spark.library.demo@gmail.com"
$env:MAIL_PASSWORD = "APP_PASSWORD_16_KY_TU"
$env:MAIL_FROM = "spark.library.demo@gmail.com"
$env:MAIL_SMTP_AUTH = "true"
$env:MAIL_STARTTLS_ENABLE = "true"
$env:MAIL_STARTTLS_REQUIRED = "true"
```

Không ghi App Password vào:

- `application.yml`.
- `.env` được commit.
- README.
- GitHub.
- Ảnh chụp màn hình hoặc log.

### Bước B6 — Khởi động lại và test

```powershell
$listener = Get-NetTCPConnection `
    -LocalPort 8080 `
    -State Listen `
    -ErrorAction SilentlyContinue

if ($listener) {
    Stop-Process -Id $listener.OwningProcess
}

.\mvnw.cmd spring-boot:run
```

Đăng ký bằng một email thật và kiểm tra Inbox/Spam.

Nếu gặp `Username and Password not accepted`:

1. Không dùng mật khẩu Gmail thường.
2. Kiểm tra 2-Step Verification.
3. Tạo App Password mới.
4. Xóa khoảng trắng trong App Password.
5. Kiểm tra đúng `smtp.gmail.com:587` và STARTTLS.

## PHẦN C — ĐĂNG NHẬP/ĐĂNG KÝ BẰNG GOOGLE

Google không cần hai nút “Đăng ký Google” và “Đăng nhập Google”.

Chỉ cần một nút `Tiếp tục với Google`:

- Google chưa liên kết: backend tạo member mới.
- Google đã liên kết: backend đăng nhập member cũ.

### Bước C1 — Tạo Google Cloud project

1. Mở `https://console.cloud.google.com`.
2. Bấm danh sách project ở thanh trên.
3. Chọn `New Project`.
4. Đặt tên `Spark Library Demo`.
5. Chọn `Create`.
6. Chọn lại project vừa tạo.

Không cần bật billing chỉ để dùng đăng nhập Google cơ bản.

### Bước C2 — Cấu hình Google Auth Platform

Trong Google Cloud Console:

1. Mở `Google Auth Platform`.
2. Chọn `Get Started`.
3. App name: `Spark Library`.
4. User support email: Gmail của bạn.
5. Audience: `External`.
6. Contact information: Gmail của bạn.
7. Đồng ý chính sách và hoàn tất.

Trong `Audience`:

1. Giữ trạng thái `Testing`.
2. Thêm Gmail của bạn vào `Test users`.
3. Thêm Gmail của những người sẽ test demo.

Khi ở Testing, chỉ Test users được đăng nhập. Với bản demo đây là lựa chọn
dễ và miễn phí nhất.

### Bước C3 — Cấu hình scope

Trong `Data Access`, chỉ dùng:

```text
openid
profile
email
```

Không xin Drive, Gmail, Calendar hoặc scope nhạy cảm vì ứng dụng chỉ cần xác
định danh người dùng.

### Bước C4 — Tạo OAuth Client

1. Mở `Google Auth Platform` → `Clients`.
2. Chọn `Create Client`.
3. Application type: `Web application`.
4. Name: `Spark Library Local`.
5. Authorized JavaScript origins:

```text
http://localhost:8080
```

6. Authorized redirect URIs:

```text
http://localhost:8080/login/oauth2/code/google
```

7. Chọn `Create`.
8. Sao chép:
   - Client ID.
   - Client Secret.

Redirect URI phải giống tuyệt đối, gồm `http`, port `8080` và toàn bộ path.

### Bước C5 — Thêm OAuth2 Client dependency

Trong `pom.xml`, thêm:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

Sau đó:

```powershell
.\mvnw.cmd dependency:tree
```

### Bước C6 — Cấu hình Google Client

Thêm dưới `spring` trong `application.yml`:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID:}
            client-secret: ${GOOGLE_CLIENT_SECRET:}
            scope:
              - openid
              - profile
              - email
```

Không cần hard-code authorization URI và token URI vì Spring Security có
cấu hình mặc định cho provider Google.

Set biến môi trường:

```powershell
$env:GOOGLE_CLIENT_ID = "CLIENT_ID.apps.googleusercontent.com"
$env:GOOGLE_CLIENT_SECRET = "GOOGLE_CLIENT_SECRET"
```

### Bước C7 — Thiết kế database liên kết Google

Không dùng email làm định danh Google duy nhất. Google cung cấp claim `sub`
ổn định cho từng người dùng/client.

Tạo Liquibase changeset:

```text
src/main/resources/db/changelog/changes/015-create-oauth-identities.yaml
```

Bảng đề xuất:

```yaml
databaseChangeLog:
  - changeSet:
      id: 015-create-oauth-identities
      author: danh
      changes:
        - createTable:
            tableName: oauth_identities
            columns:
              - column:
                  name: id
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: user_id
                  type: BIGINT
                  constraints:
                    nullable: false
              - column:
                  name: provider
                  type: VARCHAR(30)
                  constraints:
                    nullable: false
              - column:
                  name: provider_subject
                  type: VARCHAR(255)
                  constraints:
                    nullable: false
              - column:
                  name: created_at
                  type: TIMESTAMP WITH TIME ZONE
                  defaultValueComputed: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false

        - addForeignKeyConstraint:
            baseTableName: oauth_identities
            baseColumnNames: user_id
            referencedTableName: user_accounts
            referencedColumnNames: id
            constraintName: fk_oauth_identities_user
            onDelete: CASCADE

        - addUniqueConstraint:
            tableName: oauth_identities
            columnNames: provider, provider_subject
            constraintName: uk_oauth_provider_subject

        - addUniqueConstraint:
            tableName: oauth_identities
            columnNames: user_id, provider
            constraintName: uk_oauth_user_provider
```

Thêm vào cuối `db.changelog-master.yaml`:

```yaml
  - include:
      file: changes/015-create-oauth-identities.yaml
      relativeToChangelogFile: true
```

### Bước C8 — Tạo entity/repository Google identity

Tạo:

```text
auth/entity/OAuthIdentity.java
auth/repository/OAuthIdentityRepository.java
```

Repository cần:

```java
Optional<OAuthIdentity> findByProviderAndProviderSubject(
        String provider,
        String providerSubject
);
```

### Bước C9 — Tạo Google user service

Tạo:

```text
auth/oauth/GoogleOidcUserService.java
```

Service xử lý:

1. Gọi `OidcUserService` mặc định.
2. Đọc:
   - `sub`.
   - `email`.
   - `email_verified`.
   - `name`.
3. Từ chối nếu không có email hoặc `email_verified=false`.
4. Tìm `oauth_identities` bằng `GOOGLE + sub`.
5. Nếu có: tải `UserAccount`.
6. Nếu chưa có nhưng email đã tồn tại:
   - Liên kết Google với tài khoản email đó.
7. Nếu email chưa tồn tại:
   - Tạo `UserAccount`.
   - Username và email bằng email Google.
   - Password là chuỗi BCrypt ngẫu nhiên không sử dụng.
   - `enabled=true`.
   - `emailVerified=true`.
   - Gắn role `USER`.
   - Tạo `MemberProfile` và membership code.
   - Lưu `OAuthIdentity`.
8. Không lấy role từ Google.

Toàn bộ quá trình tạo/liên kết tài khoản phải chạy trong một transaction.

### Bước C10 — Phát hành JWT của Spark Library

Google chỉ xác minh danh tính. Sau callback, hệ thống vẫn phải phát hành:

```text
Spark Library access token
Spark Library refresh token
```

Trong `AuthService`, tách phương thức:

```java
@Transactional
public TokenResponse issueTokens(UserAccount user) {
    CustomUserPrincipal principal =
            CustomUserPrincipal.from(user);

    RefreshTokenService.IssuedRefreshToken refresh =
            refreshTokenService.issueForUser(user.getId());

    return createResponse(
            principal,
            refresh.value(),
            refresh.expiresAt()
    );
}
```

Đăng nhập email và đăng nhập Google cùng dùng phương thức phát token này.

### Bước C11 — Không đưa JWT vào redirect URL

Không redirect kiểu:

```text
/?accessToken=...&refreshToken=...
```

Thay vào đó:

1. Backend tạo một `socialCode` ngẫu nhiên, dùng một lần.
2. Chỉ lưu SHA-256 của code trong database.
3. Code hết hạn sau 2 phút.
4. Redirect:

```text
http://localhost:8080/?socialCode=RAW_CODE
```

5. Frontend gọi:

```http
POST /api/auth/social/exchange

{
  "code": "RAW_CODE"
}
```

6. Backend đánh dấu code đã dùng và trả `TokenResponse`.

Cần thêm bảng `social_login_codes` và dọn code hết hạn định kỳ.

### Bước C12 — Cấu hình Spring Security

Trong `SecurityConfig`:

1. Cho phép:

```text
/oauth2/**
/login/oauth2/**
POST /api/auth/social/exchange
```

2. Thêm:

```java
.oauth2Login(oauth2 -> oauth2
    .userInfoEndpoint(userInfo -> userInfo
        .oidcUserService(googleOidcUserService))
    .successHandler(googleLoginSuccessHandler)
    .failureHandler(googleLoginFailureHandler)
)
```

3. OAuth authorization request mặc định cần session tạm:

```java
.sessionManagement(session -> session
    .sessionCreationPolicy(
        SessionCreationPolicy.IF_REQUIRED
    )
)
```

4. Sau khi tạo `socialCode`, success handler phải invalidate session.

API nghiệp vụ vẫn dùng JWT Bearer như hiện tại.

### Bước C13 — Thêm nút Google vào frontend

Trong form đăng nhập:

```html
<a class="button button--secondary button--full"
   href="/oauth2/authorization/google">
    Tiếp tục với Google
</a>
```

Không cần JavaScript để bắt đầu OAuth; điều hướng trực tiếp giúp Spring
Security quản lý `state` và callback.

### Bước C14 — Exchange socialCode ở frontend

Khi trang được mở:

```javascript
const parameters = new URLSearchParams(location.search);
const socialCode = parameters.get("socialCode");

if (socialCode) {
    const response = await fetch("/api/auth/social/exchange", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({code: socialCode})
    });

    const tokens = await response.json();
    storeTokens(tokens);

    history.replaceState({}, "", "/");
}
```

Nếu exchange thất bại, xóa code khỏi URL và hiển thị thông báo đăng nhập lại.

### Bước C15 — Chạy thử

Set toàn bộ biến trong cùng cửa sổ PowerShell:

```powershell
$env:GOOGLE_CLIENT_ID = "..."
$env:GOOGLE_CLIENT_SECRET = "..."

$jwtBytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($jwtBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)

.\mvnw.cmd spring-boot:run
```

Mở:

```text
http://localhost:8080
```

Bấm `Tiếp tục với Google`, chọn một tài khoản nằm trong Test users.

### Bước C16 — Các lỗi Google thường gặp

#### `redirect_uri_mismatch`

URI trong Google Console phải chính xác:

```text
http://localhost:8080/login/oauth2/code/google
```

Không dùng nhầm:

```text
http://localhost:8080/
http://localhost:8080/oauth2/google
https://localhost:8080/login/oauth2/code/google
```

#### `Access blocked: app has not completed verification`

- Giữ app ở Testing.
- Thêm Gmail đang dùng vào Test users.
- Chỉ dùng scope `openid profile email`.

#### Trang trả về 401 sau khi Google đăng nhập

- Chưa exchange `socialCode` thành JWT.
- Success handler chưa phát hành token của Spark Library.
- Session bị xóa trước khi tạo socialCode.

#### Port 8080 already in use

```powershell
$listener = Get-NetTCPConnection `
    -LocalPort 8080 `
    -State Listen

Stop-Process -Id $listener.OwningProcess
.\mvnw.cmd spring-boot:run
```

## PHẦN D — TEST BẮT BUỘC

### Email

1. Email sai format trả 400.
2. Mật khẩu yếu trả 400.
3. Email trùng trả 409.
4. Chưa verify không đăng nhập được.
5. Token verify đúng kích hoạt tài khoản.
6. Token verify hết hạn hoặc dùng lần hai bị từ chối.
7. Login đúng trả access/refresh token.

### Google

1. Google email chưa tồn tại tạo member USER.
2. Google email đã tồn tại liên kết đúng tài khoản.
3. Cùng `sub` không tạo hai tài khoản.
4. `email_verified=false` bị từ chối.
5. Google không thể tự cấp role ADMIN.
6. Social code hết hạn bị từ chối.
7. Social code chỉ dùng được một lần.
8. Refresh/logout hoạt động như tài khoản email.

Chạy:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd clean package
```

## Thứ tự thực hiện thực tế

Không làm tất cả cùng lúc. Thực hiện theo thứ tự:

```text
1. Email + Mailpit
2. Email + Gmail SMTP nếu cần
3. Google Cloud project
4. Google OAuth Client
5. Dependency + application.yml
6. Liquibase OAuth identity
7. GoogleOidcUserService
8. Social code exchange
9. SecurityConfig
10. Nút Google
11. Integration test
```

## Tài liệu chính thức

- Spring Security OAuth2 Login:
  https://docs.spring.io/spring-security/reference/servlet/oauth2/login/
- Spring Security Google setup:
  https://docs.spring.io/spring-security/reference/7.0/servlet/oauth2/login/core.html
- Google OAuth for web server:
  https://developers.google.com/identity/protocols/oauth2/web-server
- Google OpenID Connect:
  https://developers.google.com/identity/openid-connect/openid-connect
- Google App Password:
  https://support.google.com/accounts/answer/185833
