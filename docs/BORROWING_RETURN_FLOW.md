# Luồng mượn và trả sách

## 1. Tổng quan

Luồng mượn/trả được bảo vệ ở cả ba lớp:

1. Frontend chỉ hiển thị nút khi JWT có permission phù hợp.
2. Controller dùng `@PreAuthorize` để từ chối request không đủ quyền.
3. Service kiểm tra nghiệp vụ, khóa bản ghi và chạy trong transaction để tồn
   kho và lượt mượn luôn được cập nhật cùng nhau.

Các permission:

| Permission | Ý nghĩa |
|---|---|
| `BORROWING_BORROW` | Mượn sách |
| `BORROWING_RETURN_OWN` | Trả lượt mượn của chính user |
| `BORROWING_RETURN_ANY` | Trả sách thay cho bất kỳ member nào |
| `BORROWING_READ_OWN` | Xem lịch sử của chính user |
| `BORROWING_READ_ALL` | Xem toàn bộ lịch sử và thông tin người mượn |

## 2. Các file liên quan

| Lớp | File | Trách nhiệm |
|---|---|---|
| Giao diện | `src/main/resources/static/assets/js/app.js` | Hiển thị nút, gọi API, render lịch sử và đồng bộ các tab |
| User API | `borrowing/controller/BorrowingController.java` | Mượn, trả và xem lịch sử cá nhân |
| Quản trị API | `borrowing/controller/AdminBorrowingController.java` | Xem toàn bộ lịch sử |
| Request DTO | `borrowing/dto/request/BorrowBookRequest.java` | Validate `bookId` |
| Response DTO | `borrowing/dto/response/BorrowingResponse.java` | Dữ liệu trả cho frontend |
| Nghiệp vụ | `borrowing/service/BorrowingService.java` | Validate, transaction, cập nhật tồn kho |
| Entity | `borrowing/entity/Borrowing.java` | Mapping bảng `borrowings` |
| Repository | `borrowing/repository/BorrowingRepository.java` | Query lịch sử và khóa lượt mượn |
| Book lock | `book/repository/BookRepository.java` | Khóa sách khi thay đổi tồn kho |
| Member lock | `member/repository/MemberProfileRepository.java` | Khóa member khi tạo lượt mượn |
| Mapper | `borrowing/mapper/BorrowingMapper.java` | Chuyển entity thành response, tính quá hạn |
| Migration | `db/changelog/changes/011-create-borrowings.yaml` | Tạo bảng, khóa ngoại, index và constraint |
| TTL | `src/main/resources/application.yml` | `app.time-to-live.borrowing-seconds` |

## 3. Luồng mượn sách

Frontend gửi:

```http
POST /api/borrowings
Authorization: Bearer <access-token>
Content-Type: application/json

{
  "bookId": 12
}
```

Luồng xử lý:

1. `BorrowingController.borrow()` yêu cầu `BORROWING_BORROW`.
2. `BorrowingService.borrow()` đọc `uid` trong JWT.
3. Repository khóa `member_profiles` bằng `PESSIMISTIC_WRITE`.
4. Service kiểm tra account:
   - đang hoạt động;
   - email đã xác minh;
   - không bị khóa.
5. Kiểm tra member chưa có lượt `BORROWED`. Database còn có unique partial
   index `uk_borrowings_one_active_per_member`, nên request đồng thời cũng
   không thể tạo hai lượt mượn đang hoạt động.
6. Repository khóa cuốn sách bằng `PESSIMISTIC_WRITE`.
7. Kiểm tra sách đang hoạt động và `available_quantity > 0`.
8. Tạo bản ghi `borrowings`:
   - `status = BORROWED`;
   - `borrowed_at = thời điểm UTC hiện tại`;
   - `due_at = borrowed_at + borrowing-seconds`;
   - `returned_at = NULL`.
9. Giảm `books.available_quantity` đi 1.
10. Transaction commit cả hai thay đổi. Nếu bất kỳ bước nào lỗi, toàn bộ thay
    đổi rollback.

## 4. Luồng trả sách

Frontend gửi:

```http
POST /api/borrowings/{borrowingId}/return
Authorization: Bearer <access-token>
```

Luồng xử lý:

1. Controller yêu cầu `BORROWING_RETURN_OWN` hoặc
   `BORROWING_RETURN_ANY`.
2. Service khóa bản ghi `borrowings`.
3. Nếu không có `BORROWING_RETURN_ANY`, service so sánh `uid` trong JWT với
   owner của lượt mượn.
4. Từ chối nếu sách đã được trả.
5. Khóa bản ghi sách và kiểm tra tồn kho không bị sai lệch.
6. Cập nhật:
   - `status = RETURNED`;
   - `returned_at = thời điểm UTC hiện tại`;
   - tăng `books.available_quantity` lên 1.
7. Transaction commit hoặc rollback toàn bộ.

## 5. Xem lịch sử

- User gọi `GET /api/borrowings/my?page=0&size=10`.
- Người có `BORROWING_READ_ALL` gọi
  `GET /api/admin/borrowings?page=0&size=10`.
- Mỗi trang tối đa 10 bản ghi, sắp xếp `borrowedAt` giảm dần.
- Response gồm người mượn, sách, ngày mượn, hạn trả, ngày trả, trạng thái và
  `overdue`.
- `overdue` được tính khi trạng thái còn `BORROWED` và `dueAt` đã trước thời
  điểm UTC hiện tại.

## 6. Dữ liệu được lưu ở đâu

### Bảng `borrowings`

| Cột | Nội dung |
|---|---|
| `id` | ID lượt mượn |
| `member_id` | FK tới `member_profiles.id` |
| `book_id` | FK tới `books.id` |
| `status` | `BORROWED` hoặc `RETURNED` |
| `borrowed_at` | Thời điểm mượn, có timezone |
| `due_at` | Hạn trả, có timezone |
| `returned_at` | Thời điểm trả; `NULL` nếu chưa trả |
| `created_at`, `updated_at` | Thời điểm audit |

Tồn kho nằm ở `books.total_quantity` và `books.available_quantity`. Người mượn
nằm ở `member_profiles`, còn thông tin đăng nhập/email nằm ở `user_accounts`.
Các entity liên kết `Borrowing -> MemberProfile` và `Borrowing -> Book` bằng
`@ManyToOne`.

## 7. Câu lệnh kiểm tra nhanh

```sql
SELECT b.id,
       ua.email,
       mp.membership_code,
       bk.title,
       b.status,
       b.borrowed_at,
       b.due_at,
       b.returned_at
FROM borrowings b
JOIN member_profiles mp ON mp.id = b.member_id
JOIN user_accounts ua ON ua.id = mp.user_id
JOIN books bk ON bk.id = b.book_id
ORDER BY b.borrowed_at DESC;
```

```sql
SELECT id, title, total_quantity, available_quantity
FROM books
ORDER BY id;
```
