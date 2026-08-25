# Setup đăng ký/đăng nhập bằng SĐT, Google và Facebook

Tài liệu này áp dụng cho kiến trúc hiện tại của Spark Library:

- Spring Boot + Spring Security.
- API nghiệp vụ xác thực bằng access token JWT của ứng dụng.
- Refresh token được lưu, xoay vòng và thu hồi khi logout.
- Giao diện tĩnh nằm trong `src/main/resources/static`.

Nguyên tắc quan trọng: Google, Facebook và nhà cung cấp SMS chỉ xác minh
danh tính. Sau khi xác minh thành công, backend vẫn phải phát hành access
token và refresh token của Spark Library. Không dùng access token của Google
hoặc Facebook để gọi các API `/api/**`.

## 1. Thiết kế luồng xác thực

### Số điện thoại

1. Người dùng nhập số điện thoại theo chuẩn E.164, ví dụ `+84901234567`.
2. Frontend gọi `POST /api/auth/phone/request-otp`.
3. Backend yêu cầu Twilio Verify gửi OTP.
4. Người dùng nhập OTP.
5. Frontend gọi `POST /api/auth/phone/verify-otp`.
6. Backend xác minh OTP, tạo hoặc tìm tài khoản, rồi phát hành bộ JWT hiện có.

Không tạo tài khoản trước khi OTP được xác minh thành công.

### Google/Facebook

1. Người dùng bấm `/oauth2/authorization/google` hoặc
   `/oauth2/authorization/facebook`.
2. Spring Security chuyển người dùng sang nhà cung cấp.
3. Nhà cung cấp gọi lại:
   `/login/oauth2/code/{registrationId}`.
4. Success handler tạo hoặc tìm tài khoản và tạo một mã đăng nhập dùng một lần.
5. Backend chuyển hướng về `/?socialCode=...`.
6. Frontend gửi mã tới `POST /api/auth/social/exchange`.
7. Backend đổi mã một lần thành access token và refresh token của Spark Library.

Không đặt access token hoặc refresh token trực tiếp trên query string.

## 2. Tạo nhánh triển khai

```powershell
cd D:\sparkminds\src
git switch -c codex/multi-provider-auth
```

## 3. Thêm dependency

Thêm vào `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<dependency>
    <groupId>com.twilio.sdk</groupId>
    <artifactId>twilio</artifactId>
    <version>12.1.1</version>
</dependency>
```

Sau đó kiểm tra dependency:

```powershell
.\mvnw.cmd dependency:tree
```

## 4. Tạo migration Liquibase `015`

Tạo:

`src/main/resources/db/changelog/changes/015-create-login-identities.yaml`

Migration cần thực hiện:

1. Cho phép `user_accounts.email` nullable.
2. Cho phép `user_accounts.password` nullable.
3. Thêm `phone_number VARCHAR(20)` nullable.
4. Thêm `phone_verified BOOLEAN NOT NULL DEFAULT FALSE`.
5. Tạo unique index cho `phone_number` khi giá trị không null.
6. Tạo bảng `login_identities`:

```text
id                  BIGINT, primary key
user_id             BIGINT, foreign key user_accounts.id
provider            VARCHAR(20)  -- LOCAL, PHONE, GOOGLE, FACEBOOK
provider_subject    VARCHAR(255)
provider_email      VARCHAR(255), nullable
created_at          TIMESTAMP WITH TIME ZONE
updated_at          TIMESTAMP WITH TIME ZONE
```

Ràng buộc cần có:

```text
UNIQUE(provider, provider_subject)
UNIQUE(user_id, provider)
```

7. Tạo bảng `social_login_codes`:

```text
id          BIGINT, primary key
user_id     BIGINT, foreign key user_accounts.id
code_hash   VARCHAR(64), unique
expires_at  TIMESTAMP WITH TIME ZONE
used        BOOLEAN NOT NULL DEFAULT FALSE
used_at     TIMESTAMP WITH TIME ZONE, nullable
created_at  TIMESTAMP WITH TIME ZONE
```

