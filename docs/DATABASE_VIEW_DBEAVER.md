# Xem PostgreSQL bằng DBeaver

## 1. Bảo đảm database đang chạy

Tại thư mục dự án:

```powershell
docker compose ps
```

Service `postgres` phải có trạng thái `Up` và mapping cổng dạng
`0.0.0.0:5433->5432/tcp`. Nếu chưa chạy:

```powershell
docker compose up -d postgres
```

## 2. Tạo connection trong DBeaver

1. Chọn **Database → New Database Connection**.
2. Chọn **PostgreSQL**, nhấn **Next**.
3. Điền:

| Trường DBeaver | Giá trị mặc định của project |
|---|---|
| Host | `127.0.0.1` |
| Port | `5433` |
| Database | `library_db` |
| Username | `library_user` |
| Password | Giá trị `DB_PASSWORD`/`POSTGRES_PASSWORD` trong file `.env` |

4. Nhấn **Test Connection**.
5. Nếu DBeaver hỏi tải PostgreSQL JDBC driver, chọn **Download**.
6. Khi báo **Connected**, nhấn **Finish**.

Không đưa mật khẩu database vào ảnh chụp, Git hoặc README. File `.env` đã được
`.gitignore` loại khỏi repository.

## 3. Mở danh sách bảng

Trong **Database Navigator**, mở:

```text
Tên connection
└── Databases
    └── library_db
        └── Schemas
            └── public
                └── Tables
```

Nhấp đôi một bảng, chọn tab **Data** để xem dữ liệu. Có thể nhấp phải bảng và
chọn **View Data → All Rows**.

Các bảng thường dùng:

- `user_accounts`, `member_profiles`;
- `roles`, `permissions`, `user_roles`, `role_permissions`;
- `user_permissions`, `user_denied_permissions`;
- `books`, `categories`, `authors`, `book_authors`;
- `borrowings`, `saved_books`;
- `refresh_tokens`, `revoked_tokens`;
- `email_verification_tokens`, `password_reset_tokens`;
- `oauth_identities`, `social_login_codes`;
- `databasechangelog`, `databasechangeloglock`.

## 4. Chạy SQL

Chọn connection, nhấn **SQL → New SQL Script**, sau đó chạy bằng
`Ctrl + Enter`.

```sql
SELECT current_database(), current_user, version();
```

```sql
SELECT id, username, email, enabled, email_verified, account_non_locked
FROM user_accounts
ORDER BY id;
```

```sql
SELECT ua.email, p.name AS permission, 'DIRECT' AS source
FROM user_permissions up
JOIN user_accounts ua ON ua.id = up.user_id
JOIN permissions p ON p.id = up.permission_id
ORDER BY ua.email, p.name;
```

```sql
SELECT b.id, ua.email, bk.title, b.status,
       b.borrowed_at, b.due_at, b.returned_at
FROM borrowings b
JOIN member_profiles mp ON mp.id = b.member_id
JOIN user_accounts ua ON ua.id = mp.user_id
JOIN books bk ON bk.id = b.book_id
ORDER BY b.borrowed_at DESC;
```

## 5. Lỗi kết nối thường gặp

### Connection refused

- Chạy `docker compose ps`.
- Kiểm tra PostgreSQL đang map ra cổng `5433`.
- Dùng `127.0.0.1`, không dùng hostname `postgres` từ DBeaver trên Windows.

### Password authentication failed

- Đối chiếu `POSTGRES_USER`, `POSTGRES_PASSWORD` và `POSTGRES_DB` trong `.env`.
- Nếu volume PostgreSQL được tạo trước khi đổi password, biến mới không tự đổi
  password trong database cũ. Hãy đổi password bằng SQL hoặc chủ động tạo lại
  volume chỉ khi không cần dữ liệu cũ.

### Không thấy bảng

- Chạy ứng dụng Spring Boot ít nhất một lần để Liquibase tạo schema.
- Refresh connection bằng `F5`.
- Kiểm tra đúng database `library_db` và schema `public`.

### Port 5433 đang bị chiếm

Đổi `POSTGRES_PORT` trong `.env`, đồng thời sửa `DB_URL` theo cùng cổng, rồi
khởi động lại PostgreSQL và ứng dụng.
