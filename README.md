# Library Management API

## 1. Giới thiệu

Ứng dụng quản lý thư viện xây dựng bằng Spring Boot, JPA,
Spring Security và JWT.

## 2. Công nghệ sử dụng

- Java 21
- Spring Boot 4.1.1
- Spring Data JPA
- Spring Security + JWT
- PostgreSQL 17
- Liquibase
- Lombok
- Log4j2
- Spring AOP
- OpenAPI/Swagger
- JUnit 5
- Testcontainers
- Docker Compose

## 3. Chức năng

- Đăng nhập, đăng xuất
- Refresh token
- Đăng ký và xác minh email
- Quên, reset và đổi mật khẩu
- Đổi email bằng mã xác minh
- Phân quyền ADMIN và USER
- Quản lý sách
- Import sách từ CSV
- Quản lý thành viên
- Mượn và trả sách
- Maintenance mode
- Ghi log request/response
- Xử lý exception toàn cục

## 4. Tài khoản mặc định

- Username: admin
- Password: admin

## 5. Khởi động hạ tầng

docker compose up -d

## 6. Chạy ứng dụng

Thiết lập JWT_SECRET rồi chạy:
mvn spring-boot:run

## 7. Swagger

http://localhost:8080/swagger-ui/index.html

## 8. Mailpit

http://localhost:8025

## 9. Chạy test

mvn test

Kết quả hiện tại:
Tests run: 61, Failures: 0, Errors: 0

## 10. Cấu trúc CSV

isbn,title,description,publisher,publishedDate,totalQuantity,category,authors