Thêm changelog vào cuối `db.changelog-master.yaml`:

```yaml
  - include:
      file: changes/015-create-login-identities.yaml
      relativeToChangelogFile: true
```

Không dùng `ddl-auto=update`; dự án đang dùng `ddl-auto=validate`.

## 5. Bổ sung entity và repository

Tạo package:

```text
auth/entity/LoginIdentity.java
auth/entity/LoginProvider.java
auth/entity/SocialLoginCode.java
auth/repository/LoginIdentityRepository.java
auth/repository/SocialLoginCodeRepository.java
```

`LoginProvider`:

```java
public enum LoginProvider {
    LOCAL,
    PHONE,
    GOOGLE,
    FACEBOOK
}
```

Các query repository tối thiểu:

```java
Optional<LoginIdentity> findByProviderAndProviderSubject(
        LoginProvider provider,
        String providerSubject
);

Optional<SocialLoginCode> findByCodeHashAndUsedFalse(
        String codeHash
);
```

Trong `UserAccount`, thêm:

```java
private String phoneNumber;
private boolean phoneVerified;
```

`email` và `password` phải cho phép null ở cả entity và database. Luồng đăng
nhập mật khẩu chỉ được chạy khi tài khoản có mật khẩu.

## 6. Tách chức năng phát hành token dùng chung

Hiện `AuthService.login()` mới phát hành token sau khi xác thực mật khẩu.
Tách phần đó thành phương thức dùng chung:

```java
@Transactional
public TokenResponse issueTokens(UserAccount user) {
    CustomUserPrincipal principal = CustomUserPrincipal.from(user);
    RefreshTokenService.IssuedRefreshToken refresh =
            refreshTokenService.issueForUser(user.getId());

    return createResponse(
            principal,
            refresh.value(),
            refresh.expiresAt()
    );
}
```

Cả đăng nhập mật khẩu, OTP và social login đều gọi phương thức này. Giữ nguyên
cơ chế refresh/logout hiện có để token vẫn được xoay vòng và thu hồi.

Nếu email nullable, `JwtTokenService` chỉ thêm claim `email` khi email tồn tại.
Thêm claim `phone` khi số điện thoại tồn tại.

## 7. Setup Twilio Verify cho OTP

1. Tạo tài khoản Twilio.
2. Trong Twilio Console, tạo một Verify Service.
3. Ghi lại:
   - Account SID.
   - Auth Token hoặc API Key/Secret.
   - Verify Service SID, bắt đầu bằng `VA`.
4. Với tài khoản trial, xác minh số điện thoại nhận thử nghiệm.
5. Không commit các giá trị này vào Git.

Thêm vào `application.yml`:

```yaml
app:
  phone-auth:
    account-sid: ${TWILIO_ACCOUNT_SID:}
    auth-token: ${TWILIO_AUTH_TOKEN:}
    verify-service-sid: ${TWILIO_VERIFY_SERVICE_SID:}
```

Set biến môi trường PowerShell:

```powershell
$env:TWILIO_ACCOUNT_SID = "AC..."
$env:TWILIO_AUTH_TOKEN = "..."
$env:TWILIO_VERIFY_SERVICE_SID = "VA..."
```

Tạo DTO:

```java
public record PhoneOtpRequest(
        @NotBlank
        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$")
        String phoneNumber
) {}

public record PhoneOtpVerifyRequest(
        @NotBlank
        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$")
        String phoneNumber,

        @NotBlank
        @Pattern(regexp = "^\\d{4,10}$")
        String code
) {}
```

Tạo `PhoneAuthService`:

- `requestOtp(phone)`: gọi Twilio
  `Verification.creator(serviceSid, phone, "sms").create()`.
- `verifyOtp(phone, code)`: gọi
  `VerificationCheck.creator(serviceSid).setTo(phone).setCode(code).create()`.
- Chỉ chấp nhận trạng thái `approved`.
- Sau khi approved, tạo/tìm `UserAccount`, gắn role `USER`, tạo
  `MemberProfile`, đặt `phoneVerified=true`, rồi gọi `AuthService.issueTokens`.
