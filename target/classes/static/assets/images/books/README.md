# Ảnh bìa sách

Đặt ảnh bìa trong thư mục này. Giao diện hỗ trợ các định dạng:

- `.jpg`
- `.jpeg`
- `.png`
- `.webp`

Quy tắc tên file:

- Ưu tiên dùng ISBN của sách: `9786041234567.jpg`
- Nếu sách không có ISBN, dùng ID của sách: `<book-id>.jpg`
- Bỏ dấu gạch ngang và khoảng trắng khỏi ISBN.
- Nên dùng ảnh dọc tỷ lệ 2:3, ví dụ `600 × 900 px`.
- Khuyến nghị dung lượng dưới 300 KB mỗi ảnh.

Ví dụ sách có ISBN `978-604-123-456-7`:

```text
src/main/resources/static/assets/images/books/9786041234567.jpg
```

Sau khi thêm ảnh, build và khởi động lại ứng dụng:

```bash
./mvnw clean package
java -jar target/library-management-0.0.1-SNAPSHOT.jar
```

Nếu đặt ảnh trực tiếp trong `static/assets/images`, giao diện cũng sẽ tự
động tìm ảnh tại đó. Tuy nhiên, nên dùng thư mục `images/books` để dễ quản lý.

Nếu chạy bằng Spring Boot Maven Plugin:

```bash
./mvnw spring-boot:run
```