- Chuẩn hóa mọi số điện thoại về E.164 trước khi tìm hoặc lưu.

Tạo endpoint:

```text
POST /api/auth/phone/request-otp  -> MessageResponse
POST /api/auth/phone/verify-otp   -> TokenResponse
```

Thêm hai endpoint này vào `permitAll()` trong `SecurityConfig`.

Yêu cầu bảo mật:

- Giới hạn gửi OTP theo IP và số điện thoại.
- Thời gian chờ gửi lại ít nhất 30–60 giây.
- Trả thông báo chung, không tiết lộ số điện thoại đã có tài khoản hay chưa.
- Không log OTP, Auth Token hoặc toàn bộ số điện thoại.
- Chặn thử OTP liên tục và ghi audit log.

## 8. Setup Google Login

1. Mở Google Cloud Console.
2. Tạo hoặc chọn project.
3. Cấu hình OAuth consent screen.
4. Tạo OAuth Client ID, loại `Web application`.
5. Thêm redirect URI chính xác:

```text
http://localhost:8080/login/oauth2/code/google
```

6. Khi deploy, thêm URI HTTPS của production.
7. Lưu Client ID và Client Secret vào biến môi trường.

```powershell
$env:GOOGLE_CLIENT_ID = "..."
$env:GOOGLE_CLIENT_SECRET = "..."
```

## 9. Setup Facebook Login

1. Mở Meta for Developers và tạo app.
2. Thêm use case đăng nhập/xác thực người dùng bằng Facebook.
3. Trong Facebook Login settings, thêm Valid OAuth Redirect URI:

```text
http://localhost:8080/login/oauth2/code/facebook
```

4. Lấy App ID và App Secret.
5. Thêm tài khoản test khi app đang ở Development mode.
6. Trước khi production, cấu hình domain HTTPS, Privacy Policy URL và
   Data Deletion URL theo yêu cầu của Meta.

```powershell
$env:FACEBOOK_CLIENT_ID = "..."
$env:FACEBOOK_CLIENT_SECRET = "..."
```

Email Facebook có thể không được trả về. Luôn định danh tài khoản bằng cặp
`FACEBOOK + providerSubject`, không dùng email làm khóa duy nhất của social
identity.

## 10. Cấu hình OAuth2 Client

Thêm vào `application.yml`:

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
          facebook:
            client-id: ${FACEBOOK_CLIENT_ID:}
            client-secret: ${FACEBOOK_CLIENT_SECRET:}
            scope:
              - public_profile
              - email
```

Spring Boot có cấu hình mặc định cho provider `google` và `facebook`, nên
không cần hard-code authorization URI/token URI trong dự án.

## 11. Xử lý OAuth2 callback

Tạo:

```text
auth/oauth/SocialOAuth2UserService.java
auth/oauth/SocialAuthenticationSuccessHandler.java
auth/service/SocialLoginCodeService.java
```

Trong `SocialOAuth2UserService`:

1. Đọc `registrationId`.
2. Google: lấy `sub`, `email`, `email_verified`, `name`.
3. Facebook: lấy `id`, `email` nếu có, `name`.
4. Tìm `LoginIdentity` bằng `provider + subject`.
5. Nếu chưa tồn tại:
   - Nếu email đã thuộc tài khoản khác, không tự động liên kết âm thầm.
   - Yêu cầu người dùng đăng nhập tài khoản cũ rồi xác nhận liên kết.
   - Nếu email chưa tồn tại, tạo `UserAccount`, role `USER` và
     `MemberProfile`.
6. Không nâng role từ dữ liệu social.

Trong success handler:

1. Tạo mã ngẫu nhiên 32 byte.
2. Chỉ lưu SHA-256 của mã.
3. Đặt thời hạn 2 phút, dùng một lần.
4. Redirect tới:

```text
http://localhost:8080/?socialCode=<raw-code>
```

Trong `SecurityConfig`:

```java
.oauth2Login(oauth2 -> oauth2
    .userInfoEndpoint(userInfo -> userInfo
        .userService(socialOAuth2UserService))
    .successHandler(socialAuthenticationSuccessHandler)
)
```

Cho phép các đường dẫn:

```text
/oauth2/**
/login/oauth2/**
POST /api/auth/social/exchange
```

Nếu dùng session tạm cho authorization request, đặt
`SessionCreationPolicy.IF_REQUIRED`, rồi invalidate session trong success
handler. API nghiệp vụ vẫn chỉ chấp nhận JWT Bearer.

Endpoint exchange nhận `socialCode`, kiểm tra hash, hạn sử dụng và `used=false`,
đánh dấu đã dùng trong cùng transaction, sau đó gọi
`AuthService.issueTokens(user)`.

## 12. Sửa điều kiện xác minh khi mượn sách

`BorrowingService` hiện chỉ chấp nhận `emailVerified=true`. Thay bằng một
phương thức thống nhất:

```java
private boolean hasVerifiedIdentity(UserAccount account) {
    return account.isEmailVerified()
            || account.isPhoneVerified()
            || loginIdentityRepository.existsByUserIdAndProviderIn(
                    account.getId(),
                    Set.of(LoginProvider.GOOGLE, LoginProvider.FACEBOOK)
            );
}
```

Như vậy tài khoản OTP/social đã xác minh không bị chặn mượn sách vì thiếu email.

## 13. Thêm nút và form frontend

Trong form đăng nhập:

```html
<a href="/oauth2/authorization/google">Tiếp tục với Google</a>
<a href="/oauth2/authorization/facebook">Tiếp tục với Facebook</a>
<button type="button" id="phone-login">Tiếp tục với số điện thoại</button>
```

Form số điện thoại có hai trạng thái:

1. Nhập số điện thoại và bấm “Gửi mã”.
2. Chỉ sau khi gửi thành công mới hiện ô OTP và nút “Xác minh”.

Khi trang được mở:

1. Đọc `socialCode` bằng `URLSearchParams`.
2. Gọi `POST /api/auth/social/exchange`.
3. Lưu `accessToken` và `refreshToken` bằng cơ chế hiện tại.
4. Xóa `socialCode` khỏi URL bằng `history.replaceState`.

## 14. Test bắt buộc

Không gọi Twilio/Google/Facebook thật trong integration test. Mock adapter ở
biên hệ thống.

Tối thiểu cần test:

1. Gửi OTP sai format trả 400.
2. OTP sai hoặc hết hạn không tạo tài khoản.
3. OTP đúng tạo tài khoản USER và trả JWT.
4. Một số điện thoại không tạo hai tài khoản.
5. Social subject cũ đăng nhập đúng tài khoản.
6. Email trùng không tự động chiếm tài khoản hiện có.
7. Social code dùng lần hai bị từ chối.
8. Social code hết hạn bị từ chối.
9. Google/Facebook không thể tự cấp role ADMIN.
10. Refresh/logout của tài khoản OTP/social hoạt động như tài khoản thường.
11. Người dùng có phone/social verified có thể mượn sách.

Chạy:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd clean package
```

## 15. Thứ tự triển khai khuyến nghị

1. Migration + entity.
2. Tách `AuthService.issueTokens`.
3. OTP service + endpoint + test.
4. Google OAuth2 + social code exchange + test.
5. Facebook dùng chung social service + test.
6. Sửa điều kiện xác minh khi mượn sách.
7. Frontend.
8. Rate limit, audit log và kiểm thử bảo mật.
9. Chạy toàn bộ test.
10. Thử production bằng HTTPS trước khi công khai.

## Tài liệu chính thức

- Spring Security OAuth2:
  https://docs.spring.io/spring-security/reference/servlet/oauth2/
- Spring Boot OAuth2 Client:
  https://docs.spring.io/spring-boot/reference/security/oauth2.html
- Google OAuth 2.0 Web Server:
  https://developers.google.com/identity/protocols/oauth2/web-server
- Meta Facebook Login:
  https://developers.facebook.com/docs/facebook-login/
- Twilio Verify v2:
  https://www.twilio.com/docs/verify/api/verification
