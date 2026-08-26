(() => {
    "use strict";

    const TOKEN_KEY = "sparkLibrary.accessToken";
    const REFRESH_KEY = "sparkLibrary.refreshToken";
    const LANGUAGE_KEY = "sparkLibrary.language";
    const COVER_ASSET_VERSION = "20260825-5";

    /*
     * The Vietnamese copy remains the canonical copy in the templates.
     * Keeping translations here gives every dynamically rendered view and
     * modal the same language source instead of scattering language checks.
     */
    const EN_TEXT = {
        "Đang tải": "Loading",
        "Đang mở thư viện": "Opening the library",
        "Thư viện cộng đồng": "Community library",
        "Đọc · Mượn · Khám phá": "Read · Borrow · Discover",
        "Mỗi cuốn sách mở ra một lối đi mới.": "Every book opens a new path.",
        "Tìm sách trong bộ sưu tập, theo dõi những cuốn đang mượn và quay lại kệ sách của riêng bạn bất cứ lúc nào.": "Explore the collection, track borrowed books, and return to your personal shelf at any time.",
        "Tra cứu bộ sưu tập · Mượn và trả sách · 2026": "Browse the collection · Borrow and return books · 2026",
        "Chào mừng trở lại": "Welcome back",
        "Đăng nhập": "Sign in",
        "Nhập tài khoản để tiếp tục vào thư viện.": "Enter your account to continue to the library.",
        "Tên đăng nhập hoặc email": "Username or email",
        "Mật khẩu": "Password",
        "Nhập mật khẩu": "Enter password",
        "Quên mật khẩu?": "Forgot password?",
        "Chưa có tài khoản?": "New to the library?",
        "Đăng ký ngay": "Create an account",
        "Hoặc": "Or",
        "Tiếp tục với Google": "Continue with Google",
        "Đăng nhập Google thành công": "Google sign-in successful",
        "Không thể đăng nhập bằng Google. Vui lòng thử lại.": "Unable to sign in with Google. Please try again.",
        "Thành viên mới": "New member",
        "Tạo tài khoản": "Create account",
        "Chúng tôi sẽ gửi liên kết xác minh đến email của bạn.": "We will send a verification link to your email.",
        "Tối thiểu 8 ký tự": "At least 8 characters",
        "Gồm chữ hoa, chữ thường, số và ký tự đặc biệt.": "Use uppercase, lowercase, a number, and a special character.",
        "Đã có tài khoản?": "Already have an account?",
        "Tìm kiếm sách": "Search books",
        "Tìm theo tên sách, tác giả hoặc ISBN": "Search by title, author, or ISBN",
        "Tìm sách": "Search books",
        "Đăng xuất": "Sign out",
        "Đóng menu": "Close menu",
        "Mở menu": "Open menu",
        "Tổng quan": "Overview",
        "Đang bảo trì": "Maintenance mode",
        "Kho sách cộng đồng": "Community collection",
        "Trang chủ": "Home",
        "Kho sách": "Catalog",
        "Mượn & trả": "Loans",
        "Mượn & trả sách": "Borrow & return",
        "Sách của tôi": "My books",
        "Thành viên": "Members",
        "Hệ thống": "System",
        "Tài khoản": "Account",
        "Thư viện": "Library",
        "Khám phá": "Discover",
        "Quản trị": "Administration",
        "Lưu thông": "Circulation",
        "Cá nhân": "Personal",
        "Tài khoản của tôi": "My account",
        "Quản trị viên": "Administrator",
        "Quản trị viên hệ thống": "System administrator",
        "Thành viên thư viện": "Library member",
        "Quản lý thành viên": "Member management",
        "Tìm kiếm và cập nhật tài khoản thư viện.": "Search and update library accounts.",
        "Thêm thành viên": "Add member",
        "Từ khóa": "Keyword",
        "Tên, email, mã thành viên": "Name, email, member code",
        "Tên thành viên": "Member name",
        "Tìm gần đúng": "Partial name",
        "Tên sách đã mượn": "Borrowed book title",
        "Tên sách": "Book title",
        "Trạng thái": "Status",
        "Tất cả": "All",
        "Đang hoạt động": "Active",
        "Đã vô hiệu hóa": "Disabled",
        "Sinh từ ngày": "Born from",
        "Sinh đến ngày": "Born to",
        "Xác minh email": "Email verification",
        "Đã xác minh": "Verified",
        "Chưa xác minh": "Not verified",
        "Tài khoản khóa": "Account lock",
        "Đang khóa": "Locked",
        "Không khóa": "Unlocked",
        "Tìm kiếm": "Search",
        "Không tìm thấy thành viên": "No members found",
        "Thử thay đổi các điều kiện tìm kiếm.": "Try changing the search criteria.",
        "Mã thành viên": "Member code",
        "Ngày sinh": "Date of birth",
        "Liên hệ": "Contact",
        "Thao tác": "Actions",
        "Họ và tên": "Full name",
        "Số điện thoại": "Phone number",
        "Địa chỉ": "Address",
        "Thông tin mật khẩu": "Password status",
        "Đã thiết lập": "Configured",
        "Chưa thiết lập": "Not configured",
        "Mật khẩu đã được mã hóa và không thể xem.": "The password is encrypted and cannot be viewed.",
        "Chỉnh sửa": "Edit",
        "Vô hiệu hóa": "Disable",
        "Cập nhật thành viên": "Update member",
        "Tạo thành viên": "Create member",
        "Tài khoản mới có thể đăng nhập ngay.": "The new account can sign in immediately.",
        "Email *": "Email *",
        "Mật khẩu *": "Password *",
        "Họ và tên *": "Full name *",
        "Trạng thái tài khoản *": "Account status *",
        "Khóa tài khoản *": "Account lock *",
        "Lưu thay đổi": "Save changes",
        "Hoạt động mượn trả": "Loan activity",
        "Theo dõi toàn bộ giao dịch trong thư viện.": "Track all borrowing activity in the library.",
        "Theo dõi thời hạn và lịch sử mượn sách.": "Track due dates and your borrowing history.",
        "Chưa có lượt mượn sách": "No loans yet",
        "Các giao dịch mới sẽ xuất hiện tại đây.": "New transactions will appear here.",
        "Hãy khám phá thư viện và chọn một cuốn sách.": "Explore the catalog and choose a book.",
        "Sách": "Book",
        "Người mượn": "Borrower",
        "Ngày mượn": "Borrowed date",
        "Hạn trả": "Due date",
        "Ngày trả": "Returned date",
        "Chưa trả": "Not returned",
        "Trả sách": "Return book",
        "Đã trả": "Returned",
        "Quá hạn": "Overdue",
        "Đang mượn": "Borrowed",
        "Xác nhận trả sách?": "Confirm book return?",
        "Hệ thống sẽ ghi nhận thời gian trả sách hiện tại.": "The system will record the current return time.",
        "Xác nhận trả": "Confirm return",
        "Trạng thái hệ thống": "System status",
        "Kiểm soát khả năng truy cập API khi bảo trì.": "Control API access during maintenance.",
        "Chế độ bảo trì": "Maintenance mode",
        "Khi bật, toàn bộ API nghiệp vụ sẽ tạm dừng. API đăng nhập và cấu hình vẫn hoạt động.": "When enabled, business APIs are paused. Login and configuration APIs remain available.",
        "Bật/tắt bảo trì": "Enable/disable maintenance",
        "Thông báo bảo trì": "Maintenance message",
        "Thông điệp trả về khi hệ thống tạm dừng.": "Message returned while the system is paused.",
        "Nội dung thông báo": "Message",
        "Hệ thống đang được bảo trì...": "The system is under maintenance...",
        "Lưu cấu hình": "Save configuration",
        "Cập nhật bởi": "Updated by",
        "Cập nhật lúc": "Updated at",
        "Cách thức hoạt động": "How it works",
        "Khi bật chế độ bảo trì": "When maintenance is enabled",
        "Các API sách, thành viên và mượn trả sẽ trả về HTTP 503 Service Unavailable cùng thông báo ở bên dưới.": "Book, member, and loan APIs return HTTP 503 Service Unavailable with the message below.",
        "API vẫn được phép": "APIs that remain available",
        "Đăng nhập, kiểm tra trạng thái và API cấu hình vẫn hoạt động để quản trị viên có thể quay lại trang này và tắt bảo trì.": "Login, status checks, and configuration APIs remain available so an administrator can return here and disable maintenance.",
        "Cách sử dụng an toàn": "Safe usage",
        "Nhập thông báo cho người dùng, bật công tắc rồi nhấn Lưu cấu hình. Khi hoàn tất bảo trì, tắt công tắc và lưu lại.": "Enter a user-facing message, enable the switch, and save. When maintenance is complete, disable the switch and save again.",
        "Thông tin và bảo mật": "Profile & security",
        "Quản lý hồ sơ, email và mật khẩu đăng nhập.": "Manage your profile, email, and sign-in password.",
        "Thông tin cá nhân chưa đầy đủ": "Your profile is incomplete",
        "Vui lòng cập nhật số điện thoại và ngày sinh để thư viện có thể liên hệ và hỗ trợ việc mượn trả sách.": "Please add your phone number and date of birth so the library can contact you and support loan services.",
        "Thông tin tài khoản": "Account information",
        "Cập nhật thông tin hiển thị và thông tin liên hệ của bạn.": "Update your display and contact information.",
        "Tên người dùng": "Username",
        "Lưu hồ sơ": "Save profile",
        "Quyền": "Role",
        "Đổi mật khẩu": "Change password",
        "Bạn sẽ cần đăng nhập lại sau khi đổi.": "You will need to sign in again after changing it.",
        "Mật khẩu hiện tại": "Current password",
        "Mật khẩu mới": "New password",
        "Đổi địa chỉ email": "Change email address",
        "Mã xác minh sẽ được gửi tới email mới.": "A verification code will be sent to the new email.",
        "Email mới": "New email",
        "Gửi mã xác minh": "Send verification code",
        "Xác minh email mới": "Verify new email",
        "Nhập mã gồm 6 chữ số trong email.": "Enter the 6-digit code from the email.",
        "Mã xác minh": "Verification code",
        "Xác nhận đổi email": "Confirm email change",
        "Hiển thị mật khẩu": "Show password",
        "Ẩn mật khẩu": "Hide password",
        "Hiển thị trạng thái mật khẩu": "Show password status",
        "Ẩn trạng thái mật khẩu": "Hide password status",
        "Đóng": "Close",
        "Hủy": "Cancel",
        "Xác nhận": "Confirm",
        "Kết quả": "Results",
        "kết quả": "results",
        "Trang trước": "Previous page",
        "Trang sau": "Next page",
        "Tạm hết sách": "Unavailable",
        "Hoạt động": "Active",
        "Không thể tải dữ liệu": "Unable to load data",
        "Hệ thống đang bảo trì": "System is under maintenance"
        , "Đầu sách": "Titles"
        , "Trong danh mục": "In the catalog"
        , "Tài khoản đang quản lý": "Managed accounts"
        , "Lượt mượn": "Loans"
        , "Tổng lịch sử lưu thông": "All circulation records"
        , "Cần xử lý": "Needs attention"
        , "Không có cảnh báo": "No alerts"
        , "Có thể tra cứu": "Available to browse"
        , "Lịch sử": "History"
        , "Tổng lượt mượn": "Total loans"
        , "Chưa hoàn trả": "Not yet returned"
        , "Vui lòng hoàn trả": "Please return"
        , "Không có sách quá hạn": "No overdue books"
        , "Kho sách & lưu thông": "Catalog & circulation"
        , "Tìm nhanh trong toàn bộ thư viện": "Search the entire library"
        , "Bạn muốn đọc gì hôm nay?": "What would you like to read today?"
        , "Tra cứu đầu sách trước khi cập nhật kho, mượn hoặc trả.": "Find a title before updating inventory, borrowing, or returning it."
        , "Tìm theo tên sách, tác giả hoặc mã ISBN trong bộ sưu tập.": "Search the collection by title, author, or ISBN."
        , "Nhập tên sách, tác giả hoặc ISBN": "Enter a title, author, or ISBN"
        , "Tìm trong thư viện": "Search the library"
        , "Bộ sưu tập": "Collection"
        , "Sách trên kệ": "Books on the shelf"
        , "Xem toàn bộ kho sách": "View the full catalog"
        , "Hoạt động gần đây": "Recent activity"
        , "Xem lịch sử mượn trả": "View loan history"
        , "Danh mục thư viện": "Library catalog"
        , "Khám phá kho sách": "Explore the catalog"
        , "Tra cứu, cập nhật và bổ sung tài liệu vào bộ sưu tập.": "Search, update, and add materials to the collection."
        , "Duyệt sách theo từ khóa, tác giả, danh mục và tình trạng.": "Browse by keyword, author, category, and availability."
        , "Nhập CSV": "Import CSV"
        , "Thêm sách": "Add book"
        , "Lọc kết quả": "Filter results"
        , "Xóa lọc": "Clear filters"
        , "Tên, ISBN, tác giả...": "Title, ISBN, author..."
        , "Danh mục": "Category"
        , "Tất cả danh mục": "All categories"
        , "Nhà xuất bản": "Publisher"
        , "Nhập nhà xuất bản": "Enter publisher"
        , "Tình trạng": "Availability"
        , "Có thể mượn": "Available"
        , "Đang hết sách": "Out of stock"
        , "Áp dụng bộ lọc": "Apply filters"
        , "Không tìm thấy sách": "No books found"
        , "Thử thay đổi từ khóa hoặc bộ lọc tìm kiếm.": "Try changing the keyword or filters."
        , "Khác": "Other"
        , "Mượn sách": "Borrow book"
        , "Chưa có dữ liệu sách": "No book data yet"
        , "Thêm sách mới hoặc nhập danh sách từ CSV.": "Add a new book or import a CSV list."
        , "Số lượng": "Quantity"
        , "Tên sách *": "Book title *"
        , "Tác giả *": "Authors *"
        , "Danh mục *": "Category *"
        , "ISBN *": "ISBN *"
        , "Ngày xuất bản": "Published date"
        , "Tổng số lượng *": "Total quantity *"
        , "Mô tả": "Description"
        , "Chọn file CSV *": "Choose CSV file *"
        , "Chế độ bảo trì được sử dụng như thế nào?": "How is maintenance mode used?"
        , "Dùng chế độ này khi nâng cấp ứng dụng, sửa dữ liệu hoặc thực hiện công việc cần tạm dừng giao dịch.": "Use this mode during upgrades, data repairs, or work that requires transactions to pause."
        , "Tên người dùng *": "Username *"
        , "Số điện thoại *": "Phone number *"
        , "Ngày sinh *": "Date of birth *"
        , "Cập nhật ngay": "Update now"
        , "Đã cập nhật hồ sơ": "Profile updated"
        , "Thông tin cá nhân đã được lưu.": "Your personal information has been saved."
        , "Đang cập nhật...": "Updating..."
        , "Đặt mật khẩu mới": "Set a new password"
        , "Liên kết đã được xác nhận. Hãy tạo mật khẩu mới.": "The link has been verified. Create a new password."
        , "Quên mật khẩu": "Forgot password"
        , "Nhập email đăng ký để nhận liên kết đặt lại mật khẩu.": "Enter your registered email to receive a password reset link."
        , "Nhập lại mật khẩu mới": "Confirm new password"
        , "Nhập mật khẩu mới": "Enter a new password"
        , "Nhập lại mật khẩu": "Enter the password again"
        , "Cập nhật mật khẩu": "Update password"
        , "Nhập email đăng ký": "Enter your registered email"
        , "Mở liên kết trong email": "Open the link in the email"
        , "Tạo mật khẩu mới": "Create a new password"
        , "Email đăng ký": "Registered email"
        , "Gửi liên kết đặt lại mật khẩu": "Send password reset link"
        , "Hãy kiểm tra hộp thư của bạn": "Check your inbox"
        , "Nếu email thuộc một tài khoản hợp lệ, hệ thống đã gửi liên kết có hiệu lực trong 30 phút. Kiểm tra cả thư mục spam hoặc thư rác.": "If the email belongs to a valid account, a link valid for 30 minutes has been sent. Check your spam or junk folder too."
        , "Mở hộp thư thử nghiệm Mailpit": "Open the Mailpit test inbox"
        , "Đã gửi hướng dẫn": "Instructions sent"
        , "Hãy kiểm tra email để tiếp tục.": "Check your email to continue."
        , "Mật khẩu nhập lại không khớp.": "The password confirmation does not match."
        , "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập bằng mật khẩu mới.": "Your password has been reset. You can now sign in with the new password."
        , "Chi tiết sách": "Book details"
        , "Trở lại kho sách": "Back to catalog"
        , "Lưu": "Save"
        , "Đã lưu": "Saved"
        , "Lưu sách": "Save book"
        , "Bỏ lưu": "Remove from saved"
        , "Đã lưu sách": "Book saved"
        , "Đã bỏ khỏi danh sách lưu": "Removed from saved books"
        , "Giới thiệu về cuốn sách": "About this book"
        , "Thông tin xuất bản": "Publication details"
        , "Ngày phát hành": "Published date"
        , "Số bản hiện có": "Available copies"
        , "Tổng số bản": "Total copies"
        , "Sách đã lưu": "Saved books"
        , "Bộ sưu tập bạn muốn đọc sau": "Your reading list for later"
        , "Bạn chưa lưu cuốn sách nào.": "You have not saved any books yet."
        , "Mở chi tiết": "View details"
        , "Chỉnh sửa sách": "Edit book"
        , "Mô tả đang được cập nhật.": "The description is being updated."
        , "Bạn": "You"
        , "Cấu hình hệ thống": "System configuration"
        , "Chào mừng bạn trở lại.": "Welcome back."
        , "Đang đăng nhập...": "Signing in..."
        , "Đăng nhập thành công": "Signed in successfully"
        , "Đang tạo...": "Creating..."
        , "Đăng ký thành công. Hãy kiểm tra email để xác minh.": "Registration successful. Check your email to verify your account."
        , "Sách trước": "Previous books"
        , "Sách tiếp theo": "Next books"
        , "đầu sách phù hợp": "matching books"
        , "đầu sách trong danh mục": "books in the catalog"
        , "Ngừng hoạt động": "Deactivate"
        , "Đang mượn...": "Borrowing..."
        , "Mượn sách thành công": "Book borrowed successfully"
        , "Không thể mượn sách": "Unable to borrow the book"
        , "Cập nhật sách": "Update book"
        , "Thêm sách mới": "Add a new book"
        , "Các trường có dấu * là bắt buộc.": "Fields marked with * are required."
        , "Mô tả ngắn về cuốn sách": "A short description of the book"
        , "Đang lưu...": "Saving..."
        , "Đang bỏ lưu...": "Removing..."
        , "Đã cập nhật sách": "Book updated"
        , "Đã thêm sách": "Book added"
        , "Không thể cập nhật": "Unable to update"
        , "Ngừng hoạt động sách?": "Deactivate this book?"
        , "Đang xử lý...": "Processing..."
        , "Đã cập nhật trạng thái sách": "Book status updated"
        , "Nhập sách từ CSV": "Import books from CSV"
        , "Dung lượng tối đa 5 MB, chỉ hỗ trợ file .csv.": "Maximum size is 5 MB. Only .csv files are supported."
        , "Nhập dữ liệu": "Import data"
        , "Đang nhập...": "Importing..."
        , "Nhập CSV thành công": "CSV imported successfully"
        , "Ví dụ: Member@123": "Example: Member@123"
        , "Đã cập nhật thành viên": "Member updated"
        , "Đã tạo thành viên": "Member created"
        , "Vô hiệu hóa thành viên?": "Disable this member?"
        , "Đã vô hiệu hóa thành viên": "Member disabled"
        , "Không thể vô hiệu hóa": "Unable to disable the member"
        , "Cảm ơn bạn!": "Thank you!"
        , "Trả sách thành công": "Book returned successfully"
        , "Không thể trả sách": "Unable to return the book"
        , "Đã lưu cấu hình": "Configuration saved"
        , "Hệ thống đang ở chế độ bảo trì.": "The system is in maintenance mode."
        , "Hệ thống hoạt động bình thường.": "The system is operating normally."
        , "Đã gửi mã xác minh": "Verification code sent"
        , "Đang gửi...": "Sending..."
        , "Đang xác minh...": "Verifying..."
        , "Đổi email thành công": "Email changed successfully"
        , "Vui lòng đăng nhập lại bằng email mới.": "Sign in again with your new email."
        , "Đổi mật khẩu thành công": "Password changed successfully"
        , "Vui lòng đăng nhập lại.": "Please sign in again."
        , "Bạn có thể đăng nhập bằng mật khẩu mới.": "You can sign in with your new password."
        , "Không thể kết nối tới hệ thống. Hãy kiểm tra server.": "Unable to connect to the system. Check the server."
        , "Chưa cập nhật tác giả": "Authors not yet provided"
        , "Vị trí ảnh bìa sách": "Book cover area"
        , "Về trang chủ": "Back to home"
        , "Dashboard": "Dashboard"
        , "Bảng điều khiển": "Dashboard"
        , "cuốn": "copies"
        , "Công nghệ": "Technology"
        , "Khoa học": "Science"
        , "Lịch sử": "History"
        , "Tâm lý học": "Psychology"
        , "Phát triển bản thân": "Self Development"
        , "Kinh doanh": "Business"
        , "Văn học": "Literature"
    };
    const VI_TEXT = Object.fromEntries(
        Object.entries(EN_TEXT).map(([vi, en]) => [en, vi])
    );

    const API_TEXT_VI = {
        "Validation failed": "Dữ liệu không hợp lệ",
        "Invalid username, password, or account status": "Tên đăng nhập, mật khẩu hoặc trạng thái tài khoản không hợp lệ",
        "Email is required": "Email là bắt buộc",
        "Email format is invalid": "Định dạng email không hợp lệ",
        "Email is too long": "Email quá dài",
        "Password is required": "Mật khẩu là bắt buộc",
        "Current password is required": "Mật khẩu hiện tại là bắt buộc",
        "New password is required": "Mật khẩu mới là bắt buộc",
        "Password must contain 8-72 characters, uppercase, lowercase, number and special character": "Mật khẩu phải dài 8–72 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt",
        "Username or email is required": "Tên đăng nhập hoặc email là bắt buộc",
        "Refresh token is required": "Refresh token là bắt buộc",
        "Reset token is required": "Token đặt lại mật khẩu là bắt buộc",
        "New email is required": "Email mới là bắt buộc",
        "New email format is invalid": "Định dạng email mới không hợp lệ",
        "Verification code is required": "Mã xác minh là bắt buộc",
        "Verification code must contain 6 digits": "Mã xác minh phải gồm 6 chữ số",
        "Full name is required": "Họ và tên là bắt buộc",
        "Date of birth must be in the past": "Ngày sinh phải là một ngày trong quá khứ",
        "Phone format is invalid": "Định dạng số điện thoại không hợp lệ",
        "Enabled is required": "Trạng thái hoạt động là bắt buộc",
        "Account lock status is required": "Trạng thái khóa tài khoản là bắt buộc",
        "Username is required": "Tên người dùng là bắt buộc",
        "Username must contain between 3 and 50 characters": "Tên người dùng phải dài từ 3 đến 50 ký tự",
        "Username may contain only letters, numbers, dots, underscores and hyphens": "Tên người dùng chỉ được chứa chữ, số, dấu chấm, gạch dưới và gạch ngang",
        "Maintenance status is required": "Trạng thái bảo trì là bắt buộc",
        "Maintenance message must not exceed 500 characters": "Thông báo bảo trì không được vượt quá 500 ký tự",
        "Each page contains at most 10 records": "Mỗi trang chỉ được chứa tối đa 10 bản ghi",
        "Page cannot be negative": "Số trang không được là số âm",
        "Size must be positive": "Kích thước trang phải lớn hơn 0",
        "Active status is required": "Trạng thái hoạt động là bắt buộc",
        "ISBN is required": "ISBN là bắt buộc",
        "ISBN is too long": "ISBN quá dài",
        "Title is required": "Tên sách là bắt buộc",
        "Title is too long": "Tên sách quá dài",
        "Description is too long": "Mô tả quá dài",
        "Publisher is too long": "Tên nhà xuất bản quá dài",
        "Published date cannot be in the future": "Ngày xuất bản không được ở tương lai",
        "Total quantity cannot be negative": "Tổng số lượng không được là số âm",
        "Category ID is required": "Danh mục là bắt buộc",
        "Category ID must be positive": "Mã danh mục phải lớn hơn 0",
        "Author name cannot be blank": "Tên tác giả không được để trống",
        "Author name is too long": "Tên tác giả quá dài",
        "Book ID is required": "Sách là bắt buộc",
        "Book ID must be positive": "Mã sách phải lớn hơn 0",
        "Social login code is required": "Mã đăng nhập Google là bắt buộc",
        "Social login code is invalid": "Mã đăng nhập Google không hợp lệ",
        "Email has already been registered": "Email đã được đăng ký",
        "Email cannot be used": "Email này không thể sử dụng",
        "Username is already in use": "Tên người dùng đã được sử dụng",
        "Current password is incorrect": "Mật khẩu hiện tại không đúng",
        "New password must be different from current password": "Mật khẩu mới phải khác mật khẩu hiện tại",
        "Password reset token is invalid or expired": "Token đặt lại mật khẩu không hợp lệ hoặc đã hết hạn",
        "Refresh token is invalid or expired": "Refresh token không hợp lệ hoặc đã hết hạn",
        "Social login code is invalid or expired": "Mã đăng nhập Google không hợp lệ hoặc đã hết hạn",
        "Verification token is invalid or expired": "Token xác minh không hợp lệ hoặc đã hết hạn",
        "Email has not been verified": "Email chưa được xác minh",
        "Member account is disabled": "Tài khoản thành viên đã bị vô hiệu hóa",
        "Member account is locked": "Tài khoản thành viên đang bị khóa",
        "Each member can borrow only one book at a time": "Mỗi thành viên chỉ được mượn một cuốn sách tại một thời điểm",
        "Book is inactive": "Sách đã ngừng hoạt động",
        "Book is out of stock": "Sách đã hết trong kho",
        "Book has already been returned": "Sách đã được trả trước đó",
        "Book inventory is inconsistent": "Số lượng tồn kho của sách không nhất quán",
        "Inactive books cannot be saved": "Không thể lưu sách đã ngừng hoạt động",
        "Admin account cannot be modified through member management": "Không thể chỉnh sửa tài khoản admin trong màn hình quản lý thành viên",
        "Date of birth from must be before date of birth to": "Ngày sinh bắt đầu phải trước ngày sinh kết thúc",
        "Google account email is not verified": "Email của tài khoản Google chưa được xác minh",
        "This library account is already linked to another Google account": "Tài khoản thư viện này đã liên kết với một tài khoản Google khác",
        "Unable to send email": "Không thể gửi email",
        "File size must not exceed 5 MB": "Dung lượng file không được vượt quá 5 MB",
        "File must be a CSV document": "File phải có định dạng CSV",
        "CSV file is required": "Vui lòng chọn file CSV",
        "CSV file size must not exceed 5 MB": "Dung lượng file CSV không được vượt quá 5 MB",
        "Only .csv files are supported": "Chỉ hỗ trợ file có phần mở rộng .csv",
        "CSV file does not contain any data rows": "File CSV không chứa dòng dữ liệu nào",
        "Cannot read CSV file": "Không thể đọc file CSV",
        "ISBN has already existed": "ISBN đã tồn tại",
        "Cannot delete a book that is currently borrowed": "Không thể ngừng hoạt động sách đang được mượn",
        "Category is inactive": "Danh mục đã ngừng hoạt động",
        "Published from must be before published to": "Ngày xuất bản bắt đầu phải trước ngày xuất bản kết thúc",
        "Invalid authenticated user": "Người dùng đã xác thực không hợp lệ",
        "System is operating normally.": "Hệ thống hoạt động bình thường.",
        "System is under maintenance.": "Hệ thống đang được bảo trì.",
        "Registration successful. Please verify your email.": "Đăng ký thành công. Vui lòng xác minh email.",
        "Email verified successfully. You can now login.": "Xác minh email thành công. Bạn có thể đăng nhập.",
        "If the email exists, a password reset email was sent.": "Nếu email tồn tại, hệ thống đã gửi thư đặt lại mật khẩu.",
        "Password reset successfully. Please login again.": "Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại.",
        "Password changed successfully. Please login again.": "Đổi mật khẩu thành công. Vui lòng đăng nhập lại.",
        "Verification code was sent to the new email.": "Mã xác minh đã được gửi tới email mới.",
        "Email changed successfully. Please login again.": "Đổi email thành công. Vui lòng đăng nhập lại.",
        "Account does not exist": "Tài khoản không tồn tại",
        "New email must be different from current email": "Email mới phải khác email hiện tại",
        "No pending email change request": "Không có yêu cầu đổi email nào đang chờ xử lý",
        "Verification code has expired": "Mã xác minh đã hết hạn",
        "Maximum verification attempts exceeded": "Đã vượt quá số lần nhập mã xác minh",
        "Verification code is incorrect": "Mã xác minh không chính xác",
        "Authenticated user is invalid": "Người dùng đã xác thực không hợp lệ",
        "New email has already been registered": "Email mới đã được đăng ký",
        "New username has already been registered": "Tên người dùng mới đã được đăng ký",
        "Member profile does not exist": "Hồ sơ thành viên không tồn tại",
        "You cannot return this borrowing": "Bạn không có quyền trả lượt mượn này",
        "Borrowed book does not exist": "Sách đã mượn không còn tồn tại",
        "At least one author is required": "Phải có ít nhất một tác giả",
        "Total quantity cannot be less than borrowed quantity": "Tổng số lượng không được nhỏ hơn số sách đang được mượn"
    };

    const VI_API_TEXT_EN = Object.fromEntries(
        Object.entries(API_TEXT_VI)
            .map(([en, vi]) => [vi, en])
    );

    const VI_TO_EN_PATTERNS = [
        [/^Xin chào (.+)$/u, "Hello $1"],
        [/^Hạn trả: (.+)$/u, "Due: $1"],
        [/^(\d+) cuốn có sẵn$/u, "$1 copies available"],
        [/^(\d+) sách đã được thêm\.$/u, "$1 books were added."],
        [/^Trang (\d+)\/(\d+) · (\d+) kết quả$/u, "Page $1/$2 · $3 results"],
        [/^(\d+) kết quả$/u, "$1 results"],
        [/^Mở chi tiết (.+)$/u, "View details for $1"],
        [/^Bìa sách (.+)$/u, "Cover of $1"],
        [/^“(.+)” sẽ không còn hiển thị cho thành viên\.$/u, "“$1” will no longer be visible to members."],
        [/^(.+) sẽ không thể đăng nhập\.$/u, "$1 will no longer be able to sign in."],
        [/^Yêu cầu thất bại \((\d+)\)$/u, "Request failed ($1)"]
    ];

    const EN_TO_VI_PATTERNS = [
        [/^Hello (.+)$/u, "Xin chào $1"],
        [/^Due: (.+)$/u, "Hạn trả: $1"],
        [/^(\d+) copies available$/u, "$1 cuốn có sẵn"],
        [/^(\d+) books were added\.$/u, "$1 sách đã được thêm."],
        [/^Page (\d+)\/(\d+) · (\d+) results$/u, "Trang $1/$2 · $3 kết quả"],
        [/^(\d+) results$/u, "$1 kết quả"],
        [/^View details for (.+)$/u, "Mở chi tiết $1"],
        [/^Cover of (.+)$/u, "Bìa sách $1"],
        [/^“(.+)” will no longer be visible to members\.$/u, "“$1” sẽ không còn hiển thị cho thành viên."],
        [/^(.+) will no longer be able to sign in\.$/u, "$1 sẽ không thể đăng nhập."],
        [/^Request failed \((\d+)\)$/u, "Yêu cầu thất bại ($1)"],
        [/^Book does not exist: (.+)$/u, "Không tồn tại sách: $1"],
        [/^Borrowing does not exist: (.+)$/u, "Không tồn tại lượt mượn: $1"],
        [/^Member does not exist: (.+)$/u, "Không tồn tại thành viên: $1"],
        [/^User account does not exist: (.+)$/u, "Không tồn tại tài khoản người dùng: $1"],
        [/^Category does not exist: (.+)$/u, "Không tồn tại danh mục: $1"],
        [/^ISBN has already been registered: (.+)$/u, "ISBN đã được đăng ký: $1"],
        [/^Total quantity cannot be smaller than borrowed quantity: (\d+)$/u,
            "Tổng số lượng không được nhỏ hơn số sách đang được mượn: $1"],
        [/^CSV is missing required headers: (.+)$/u,
            "File CSV thiếu các cột bắt buộc: $1"],
        [/^CSV line (\d+): (.+)$/u, "Dòng CSV $1: $2"]
    ];

    const state = {
        user: null,
        profile: null,
        isAdmin: false,
        locale: localStorage.getItem(LANGUAGE_KEY) === "en" ? "en" : "vi",
        currentView: "dashboard",
        categories: [],
        books: new Map(),
        members: new Map(),
        maintenance: null,
        pendingBookSearch: "",
        selectedBookId: null,
        dashboardShelfBooks: [],
        dashboardShelfIndex: 0
    };

    const elements = {
        loading: document.querySelector("#loading-screen"),
        authView: document.querySelector("#auth-view"),
        appView: document.querySelector("#app-view"),
        authMessage: document.querySelector("#auth-message"),
        pageContent: document.querySelector("#page-content"),
        pageTitle: document.querySelector("#page-title"),
        pageEyebrow: document.querySelector("#page-eyebrow"),
        sidebar: document.querySelector("#sidebar"),
        sidebarOverlay: document.querySelector("#sidebar-overlay"),
        sidebarNav: document.querySelector("#sidebar-nav"),
        modalRoot: document.querySelector("#modal-root"),
        toastRoot: document.querySelector("#toast-root")
    };

    const viewMeta = {
        dashboard: ["Thư viện", "Trang chủ"],
        books: ["Khám phá", "Kho sách"],
        bookDetail: ["Khám phá", "Chi tiết sách"],
        members: ["Quản trị", "Thành viên"],
        borrowings: ["Lưu thông", "Mượn & trả sách"],
        system: ["Quản trị", "Cấu hình hệ thống"],
        account: ["Cá nhân", "Tài khoản của tôi"]
    };

    document.addEventListener("DOMContentLoaded", bootstrap);

    async function bootstrap() {
        initializeLanguage();
        bindGlobalEvents();

        const parameters = new URLSearchParams(window.location.search);
        if (parameters.has("socialCode")) {
            showAuth();
            await handleSocialLoginCode(
                parameters.get("socialCode")
            );
            return;
        }
        if (parameters.has("oauthError")) {
            showAuth();
            setAuthMessage(
                "Không thể đăng nhập bằng Google. Vui lòng thử lại.",
                "error"
            );
            clearAuthQueryParameters();
            return;
        }
        if (parameters.has("token")) {
            showAuth();
            await handleVerificationToken();
            return;
        }
        if (parameters.has("resetToken")) {
            showAuth();
            openPasswordRecoveryModal(
                parameters.get("resetToken")
            );
            window.history.replaceState(
                {},
                document.title,
                window.location.pathname
            );
            return;
        }

        const accessToken = sessionStorage.getItem(TOKEN_KEY);
        if (accessToken) {
            try {
                state.user = await api("/api/auth/me");
                await enterApplication();
                return;
            } catch (error) {
                clearTokens();
            }
        }

        showAuth();
    }

    function initializeLanguage() {
        document.documentElement.lang = state.locale;
        document.querySelectorAll(
            "#auth-language-select, #app-language-select"
        ).forEach((select) => {
            select.value = state.locale;
        });
        translateTree(document.body);
    }

    function t(value) {
        const text = String(value ?? "");
        if (state.locale === "en") {
            const exact = EN_TEXT[text]
                || VI_API_TEXT_EN[text];
            return exact || applyTranslationPatterns(
                text,
                VI_TO_EN_PATTERNS
            );
        }
        const exact = VI_TEXT[text]
            || API_TEXT_VI[text];
        return exact || applyTranslationPatterns(
            text,
            EN_TO_VI_PATTERNS
        );
    }

    function applyTranslationPatterns(text, patterns) {
        for (const [pattern, replacement] of patterns) {
            if (pattern.test(text)) {
                return text.replace(pattern, replacement);
            }
        }
        return text;
    }

    function translateTree(root) {
        if (!root) {
            return;
        }
        const translateNode = (node) => {
            const original = node.textContent;
            const normalized = original.replace(/\s+/g, " ").trim();
            if (!normalized) {
                return;
            }
            const translated = t(normalized);
            if (translated === normalized) {
                return;
            }
            const leading = /^\s/.test(original) ? " " : "";
            const trailing = /\s$/.test(original) ? " " : "";
            node.textContent = `${leading}${translated}${trailing}`;
        };

        if (root.nodeType === Node.TEXT_NODE) {
            translateNode(root);
            return;
        }
        if (!(root instanceof Element) && root !== document.body) {
            return;
        }

        const walker = document.createTreeWalker(
            root,
            NodeFilter.SHOW_TEXT,
            {
                acceptNode(node) {
                    return ["SCRIPT", "STYLE"].includes(
                        node.parentElement?.tagName
                    )
                        ? NodeFilter.FILTER_REJECT
                        : NodeFilter.FILTER_ACCEPT;
                }
            }
        );
        while (walker.nextNode()) {
            translateNode(walker.currentNode);
        }

        const elementsToTranslate = root instanceof Element
            ? [root, ...root.querySelectorAll("*")]
            : [...document.body.querySelectorAll("*")];
        elementsToTranslate.forEach((element) => {
            ["placeholder", "title", "aria-label"].forEach((attributeName) => {
                if (!element.hasAttribute(attributeName)) {
                    return;
                }
                const value = element.getAttribute(attributeName);
                const translated = t(value);
                if (translated !== value) {
                    element.setAttribute(attributeName, translated);
                }
            });
        });
    }

    async function applyLanguage(locale, rerender = true) {
        state.locale = locale === "en" ? "en" : "vi";
        localStorage.setItem(LANGUAGE_KEY, state.locale);
        document.documentElement.lang = state.locale;
        document.querySelectorAll(
            "#auth-language-select, #app-language-select"
        ).forEach((select) => {
            select.value = state.locale;
        });
        translateTree(document.body);

        if (rerender && state.user
            && !elements.appView.classList.contains("hidden")) {
            hydrateUserIdentity();
            renderNavigation();
            await navigate(state.currentView);
        }
    }

    function bindGlobalEvents() {
        document.querySelectorAll(
            "#auth-language-select, #app-language-select"
        ).forEach((select) => {
            select.addEventListener("change", (event) => {
                applyLanguage(event.currentTarget.value);
            });
        });

        document.querySelector("#login-form")
            .addEventListener("submit", handleLogin);
        document.querySelector("#register-form")
            .addEventListener("submit", handleRegister);

        document.querySelectorAll("[data-auth-panel]")
            .forEach((button) => {
                button.addEventListener("click", () => {
                    showAuthPanel(button.dataset.authPanel);
                });
            });

        document.querySelector("#forgot-password-link")
            .addEventListener(
                "click",
                () => openPasswordRecoveryModal()
            );
        document.querySelector("#logout-button")
            .addEventListener("click", logout);
        document.querySelector("#topbar-profile")
            .addEventListener("click", () => navigate("account"));
        document.querySelector("#global-search-form")
            .addEventListener("submit", async (event) => {
                event.preventDefault();
                state.pendingBookSearch = event.currentTarget
                    .querySelector("[name='keyword']").value.trim();
                await navigate("books");
                closeSidebar();
            });

        document.querySelector("#sidebar-open")
            .addEventListener("click", openSidebar);
        document.querySelector("#sidebar-close")
            .addEventListener("click", closeSidebar);
        elements.sidebarOverlay.addEventListener("click", closeSidebar);

        document.addEventListener("keydown", (event) => {
            if (event.key === "Escape" && elements.modalRoot.firstChild) {
                closeModal();
            }
        });
        document.addEventListener("error", (event) => {
            if (!event.target.matches?.("[data-book-cover]")) {
                return;
            }
            const image = event.target;
            const candidates = (image.dataset.coverCandidates || "")
                .split("|")
                .filter(Boolean);
            const nextIndex = Number(image.dataset.coverIndex || "0") + 1;
            if (nextIndex < candidates.length) {
                image.dataset.coverIndex = String(nextIndex);
                image.src = candidates[nextIndex];
                return;
            }
            image.hidden = true;
            image.closest("[data-cover-frame]")
                ?.classList.add("is-empty");
        }, true);
        document.addEventListener("load", (event) => {
            if (!event.target.matches?.("[data-book-cover]")) {
                return;
            }
            event.target.hidden = false;
            event.target.closest("[data-cover-frame]")
                ?.classList.remove("is-empty");
        }, true);

        new MutationObserver((mutations) => {
            mutations.forEach((mutation) => {
                mutation.addedNodes.forEach((node) => {
                    if (node.nodeType === Node.ELEMENT_NODE
                        || node.nodeType === Node.TEXT_NODE) {
                        translateTree(node);
                    }
                });
            });
        }).observe(document.body, {
            childList: true,
            subtree: true
        });

        setupPasswordToggles();
    }

    function setupPasswordToggles() {
        const enhance = (root) => {
            const inputs = [];
            if (root instanceof Element
                && root.matches("input[type='password']")) {
                inputs.push(root);
            }
            if (root.querySelectorAll) {
                inputs.push(...root.querySelectorAll(
                    "input[type='password']"
                ));
            }

            inputs.forEach((input) => {
                if (input.closest(".password-control")) {
                    return;
                }

                const wrapper = document.createElement("span");
                wrapper.className = "password-control";
                input.parentNode.insertBefore(wrapper, input);
                wrapper.appendChild(input);

                const button = document.createElement("button");
                button.type = "button";
                button.className = "password-toggle";
                button.setAttribute("aria-label", "Hiển thị mật khẩu");
                button.setAttribute("aria-pressed", "false");
                button.innerHTML = icon("i-eye");
                wrapper.appendChild(button);

                button.addEventListener("click", () => {
                    const reveal = input.type === "password";
                    input.type = reveal ? "text" : "password";
                    button.setAttribute(
                        "aria-label",
                        reveal ? "Ẩn mật khẩu" : "Hiển thị mật khẩu"
                    );
                    button.setAttribute(
                        "aria-pressed",
                        String(reveal)
                    );
                    button.innerHTML = icon(
                        reveal ? "i-eye-off" : "i-eye"
                    );
                    input.focus({ preventScroll: true });
                });
            });
        };

        enhance(document);
        new MutationObserver((mutations) => {
            mutations.forEach((mutation) => {
                mutation.addedNodes.forEach((node) => {
                    if (node.nodeType === Node.ELEMENT_NODE) {
                        enhance(node);
                    }
                });
            });
        }).observe(document.body, {
            childList: true,
            subtree: true
        });
    }

    async function handleVerificationToken() {
        const parameters = new URLSearchParams(window.location.search);
        const token = parameters.get("token");
        if (!token) {
            return;
        }

        try {
            const result = await api(
                `/api/auth/verify-email?token=${encodeURIComponent(token)}`,
                {},
                false
            );
            setAuthMessage(result.message, "success");
        } catch (error) {
            setAuthMessage(error.message, "error");
        } finally {
            window.history.replaceState(
                {},
                document.title,
                window.location.pathname
            );
        }
    }
    // Xử lý mã đăng nhập xã hội
    async function handleSocialLoginCode(code) {
        try {
            const tokens = await api(
                "/api/auth/social/exchange",
                {
                    method: "POST",
                    body: JSON.stringify({ code })
                },
                false
            );

            storeTokens(tokens);
            state.user = await api("/api/auth/me");
            clearAuthQueryParameters();
            await enterApplication();
            toast(
                "Đăng nhập Google thành công",
                "Chào mừng bạn trở lại."
            );
        } catch (error) {
            clearTokens();
            clearAuthQueryParameters();
            setAuthMessage(
                error.message
                || "Không thể đăng nhập bằng Google. Vui lòng thử lại.",
                "error"
            );
        }
    }

    function clearAuthQueryParameters() {
        window.history.replaceState(
            {},
            document.title,
            window.location.pathname
        );
    }

    async function handleLogin(event) {
        event.preventDefault();
        const form = event.currentTarget;
        const submitButton = form.querySelector("[type='submit']");
        setButtonLoading(submitButton, true, "Đang đăng nhập...");

        try {
            const data = formDataToObject(form);
            const tokens = await api(
                "/api/auth/login",
                {
                    method: "POST",
                    body: JSON.stringify(data)
                },
                false
            );

            storeTokens(tokens);
            state.user = await api("/api/auth/me");
            form.reset();
            await enterApplication();
            toast("Đăng nhập thành công", "Chào mừng bạn trở lại.");
        } catch (error) {
            setAuthMessage(error.message, "error");
        } finally {
            setButtonLoading(submitButton, false);
        }
    }

    async function handleRegister(event) {
        event.preventDefault();
        const form = event.currentTarget;
        const submitButton = form.querySelector("[type='submit']");
        setButtonLoading(submitButton, true, "Đang tạo...");

        try {
            const result = await api(
                "/api/auth/register",
                {
                    method: "POST",
                    body: JSON.stringify(formDataToObject(form))
                },
                false
            );
            form.reset();
            showAuthPanel("login");
            setAuthMessage(
                result.message
                || "Đăng ký thành công. Hãy kiểm tra email để xác minh.",
                "success"
            );
        } catch (error) {
            setAuthMessage(formatApiError(error), "error");
        } finally {
            setButtonLoading(submitButton, false);
        }
    }

    function showAuthPanel(panel) {
        document.querySelector("#login-panel")
            .classList.toggle("hidden", panel !== "login");
        document.querySelector("#register-panel")
            .classList.toggle("hidden", panel !== "register");
        elements.authMessage.classList.add("hidden");
    }

    function showAuth() {
        elements.loading.classList.add("hidden");
        elements.appView.classList.add("hidden");
        elements.authView.classList.remove("hidden");
        showAuthPanel("login");
    }

    async function enterApplication() {
        state.isAdmin = state.user.roles.includes("ROLE_ADMIN");
        state.profile = await safeApi("/api/profile");
        elements.loading.classList.add("hidden");
        elements.authView.classList.add("hidden");
        elements.appView.classList.remove("hidden");
        hydrateUserIdentity();
        renderNavigation();
        await navigate("dashboard");
        if (state.profile
            && (!state.profile.phone || !state.profile.dateOfBirth)) {
            toast(
                t("Thông tin cá nhân chưa đầy đủ"),
                t("Vui lòng cập nhật số điện thoại và ngày sinh để thư viện có thể liên hệ và hỗ trợ việc mượn trả sách.")
            );
        }
    }

    function hydrateUserIdentity() {
        const name = state.profile?.fullName
            || state.profile?.username
            || state.user.username
            || state.user.email;
        const role = t(state.isAdmin ? "Quản trị viên" : "Thành viên");

        document.querySelector("#sidebar-user-name").textContent = name;
        document.querySelector("#sidebar-user-role").textContent = role;
        document.querySelector("#sidebar-avatar").innerHTML = icon("i-user");
    }

    function renderNavigation() {
        const generalItems = [
            navItem("dashboard", t("Trang chủ")),
            navItem("books", t("Kho sách")),
            navItem("borrowings",
                t(state.isAdmin ? "Mượn & trả" : "Sách của tôi"))
        ];

        const adminItems = state.isAdmin
            ? [
                navItem("members", t("Thành viên")),
                navItem("system", t("Hệ thống"))
            ]
            : [];

        elements.sidebarNav.innerHTML = `
            ${generalItems.join("")}
            ${adminItems.join("")}
            ${navItem("account", t("Tài khoản"))}
        `;

        elements.sidebarNav.querySelectorAll("[data-view]")
            .forEach((button) => {
                button.addEventListener("click", () => {
                    navigate(button.dataset.view);
                    closeSidebar();
                });
            });
    }

    function navItem(view, label) {
        return `
            <button type="button" class="nav-item"
                    data-view="${view}">
                <span>${label}</span>
            </button>
        `;
    }

    async function navigate(view) {
        if (!viewMeta[view]) {
            view = "dashboard";
        }
        if (view === "bookDetail" && !state.selectedBookId) {
            view = "books";
        }
        if (!state.isAdmin && ["members", "system"].includes(view)) {
            view = "dashboard";
        }

        state.currentView = view;
        const [eyebrow, title] = viewMeta[view];
        elements.pageEyebrow.textContent = t(eyebrow);
        elements.pageTitle.textContent = t(title);
        elements.sidebarNav.querySelectorAll("[data-view]")
            .forEach((item) => {
                item.classList.toggle(
                    "active",
                    item.dataset.view === view
                );
            });
        elements.pageContent.innerHTML = pageSkeleton();

        const renderers = {
            dashboard: renderDashboard,
            books: renderBooksPage,
            bookDetail: renderBookDetailPage,
            members: renderMembersPage,
            borrowings: renderBorrowingsPage,
            system: renderSystemPage,
            account: renderAccountPage
        };

        try {
            await renderers[view]();
            translateTree(elements.pageContent);
        } catch (error) {
            renderPageError(error);
        }
    }

    async function renderDashboard() {
        const requests = [
            safeApi("/api/books?page=0&size=10&active=true&sortBy=title&direction=asc"),
            safeApi(state.isAdmin
                ? "/api/admin/borrowings?page=0&size=5"
                : "/api/borrowings/my?page=0&size=5")
        ];

        if (state.isAdmin) {
            requests.push(
                safeApi("/api/admin/members?page=0&size=1"),
                safeApi("/api/admin/system-config")
            );
        }

        const [books, borrowings, members, systemConfig] =
            await Promise.all(requests);
        state.dashboardShelfBooks =
            await loadDashboardShelfBooks(books);
        state.dashboardShelfIndex = 0;

        if (systemConfig) {
            state.maintenance = systemConfig;
            updateMaintenancePill(systemConfig.maintenanceMode);
        }

        const activeLoans = (borrowings?.content || [])
            .filter((item) => item.status === "BORROWED").length;
        const overdueLoans = (borrowings?.content || [])
            .filter((item) => item.overdue).length;

        const stats = state.isAdmin
            ? [
                ["Đầu sách", books?.totalElements ?? "—",
                    "Trong danh mục"],
                ["Thành viên", members?.totalElements ?? "—",
                    "Tài khoản đang quản lý"],
                ["Lượt mượn", borrowings?.totalElements ?? "—",
                    "Tổng lịch sử lưu thông"],
                ["Quá hạn", overdueLoans,
                    overdueLoans ? "Cần xử lý" : "Không có cảnh báo"]
            ]
            : [
                ["Sách", books?.totalElements ?? "—",
                    "Có thể tra cứu"],
                ["Lịch sử", borrowings?.totalElements ?? "—",
                    "Tổng lượt mượn"],
                ["Đang mượn", activeLoans,
                    "Chưa hoàn trả"],
                ["Quá hạn", overdueLoans,
                    overdueLoans ? "Vui lòng hoàn trả" : "Không có sách quá hạn"]
            ];

        elements.pageContent.innerHTML = `
            <section class="library-home">
                <section class="library-hero">
                    <div class="library-hero-copy">
                        <span class="eyebrow">
                            ${state.isAdmin
                ? "Kho sách & lưu thông"
                : `Xin chào ${escapeHtml(displayName())}`}
                        </span>
                        <h2>${state.isAdmin
                ? "Tìm nhanh trong toàn bộ thư viện"
                : "Bạn muốn đọc gì hôm nay?"}</h2>
                        <p>${state.isAdmin
                ? "Tra cứu đầu sách trước khi cập nhật kho, mượn hoặc trả."
                : "Tìm theo tên sách, tác giả hoặc mã ISBN trong bộ sưu tập."}</p>
                        <form id="dashboard-search-form"
                              class="library-hero-search" role="search">
                            <input name="keyword" autocomplete="off"
                                   placeholder="Nhập tên sách, tác giả hoặc ISBN">
                            <button type="submit">Tìm trong thư viện</button>
                        </form>
                    </div>
                    ${dashboardHeroShelf(books?.content || [])}
                </section>

                <section class="shelf-section">
                    <div class="shelf-heading">
                        <div>
                            <span class="section-kicker">Bộ sưu tập</span>
                            <h2>Sách trên kệ</h2>
                        </div>
                        <div class="shelf-heading-actions">
                            <span class="shelf-carousel-controls">
                                <button type="button"
                                        class="icon-button"
                                        data-shelf-step="-1"
                                        aria-label="Sách trước">
                                    ${icon("i-chevron-left")}
                                </button>
                                <button type="button"
                                        class="icon-button"
                                        data-shelf-step="1"
                                        aria-label="Sách tiếp theo">
                                    ${icon("i-chevron-right")}
                                </button>
                            </span>
                            <button class="text-button"
                                    data-go-view="books">
                                Xem toàn bộ kho sách
                            </button>
                        </div>
                    </div>
                    <div id="dashboard-shelf-host">
                        ${dashboardBookShelf(
                    dashboardShelfWindow()
                )}
                    </div>
                </section>

                <section class="library-metrics">
                    ${stats.map(statCard).join("")}
                </section>

                <section class="circulation-section">
                    <div class="shelf-heading">
                        <div>
                            <span class="section-kicker">Lưu thông</span>
                            <h2>Hoạt động gần đây</h2>
                        </div>
                        <button class="text-button"
                                data-go-view="borrowings">
                            Xem lịch sử mượn trả
                        </button>
                    </div>
                    <div class="card">
                        ${borrowingTable(
                    borrowings?.content || [],
                    { compact: true, canReturn: false }
                )}
                    </div>
                </section>
            </section>
        `;

        document.querySelector("#dashboard-search-form")
            .addEventListener("submit", async (event) => {
                event.preventDefault();
                state.pendingBookSearch = event.currentTarget
                    .querySelector("[name='keyword']").value.trim();
                await navigate("books");
            });

        elements.pageContent.querySelectorAll("[data-go-view]")
            .forEach((button) => {
                button.addEventListener("click", () => {
                    navigate(button.dataset.goView);
                });
            });
        elements.pageContent.querySelectorAll("[data-book-query]")
            .forEach((button) => {
                button.addEventListener("click", async () => {
                    state.pendingBookSearch = button.dataset.bookQuery;
                    await navigate("books");
                });
            });
        elements.pageContent.querySelectorAll("[data-shelf-step]")
            .forEach((button) => {
                button.addEventListener("click", () => {
                    shiftDashboardShelf(
                        Number(button.dataset.shelfStep)
                    );
                });
            });
        bindBookDetailLinks(elements.pageContent);
    }

    async function loadDashboardShelfBooks(firstPage) {
        if (!firstPage?.content?.length) {
            return [];
        }
        if (firstPage.totalPages <= 1) {
            return firstPage.content;
        }

        const remainingPages = await Promise.all(
            Array.from(
                { length: firstPage.totalPages - 1 },
                (_, index) => safeApi(
                    `/api/books?page=${index + 1}`
                    + "&size=10&active=true"
                    + "&sortBy=title&direction=asc"
                )
            )
        );

        return [
            ...firstPage.content,
            ...remainingPages
                .filter(Boolean)
                .flatMap((page) => page.content || [])
        ];
    }

    function dashboardShelfWindow() {
        const books = state.dashboardShelfBooks;
        if (!books.length) {
            return [];
        }
        const visibleCount = Math.min(8, books.length);
        return Array.from(
            { length: visibleCount },
            (_, offset) => books[
                (state.dashboardShelfIndex + offset)
                % books.length
            ]
        );
    }

    function shiftDashboardShelf(step) {
        const books = state.dashboardShelfBooks;
        if (books.length <= 1) {
            return;
        }
        state.dashboardShelfIndex =
            (state.dashboardShelfIndex + step + books.length)
            % books.length;

        const host = document.querySelector(
            "#dashboard-shelf-host"
        );
        if (!host) {
            return;
        }
        host.innerHTML = dashboardBookShelf(
            dashboardShelfWindow()
        );
        bindBookDetailLinks(host);
    }

    function dashboardHeroShelf(books) {
        const items = books.slice(0, 4);
        if (!items.length) {
            return "";
        }
        return `
            <div class="library-hero-shelf" aria-hidden="true">
                ${items.map((book) =>
            bookCover(
                book,
                "book-cover-frame--hero"
            )
        ).join("")}
            </div>
        `;
    }

    function dashboardBookShelf(books) {
        if (!books.length) {
            return `
                <div class="book-shelf book-shelf--empty" aria-hidden="true">
                    ${Array.from({ length: 7 }, (_, index) => `
                        <span class="book-cover-frame is-empty
                            ${index % 3 === 0 ? "book-cover-frame--short" : ""}">
                            <span class="book-cover-empty"></span>
                        </span>
                    `).join("")}
                </div>
            `;
        }

        return `
            <div class="book-shelf">
                ${books.slice(0, 8).map((book) => `
                    <button type="button" class="shelf-book"
                            data-book-detail="${book.id}">
                        ${bookCover(book, "book-cover-frame--shelf")}
                        <span class="shelf-book-title">
                            ${escapeHtml(book.title)}
                        </span>
                        <span class="shelf-book-author">
                            ${escapeHtml(authorNames(book))}
                        </span>
                    </button>
                `).join("")}
            </div>
        `;
    }

    function statCard([label, value, note]) {
        return `
            <article class="stat-card">
                <div class="stat-card-head">
                    <span class="stat-card-label">${escapeHtml(label)}</span>
                </div>
                <strong class="stat-value">${escapeHtml(String(value))}</strong>
                <span class="stat-note">${escapeHtml(note)}</span>
            </article>
        `;
    }

    function quickAction(title, subtitle, view) {
        return `
            <button type="button" class="activity-item text-button"
                    data-go-view="${view}">
                <span class="activity-content">
                    <strong>${escapeHtml(title)}</strong>
                    <small>${escapeHtml(subtitle)}</small>
                </span>
                <span class="activity-link">Mở</span>
            </button>
        `;
    }

    async function renderBooksPage() {
        await ensureCategories();
        elements.pageContent.innerHTML = `
            <section class="page-section">
                <div class="section-heading">
                    <div>
                        <h2>${state.isAdmin
                ? "Danh mục thư viện"
                : "Khám phá kho sách"}</h2>
                        <p>${state.isAdmin
                ? "Tra cứu, cập nhật và bổ sung tài liệu vào bộ sưu tập."
                : "Duyệt sách theo từ khóa, tác giả, danh mục và tình trạng."}</p>
                    </div>
                    ${state.isAdmin
                ? `<div class="action-row">
                            <button id="import-books-button"
                                    class="button button--secondary">
                                Nhập CSV
                            </button>
                            <button id="create-book-button"
                                    class="button button--primary">
                                Thêm sách
                            </button>
                           </div>`
                : ""}
                </div>

                <div class="catalog-layout">
                    <aside class="catalog-filter-sidebar">
                        <form id="book-filter-form" class="catalog-filter-form">
                            <div class="catalog-filter-title">
                                <h3>Lọc kết quả</h3>
                                <button type="reset" class="text-button"
                                        id="clear-book-filters">
                                    Xóa lọc
                                </button>
                            </div>
                            <label class="field">
                                <span>Từ khóa</span>
                                <input name="keyword"
                                       value="${attribute(state.pendingBookSearch)}"
                                       placeholder="Tên, ISBN, tác giả...">
                            </label>
                            <label class="field">
                                <span>Danh mục</span>
                                <select name="categoryId">
                                    <option value="">Tất cả danh mục</option>
                                    ${state.categories.map((category) => `
                                        <option value="${category.id}">
                                            ${escapeHtml(t(category.name))}
                                        </option>
                                    `).join("")}
                                </select>
                            </label>
                            <label class="field">
                                <span>Nhà xuất bản</span>
                                <input name="publisher"
                                       placeholder="Nhập nhà xuất bản">
                            </label>
                            <label class="field">
                                <span>Tình trạng</span>
                                <select name="availableOnly">
                                    <option value="">Tất cả</option>
                                    <option value="true">Có thể mượn</option>
                                    <option value="false">Đang hết sách</option>
                                </select>
                            </label>
                            <button type="submit"
                                    class="button button--primary button--full">
                                Áp dụng bộ lọc
                            </button>
                        </form>
                    </aside>
                    <div class="catalog-results">
                        <div id="books-result">${pageSkeleton(3)}</div>
                    </div>
                </div>
            </section>
        `;

        document.querySelector("#book-filter-form")
            .addEventListener("submit", (event) => {
                event.preventDefault();
                loadBooks(0);
            });
        document.querySelector("#clear-book-filters")
            .addEventListener("click", () => {
                setTimeout(() => loadBooks(0));
            });

        document.querySelector("#create-book-button")
            ?.addEventListener("click", () => openBookModal());
        document.querySelector("#import-books-button")
            ?.addEventListener("click", openImportModal);

        await loadBooks(0);
        state.pendingBookSearch = "";
    }

    async function loadBooks(page) {
        const resultHost = document.querySelector("#books-result");
        resultHost.innerHTML = pageSkeleton(3);

        try {
            const form = document.querySelector("#book-filter-form");
            const parameters = form
                ? queryFromForm(form)
                : new URLSearchParams();
            parameters.set("page", page);
            parameters.set("size", 10);
            parameters.set("sortBy", "title");
            parameters.set("direction", "asc");
            if (!state.isAdmin) {
                parameters.set("active", "true");
            }

            const response = await api(
                `/api/books?${parameters.toString()}`
            );
            state.books.clear();
            response.content.forEach((book) => {
                state.books.set(String(book.id), book);
            });

            resultHost.innerHTML = state.isAdmin
                ? bookAdminTable(response)
                : bookCatalogue(response);
            bindBookActions(response);
        } catch (error) {
            resultHost.innerHTML = inlineError(error);
        }
    }

    function bookCatalogue(response) {
        if (!response.content.length) {
            return emptyState(
                "i-book",
                "Không tìm thấy sách",
                "Thử thay đổi từ khóa hoặc bộ lọc tìm kiếm."
            );
        }

        return `
            <div class="catalog-result-heading">
                <p>
                    <strong>${response.totalElements}</strong>
                    đầu sách phù hợp
                </p>
            </div>
            <div class="book-grid">
                ${response.content.map((book) => `
                    <article class="book-card">
                        <div class="book-card-top">
                            <button type="button"
                                    class="book-cover-link"
                                    data-book-detail="${book.id}"
                                    aria-label="Mở chi tiết ${attribute(book.title)}">
                                ${bookCover(
            book,
            "book-cover-frame--catalog"
        )}
                            </button>
                            ${availabilityBadge(book)}
                        </div>
                        <button type="button"
                                class="book-title-link"
                                data-book-detail="${book.id}">
                            <h3 title="${attribute(book.title)}">
                                ${escapeHtml(book.title)}
                            </h3>
                        </button>
                        <p class="book-authors">
                            ${escapeHtml(authorNames(book))}
                        </p>
                        <div class="book-meta">
                            <div>
                                <small>Danh mục</small>
                                <strong>${escapeHtml(
            t(book.category?.name || "Khác")
        )}</strong>
                            </div>
                            <button class="button button--primary button--small"
                                    data-borrow-book="${book.id}"
                                    ${book.availableQuantity < 1
                || !book.active ? "disabled" : ""}>
                                Mượn sách
                            </button>
                        </div>
                    </article>
                `).join("")}
            </div>
            ${pagination(response, "books")}
        `;
    }

    function bookAdminTable(response) {
        if (!response.content.length) {
            return `
                <div class="card">
                    ${emptyState(
                "i-book",
                "Chưa có dữ liệu sách",
                "Thêm sách mới hoặc nhập danh sách từ CSV."
            )}
                </div>
            `;
        }

        return `
            <div class="catalog-result-heading">
                <p>
                    <strong>${response.totalElements}</strong>
                    đầu sách trong danh mục
                </p>
            </div>
            <div class="card">
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Sách</th>
                                <th>Danh mục</th>
                                <th>Nhà xuất bản</th>
                                <th>Số lượng</th>
                                <th>Trạng thái</th>
                                <th class="text-right">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${response.content.map((book) => `
                                <tr>
                                    <td>
                                        <span class="table-book">
                                            ${bookCover(
            book,
            "book-cover-frame--table"
        )}
                                            <span class="table-primary">
                                                <strong>${escapeHtml(book.title)}</strong>
                                                <small>${escapeHtml(book.isbn)}
                                                    · ${escapeHtml(authorNames(book))}
                                                </small>
                                            </span>
                                        </span>
                                    </td>
                                    <td>${escapeHtml(
            t(book.category?.name || "—")
        )}</td>
                                    <td>${escapeHtml(book.publisher || "—")}</td>
                                    <td>
                                        ${book.availableQuantity}
                                        / ${book.totalQuantity}
                                    </td>
                                    <td>${book.active
                ? availabilityBadge(book)
                : badge("Ngừng hoạt động", "neutral")}</td>
                                    <td>
                                        <span class="table-actions">
                                            <button class="icon-button"
                                                    data-book-detail="${book.id}"
                                                    title="Mở chi tiết">
                                                ${icon("i-book")}
                                            </button>
                                            <button class="icon-button"
                                                    data-edit-book="${book.id}"
                                                    title="Chỉnh sửa">
                                                ${icon("i-edit")}
                                            </button>
                                            <button class="icon-button danger"
                                                    data-delete-book="${book.id}"
                                                    title="Ngừng hoạt động">
                                                ${icon("i-trash")}
                                            </button>
                                        </span>
                                    </td>
                                </tr>
                            `).join("")}
                        </tbody>
                    </table>
                </div>
                ${pagination(response, "books")}
            </div>
        `;
    }

    function bindBookActions(response) {
        document.querySelectorAll("[data-books-page]")
            .forEach((button) => {
                button.addEventListener("click", () => {
                    loadBooks(Number(button.dataset.booksPage));
                });
            });

        document.querySelectorAll("[data-borrow-book]")
            .forEach((button) => {
                button.addEventListener("click", async () => {
                    await borrowBook(button.dataset.borrowBook, button);
                });
            });

        document.querySelectorAll("[data-edit-book]")
            .forEach((button) => {
                button.addEventListener("click", () => {
                    openBookModal(
                        state.books.get(button.dataset.editBook)
                    );
                });
            });

        document.querySelectorAll("[data-delete-book]")
            .forEach((button) => {
                button.addEventListener("click", () => {
                    confirmDeleteBook(
                        state.books.get(button.dataset.deleteBook)
                    );
                });
            });
        bindBookDetailLinks(elements.pageContent);
    }

    async function borrowBook(bookId, button) {
        setButtonLoading(button, true, "Đang mượn...");
        try {
            const result = await api("/api/borrowings", {
                method: "POST",
                body: JSON.stringify({ bookId: Number(bookId) })
            });
            toast(
                "Mượn sách thành công",
                `Hạn trả: ${formatDate(result.dueAt)}`
            );
            if (state.currentView === "books") {
                await loadBooks(0);
            }
            return true;
        } catch (error) {
            toast("Không thể mượn sách", error.message, "error");
            setButtonLoading(button, false);
            return false;
        }
    }

    function bindBookDetailLinks(root = document) {
        root.querySelectorAll("[data-book-detail]")
            .forEach((button) => {
                if (button.dataset.detailBound === "true") {
                    return;
                }
                button.dataset.detailBound = "true";
                button.addEventListener("click", async () => {
                    state.selectedBookId = Number(
                        button.dataset.bookDetail
                    );
                    await navigate("bookDetail");
                });
            });
    }

    async function renderBookDetailPage() {
        const bookId = Number(state.selectedBookId);
        if (!Number.isInteger(bookId) || bookId < 1) {
            state.selectedBookId = null;
            await navigate("books");
            return;
        }

        const [book, savedStatus] = await Promise.all([
            api(`/api/books/${bookId}`),
            safeApi(`/api/saved-books/${bookId}/status`)
        ]);
        state.books.set(String(book.id), book);

        const isSaved = Boolean(savedStatus?.saved);
        const canBorrow = !state.isAdmin
            && book.active
            && book.availableQuantity > 0;

        elements.pageContent.innerHTML = `
            <section class="page-section book-detail-page">
                <button type="button"
                        class="book-detail-back text-button"
                        data-back-to-books>
                    ${icon("i-chevron-left")} Trở lại kho sách
                </button>

                <div class="book-detail-hero">
                    <aside class="book-detail-cover-panel">
                        ${bookCover(
            book,
            "book-cover-frame--detail"
        )}
                        <div class="book-detail-availability">
                            ${availabilityBadge(book)}
                            <span>
                                ${book.availableQuantity}
                                / ${book.totalQuantity} cuốn
                            </span>
                        </div>
                    </aside>

                    <div class="book-detail-summary">
                        <span class="section-kicker">
                            ${escapeHtml(
            t(book.category?.name || "Khác")
        )}
                        </span>
                        <h2>${escapeHtml(book.title)}</h2>
                        <p class="book-detail-authors">
                            ${escapeHtml(authorNames(book))}
                        </p>

                        <div class="book-detail-actions">
                            <button type="button"
                                    class="button button--secondary
                                        ${isSaved ? "is-saved" : ""}"
                                    data-toggle-saved-book
                                    data-saved="${isSaved}">
                                ${icon("i-bookmark")}
                                ${isSaved ? "Đã lưu" : "Lưu"}
                            </button>
                            ${state.isAdmin
                ? `<button type="button"
                                           class="button button--primary"
                                           data-edit-detail-book>
                                       ${icon("i-edit")} Chỉnh sửa sách
                                   </button>`
                : `<button type="button"
                                           class="button button--primary"
                                           data-detail-borrow="${book.id}"
                                           ${canBorrow ? "" : "disabled"}>
                                       ${icon("i-borrow")} Mượn sách
                                   </button>`}
                        </div>
                    </div>
                </div>

                <div class="book-detail-content-grid">
                    <article class="book-detail-description card">
                        <div class="card-heading">
                            <div>
                                <span class="section-kicker">
                                    Nội dung
                                </span>
                                <h3>Giới thiệu về cuốn sách</h3>
                            </div>
                        </div>
                        <div class="card-body">
                            <p>${escapeHtml(
                    book.description
                    || "Mô tả đang được cập nhật."
                )}</p>
                        </div>
                    </article>

                    <aside class="book-detail-facts card">
                        <div class="card-heading">
                            <div>
                                <span class="section-kicker">
                                    Thông tin
                                </span>
                                <h3>Thông tin xuất bản</h3>
                            </div>
                        </div>
                        <dl class="book-facts-list">
                            <div>
                                <dt>ISBN</dt>
                                <dd>${escapeHtml(book.isbn)}</dd>
                            </div>
                            <div>
                                <dt>Tác giả</dt>
                                <dd>${escapeHtml(authorNames(book))}</dd>
                            </div>
                            <div>
                                <dt>Nhà xuất bản</dt>
                                <dd>${escapeHtml(
                    book.publisher || "—"
                )}</dd>
                            </div>
                            <div>
                                <dt>Ngày phát hành</dt>
                                <dd>${formatDate(
                    book.publishedDate
                )}</dd>
                            </div>
                            <div>
                                <dt>Danh mục</dt>
                                <dd>${escapeHtml(
                    t(book.category?.name || "Khác")
                )}</dd>
                            </div>
                            <div>
                                <dt>Số bản hiện có</dt>
                                <dd>${book.availableQuantity}</dd>
                            </div>
                            <div>
                                <dt>Tổng số bản</dt>
                                <dd>${book.totalQuantity}</dd>
                            </div>
                        </dl>
                    </aside>
                </div>
            </section>
        `;

        document.querySelector("[data-back-to-books]")
            .addEventListener(
                "click",
                () => navigate("books")
            );

        document.querySelector("[data-toggle-saved-book]")
            .addEventListener("click", async (event) => {
                const button = event.currentTarget;
                const wasSaved =
                    button.dataset.saved === "true";
                setButtonLoading(
                    button,
                    true,
                    wasSaved
                        ? "Đang bỏ lưu..."
                        : "Đang lưu..."
                );
                try {
                    await api(`/api/saved-books/${book.id}`, {
                        method: wasSaved ? "DELETE" : "POST"
                    });
                    toast(
                        wasSaved
                            ? "Đã bỏ khỏi danh sách lưu"
                            : "Đã lưu sách",
                        book.title
                    );
                    await renderBookDetailPage();
                } catch (error) {
                    toast(
                        "Không thể cập nhật",
                        error.message,
                        "error"
                    );
                    setButtonLoading(button, false);
                }
            });

        document.querySelector("[data-detail-borrow]")
            ?.addEventListener("click", async (event) => {
                const borrowed = await borrowBook(
                    book.id,
                    event.currentTarget
                );
                if (borrowed) {
                    await renderBookDetailPage();
                }
            });

        document.querySelector("[data-edit-detail-book]")
            ?.addEventListener(
                "click",
                () => openBookModal(book)
            );
    }

    async function openBookModal(book = null) {
        await ensureCategories();
        const editing = Boolean(book);
        openModal({
            title: editing ? "Cập nhật sách" : "Thêm sách mới",
            subtitle: "Các trường có dấu * là bắt buộc.",
            large: true,
            body: `
                <form id="book-form" class="form-grid">
                    <label class="field">
                        <span>ISBN *</span>
                        <input name="isbn" required maxlength="20"
                               value="${attribute(book?.isbn)}">
                    </label>
                    <label class="field">
                        <span>Tên sách *</span>
                        <input name="title" required maxlength="255"
                               value="${attribute(book?.title)}">
                    </label>
                    <label class="field">
                        <span>Danh mục *</span>
                        <select name="categoryId" required>
                            <option value="">Chọn danh mục</option>
                            ${state.categories.map((category) => `
                                <option value="${category.id}"
                                    ${book?.category?.id === category.id
                    ? "selected" : ""}>
                                    ${escapeHtml(t(category.name))}
                                </option>
                            `).join("")}
                        </select>
                    </label>
                    <label class="field">
                        <span>Tác giả *</span>
                        <input name="authorNames" required
                               placeholder="Nguyễn Nhật Ánh, Tô Hoài"
                               value="${attribute(
                        (book?.authors || [])
                            .map((author) => author.name)
                            .join(", ")
                    )}">
                        <small>Phân cách nhiều tác giả bằng dấu phẩy.</small>
                    </label>
                    <label class="field">
                        <span>Nhà xuất bản</span>
                        <input name="publisher" maxlength="255"
                               value="${attribute(book?.publisher)}">
                    </label>
                    <label class="field">
                        <span>Ngày xuất bản</span>
                        <input name="publishedDate" type="date"
                               max="${today()}"
                               value="${attribute(book?.publishedDate)}">
                    </label>
                    <label class="field">
                        <span>Tổng số lượng *</span>
                        <input name="totalQuantity" type="number"
                               min="0" required
                               value="${book?.totalQuantity ?? 1}">
                    </label>
                    ${editing
                    ? `<label class="field">
                            <span>Trạng thái *</span>
                            <select name="active" required>
                                <option value="true"
                                    ${book.active ? "selected" : ""}>
                                    Đang hoạt động
                                </option>
                                <option value="false"
                                    ${!book.active ? "selected" : ""}>
                                    Ngừng hoạt động
                                </option>
                            </select>
                           </label>`
                    : ""}
                    <label class="field field--full">
                        <span>Mô tả</span>
                        <textarea name="description" maxlength="2000"
                                  placeholder="Mô tả ngắn về cuốn sách">${escapeHtml(
                        book?.description || ""
                    )}</textarea>
                    </label>
                    <p id="book-form-error"
                       class="form-error field--full hidden"></p>
                </form>
            `,
            confirmText: editing ? "Lưu thay đổi" : "Thêm sách",
            onConfirm: async (button) => {
                const form = document.querySelector("#book-form");
                if (!form.reportValidity()) {
                    return;
                }
                setButtonLoading(button, true, "Đang lưu...");
                const raw = formDataToObject(form);
                const payload = {
                    isbn: raw.isbn.trim(),
                    title: raw.title.trim(),
                    description: emptyToNull(raw.description),
                    publisher: emptyToNull(raw.publisher),
                    publishedDate: emptyToNull(raw.publishedDate),
                    totalQuantity: Number(raw.totalQuantity),
                    categoryId: Number(raw.categoryId),
                    authorNames: raw.authorNames
                        .split(",")
                        .map((name) => name.trim())
                        .filter(Boolean)
                };
                if (editing) {
                    payload.active = raw.active === "true";
                }

                try {
                    await api(
                        editing
                            ? `/api/books/${book.id}`
                            : "/api/books",
                        {
                            method: editing ? "PUT" : "POST",
                            body: JSON.stringify(payload)
                        }
                    );
                    closeModal();
                    toast(
                        editing ? "Đã cập nhật sách" : "Đã thêm sách",
                        payload.title
                    );
                    await loadBooks(0);
                } catch (error) {
                    showFormError("#book-form-error", error);
                    setButtonLoading(button, false);
                }
            }
        });
    }

    function confirmDeleteBook(book) {
        openConfirmModal({
            title: "Ngừng hoạt động sách?",
            message: `“${book.title}” sẽ không còn hiển thị cho thành viên.`,
            confirmText: "Ngừng hoạt động",
            danger: true,
            onConfirm: async (button) => {
                setButtonLoading(button, true, "Đang xử lý...");
                try {
                    await api(`/api/books/${book.id}`, {
                        method: "DELETE"
                    });
                    closeModal();
                    toast("Đã cập nhật trạng thái sách", book.title);
                    await loadBooks(0);
                } catch (error) {
                    toast("Không thể cập nhật", error.message, "error");
                    setButtonLoading(button, false);
                }
            }
        });
    }

    function openImportModal() {
        openModal({
            title: "Nhập sách từ CSV",
            subtitle: "Dung lượng tối đa 5 MB, chỉ hỗ trợ file .csv.",
            body: `
                <form id="import-form" class="form-stack">
                    <label class="field">
                        <span>Chọn file CSV *</span>
                        <input name="file" type="file" accept=".csv,text/csv"
                               required>
                        <small>
                            Cột bắt buộc: isbn, title, description,
                            publisher, publishedDate, totalQuantity,
                            category, authors.
                        </small>
                    </label>
                    <p id="import-form-error"
                       class="form-error hidden"></p>
                </form>
            `,
            confirmText: "Nhập dữ liệu",
            onConfirm: async (button) => {
                const form = document.querySelector("#import-form");
                if (!form.reportValidity()) {
                    return;
                }
                const file = form.elements.file.files[0];
                const body = new FormData();
                body.append("file", file);
                setButtonLoading(button, true, "Đang nhập...");

                try {
                    const result = await api("/api/books/import", {
                        method: "POST",
                        body
                    });
                    closeModal();
                    toast(
                        "Nhập CSV thành công",
                        `${result.importedCount} sách đã được thêm.`
                    );
                    await loadBooks(0);
                } catch (error) {
                    showFormError("#import-form-error", error);
                    setButtonLoading(button, false);
                }
            }
        });
    }

    async function renderMembersPage() {
        elements.pageContent.innerHTML = `
            <section class="page-section">
                <div class="section-heading">
                    <div>
                        <h2>Quản lý thành viên</h2>
                        <p>Tìm kiếm và cập nhật tài khoản thư viện.</p>
                    </div>
                    <button id="create-member-button"
                            class="button button--primary">
                        ${icon("i-plus")} Thêm thành viên
                    </button>
                </div>

                <form id="member-filter-form"
                      class="filter-panel filter-panel--members">
                    <label class="field">
                        <span>Từ khóa</span>
                        <span class="search-input-wrap">
                            ${icon("i-search")}
                            <input name="keyword"
                                   placeholder="Tên, email, mã thành viên">
                        </span>
                    </label>
                    <label class="field">
                        <span>Tên thành viên</span>
                        <input name="fullName" placeholder="Tìm gần đúng">
                    </label>
                    <label class="field">
                        <span>Tên sách đã mượn</span>
                        <input name="bookTitle" placeholder="Tên sách">
                    </label>
                    <label class="field">
                        <span>Trạng thái</span>
                        <select name="enabled">
                            <option value="">Tất cả</option>
                            <option value="true">Đang hoạt động</option>
                            <option value="false">Đã vô hiệu hóa</option>
                        </select>
                    </label>
                    <label class="field">
                        <span>Sinh từ ngày</span>
                        <input name="dateOfBirthFrom" type="date"
                               max="${today()}">
                    </label>
                    <label class="field">
                        <span>Sinh đến ngày</span>
                        <input name="dateOfBirthTo" type="date"
                               max="${today()}">
                    </label>
                    <label class="field">
                        <span>Xác minh email</span>
                        <select name="emailVerified">
                            <option value="">Tất cả</option>
                            <option value="true">Đã xác minh</option>
                            <option value="false">Chưa xác minh</option>
                        </select>
                    </label>
                    <label class="field">
                        <span>Tài khoản khóa</span>
                        <select name="accountNonLocked">
                            <option value="">Tất cả</option>
                            <option value="false">Đang khóa</option>
                            <option value="true">Không khóa</option>
                        </select>
                    </label>
                    <button type="submit" class="button button--primary">
                        ${icon("i-search")} Tìm kiếm
                    </button>
                </form>

                <div id="members-result">${pageSkeleton(3)}</div>
            </section>
        `;

        document.querySelector("#member-filter-form")
            .addEventListener("submit", (event) => {
                event.preventDefault();
                loadMembers(0);
            });
        document.querySelector("#create-member-button")
            .addEventListener("click", () => openMemberModal());

        await loadMembers(0);
    }

    async function loadMembers(page) {
        const host = document.querySelector("#members-result");
        host.innerHTML = pageSkeleton(3);
        try {
            const params = queryFromForm(
                document.querySelector("#member-filter-form")
            );
            params.set("page", page);
            params.set("size", 10);
            params.set("sortBy", "id");
            params.set("direction", "desc");
            const response = await api(
                `/api/admin/members?${params.toString()}`
            );
            state.members.clear();
            response.content.forEach((member) => {
                state.members.set(String(member.id), member);
            });
            host.innerHTML = memberTable(response);
            bindMemberActions();
        } catch (error) {
            host.innerHTML = inlineError(error);
        }
    }

    function memberTable(response) {
        if (!response.content.length) {
            return `
                <div class="card">
                    ${emptyState(
                "i-users",
                "Không tìm thấy thành viên",
                "Thử thay đổi các điều kiện tìm kiếm."
            )}
                </div>
            `;
        }

        return `
            <div class="card">
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Thành viên</th>
                                <th>Mã thành viên</th>
                                <th>Ngày sinh</th>
                                <th>Liên hệ</th>
                                <th>Thông tin mật khẩu</th>
                                <th>Trạng thái</th>
                                <th class="text-right">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${response.content.map((member) => `
                                <tr>
                                    <td>
                                        <span class="member-identity">
                                            <span class="avatar avatar--person avatar--small">
                                                ${icon("i-user")}
                                            </span>
                                            <span class="table-primary">
                                                <strong>${escapeHtml(
            member.fullName
            || member.username
        )}</strong>
                                                <small>${escapeHtml(
            member.email
        )}</small>
                                            </span>
                                        </span>
                                    </td>
                                    <td>${escapeHtml(
            member.membershipCode || "—"
        )}</td>
                                    <td>${formatDate(member.dateOfBirth)}</td>
                                    <td>${escapeHtml(member.phone || "—")}</td>
                                    <td>
                                        ${passwordStatusControl(
            member.passwordConfigured !== false
        )}
                                    </td>
                                    <td>${memberStatus(member)}</td>
                                    <td>
                                        <span class="table-actions">
                                            <button class="icon-button"
                                                    data-edit-member="${member.id}"
                                                    title="Chỉnh sửa">
                                                ${icon("i-edit")}
                                            </button>
                                            <button class="icon-button danger"
                                                    data-delete-member="${member.id}"
                                                    title="Vô hiệu hóa">
                                                ${icon("i-trash")}
                                            </button>
                                        </span>
                                    </td>
                                </tr>
                            `).join("")}
                        </tbody>
                    </table>
                </div>
                ${pagination(response, "members")}
            </div>
        `;
    }

    function bindMemberActions() {
        bindPasswordStatusToggles(document);
        document.querySelectorAll("[data-members-page]")
            .forEach((button) => {
                button.addEventListener("click", () => {
                    loadMembers(Number(button.dataset.membersPage));
                });
            });
        document.querySelectorAll("[data-edit-member]")
            .forEach((button) => {
                button.addEventListener("click", () => {
                    openMemberModal(
                        state.members.get(button.dataset.editMember)
                    );
                });
            });
        document.querySelectorAll("[data-delete-member]")
            .forEach((button) => {
                button.addEventListener("click", () => {
                    confirmDeactivateMember(
                        state.members.get(button.dataset.deleteMember)
                    );
                });
            });
    }

    function openMemberModal(member = null) {
        const editing = Boolean(member);
        openModal({
            title: editing ? "Cập nhật thành viên" : "Tạo thành viên",
            subtitle: editing
                ? member.membershipCode
                : "Tài khoản mới có thể đăng nhập ngay.",
            large: true,
            body: `
                <form id="member-form" class="form-grid">
                    ${editing ? "" : `
                        <label class="field">
                            <span>Email *</span>
                            <input name="email" type="email" required
                                   value="${attribute(member?.email)}">
                        </label>
                        <label class="field">
                            <span>Mật khẩu *</span>
                            <input name="password" type="password" required
                                   autocomplete="new-password"
                                   placeholder="Ví dụ: Member@123">
                        </label>
                    `}
                    <label class="field">
                        <span>Họ và tên *</span>
                        <input name="fullName" required maxlength="150"
                               value="${attribute(member?.fullName)}">
                    </label>
                    <label class="field">
                        <span>Ngày sinh</span>
                        <input name="dateOfBirth" type="date"
                               max="${today()}"
                               value="${attribute(member?.dateOfBirth)}">
                    </label>
                    <label class="field">
                        <span>Số điện thoại</span>
                        <input name="phone" maxlength="20"
                               value="${attribute(member?.phone)}">
                    </label>
                    ${editing ? `
                        <label class="field">
                            <span>Thông tin mật khẩu</span>
                            ${passwordStatusControl(
                member.passwordConfigured !== false
            )}
                            <small>
                                Mật khẩu đã được mã hóa và không thể xem.
                            </small>
                        </label>
                        <label class="field">
                            <span>Trạng thái tài khoản *</span>
                            <select name="enabled">
                                <option value="true"
                                    ${member.enabled ? "selected" : ""}>
                                    Đang hoạt động
                                </option>
                                <option value="false"
                                    ${!member.enabled ? "selected" : ""}>
                                    Vô hiệu hóa
                                </option>
                            </select>
                        </label>
                        <label class="field">
                            <span>Khóa tài khoản *</span>
                            <select name="accountNonLocked">
                                <option value="true"
                                    ${member.accountNonLocked
                        ? "selected" : ""}>
                                    Không khóa
                                </option>
                                <option value="false"
                                    ${!member.accountNonLocked
                        ? "selected" : ""}>
                                    Đang khóa
                                </option>
                            </select>
                        </label>
                    ` : ""}
                    <label class="field field--full">
                        <span>Địa chỉ</span>
                        <textarea name="address" maxlength="500">${escapeHtml(
                            member?.address || ""
                        )}</textarea>
                    </label>
                    <p id="member-form-error"
                       class="form-error field--full hidden"></p>
                </form>
            `,
            confirmText: editing ? "Lưu thay đổi" : "Tạo thành viên",
            onConfirm: async (button) => {
                const form = document.querySelector("#member-form");
                if (!form.reportValidity()) {
                    return;
                }
                const raw = formDataToObject(form);
                const payload = {
                    fullName: raw.fullName.trim(),
                    dateOfBirth: emptyToNull(raw.dateOfBirth),
                    phone: raw.phone?.trim() || "",
                    address: emptyToNull(raw.address)
                };
                if (editing) {
                    payload.enabled = raw.enabled === "true";
                    payload.accountNonLocked =
                        raw.accountNonLocked === "true";
                } else {
                    payload.email = raw.email.trim();
                    payload.password = raw.password;
                }

                setButtonLoading(button, true, "Đang lưu...");
                try {
                    await api(
                        editing
                            ? `/api/admin/members/${member.id}`
                            : "/api/admin/members",
                        {
                            method: editing ? "PUT" : "POST",
                            body: JSON.stringify(payload)
                        }
                    );
                    closeModal();
                    toast(
                        editing
                            ? "Đã cập nhật thành viên"
                            : "Đã tạo thành viên",
                        payload.fullName
                    );
                    await loadMembers(0);
                } catch (error) {
                    showFormError("#member-form-error", error);
                    setButtonLoading(button, false);
                }
            }
        });
        bindPasswordStatusToggles(elements.modalRoot);
    }

    function passwordStatusControl(configured) {
        const status = configured ? "Đã thiết lập" : "Chưa thiết lập";
        return `
            <span class="password-status-control"
                  data-password-status="${configured}">
                <span class="password-status-value"
                      data-password-masked>••••••••</span>
                <span class="password-status-value hidden"
                      data-password-revealed>${t(status)}</span>
                <button type="button" class="password-toggle"
                        data-toggle-password-status
                        aria-label="${t("Hiển thị trạng thái mật khẩu")}"
                        aria-pressed="false">
                    ${icon("i-eye")}
                </button>
            </span>
        `;
    }

    function bindPasswordStatusToggles(root) {
        root.querySelectorAll("[data-toggle-password-status]")
            .forEach((button) => {
                if (button.dataset.bound === "true") {
                    return;
                }
                button.dataset.bound = "true";
                button.addEventListener("click", () => {
                    const control = button.closest(
                        "[data-password-status]"
                    );
                    const reveal = control.querySelector(
                        "[data-password-revealed]"
                    ).classList.contains("hidden");
                    control.querySelector("[data-password-masked]")
                        .classList.toggle("hidden", reveal);
                    control.querySelector("[data-password-revealed]")
                        .classList.toggle("hidden", !reveal);
                    button.setAttribute("aria-pressed", String(reveal));
                    button.setAttribute(
                        "aria-label",
                        t(reveal
                            ? "Ẩn trạng thái mật khẩu"
                            : "Hiển thị trạng thái mật khẩu")
                    );
                    button.innerHTML = icon(
                        reveal ? "i-eye-off" : "i-eye"
                    );
                });
            });
    }

    function confirmDeactivateMember(member) {
        openConfirmModal({
            title: "Vô hiệu hóa thành viên?",
            message: `${member.fullName || member.email} sẽ không thể đăng nhập.`,
            confirmText: "Vô hiệu hóa",
            danger: true,
            onConfirm: async (button) => {
                setButtonLoading(button, true, "Đang xử lý...");
                try {
                    await api(`/api/admin/members/${member.id}`, {
                        method: "DELETE"
                    });
                    closeModal();
                    toast("Đã vô hiệu hóa thành viên", member.email);
                    await loadMembers(0);
                } catch (error) {
                    toast("Không thể vô hiệu hóa", error.message, "error");
                    setButtonLoading(button, false);
                }
            }
        });
    }

    async function renderBorrowingsPage() {
        elements.pageContent.innerHTML = `
            <section class="page-section">
                <div class="section-heading">
                    <div>
                        <h2>${state.isAdmin
                ? "Hoạt động mượn trả"
                : "Sách của tôi"}</h2>
                        <p>${state.isAdmin
                ? "Theo dõi toàn bộ giao dịch trong thư viện."
                : "Theo dõi thời hạn và lịch sử mượn sách."}</p>
                    </div>
                    ${state.isAdmin ? "" : `
                        <button class="button button--primary"
                                data-go-books>
                            ${icon("i-book")} Tìm sách
                        </button>
                    `}
                </div>
                ${state.isAdmin ? "" : `
                    <section class="saved-books-section">
                        <div class="shelf-heading">
                            <div>
                                <span class="section-kicker">
                                    Bộ sưu tập cá nhân
                                </span>
                                <h3>Sách đã lưu</h3>
                                <p>Bộ sưu tập bạn muốn đọc sau</p>
                            </div>
                        </div>
                        <div id="saved-books-result">
                            ${pageSkeleton(2)}
                        </div>
                    </section>
                `}
                <div id="borrowings-result">${pageSkeleton(3)}</div>
            </section>
        `;
        document.querySelector("[data-go-books]")
            ?.addEventListener("click", () => navigate("books"));
        await Promise.all([
            loadBorrowings(0),
            state.isAdmin
                ? Promise.resolve()
                : loadSavedBooks()
        ]);
    }

    async function loadSavedBooks() {
        const host = document.querySelector(
            "#saved-books-result"
        );
        if (!host) {
            return;
        }

        try {
            const response = await api(
                "/api/saved-books?page=0&size=10"
            );
            if (!response.content.length) {
                host.innerHTML = `
                    <div class="saved-books-empty">
                        ${icon("i-bookmark")}
                        <span>Bạn chưa lưu cuốn sách nào.</span>
                    </div>
                `;
                return;
            }

            host.innerHTML = `
                <div class="saved-books-grid">
                    ${response.content.map((savedBook) => {
                const book = savedBook.book;
                state.books.set(
                    String(book.id),
                    book
                );
                return `
                            <button type="button"
                                    class="saved-book-card"
                                    data-book-detail="${book.id}">
                                ${bookCover(
                    book,
                    "book-cover-frame--saved"
                )}
                                <span>
                                    <strong>
                                        ${escapeHtml(book.title)}
                                    </strong>
                                    <small>
                                        ${escapeHtml(
                    authorNames(book)
                )}
                                    </small>
                                    <em>Mở chi tiết</em>
                                </span>
                            </button>
                        `;
            }).join("")}
                </div>
            `;
            bindBookDetailLinks(host);
        } catch (error) {
            host.innerHTML = inlineError(error);
        }
    }

    async function loadBorrowings(page) {
        const host = document.querySelector("#borrowings-result");
        host.innerHTML = pageSkeleton(3);
        try {
            const endpoint = state.isAdmin
                ? "/api/admin/borrowings"
                : "/api/borrowings/my";
            const response = await api(
                `${endpoint}?page=${page}&size=10`
            );
            host.innerHTML = `
                <div class="card">
                    ${borrowingTable(response.content, {
                canReturn: !state.isAdmin,
                compact: false
            })}
                    ${pagination(response, "borrowings")}
                </div>
            `;
            bindBorrowingActions();
        } catch (error) {
            host.innerHTML = inlineError(error);
        }
    }

    function borrowingTable(items, options) {
        if (!items.length) {
            return emptyState(
                "i-borrow",
                "Chưa có lượt mượn sách",
                state.isAdmin
                    ? "Các giao dịch mới sẽ xuất hiện tại đây."
                    : "Hãy khám phá thư viện và chọn một cuốn sách."
            );
        }

        return `
            <div class="table-wrap">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Sách</th>
                            ${state.isAdmin ? "<th>Người mượn</th>" : ""}
                            <th>Ngày mượn</th>
                            <th>Hạn trả</th>
                            <th>Ngày trả</th>
                            <th>Trạng thái</th>
                            ${options.canReturn
                ? '<th class="text-right">Thao tác</th>'
                : ""}
                        </tr>
                    </thead>
                    <tbody>
                        ${items.map((item) => `
                            <tr>
                                <td>
                                    <span class="table-primary">
                                        <strong>${escapeHtml(
                    item.bookTitle
                )}</strong>
                                        <small>${escapeHtml(item.isbn)}</small>
                                    </span>
                                </td>
                                ${state.isAdmin ? `
                                    <td>
                                        <span class="borrower-cell">
                                            <span class="avatar avatar--person avatar--small">
                                                ${icon("i-user")}
                                            </span>
                                            <span class="table-primary">
                                                <strong>${escapeHtml(
                    item.memberName
                    || item.fullName
                    || item.username
                    || item.memberEmail
                    || "—"
                )}</strong>
                                                <small>${escapeHtml(
                    item.membershipCode
                    || "—"
                )}</small>
                                                <small>${escapeHtml(
                    item.memberEmail
                    || item.email
                    || "—"
                )}</small>
                                                <small>${escapeHtml(
                    item.memberPhone
                    || item.phone
                    || "—"
                )}</small>
                                            </span>
                                        </span>
                                    </td>
                                ` : ""}
                                <td>${formatDateTime(item.borrowedAt)}</td>
                                <td>${formatDateTime(item.dueAt)}</td>
                                <td>
                                    ${item.returnedAt
                        ? formatDateTime(item.returnedAt)
                        : `<span class="muted">${t(
                            "Chưa trả"
                        )}</span>`}
                                </td>
                                <td>${borrowingStatus(item)}</td>
                                ${options.canReturn ? `
                                    <td>
                                        <span class="table-actions">
                                            ${item.status === "BORROWED"
                            ? `<button
                                                    class="button button--secondary button--small"
                                                    data-return-borrowing="${item.id}">
                                                    Trả sách
                                                   </button>`
                            : "—"}
                                        </span>
                                    </td>
                                ` : ""}
                            </tr>
                        `).join("")}
                    </tbody>
                </table>
            </div>
        `;
    }

    function bindBorrowingActions() {
        document.querySelectorAll("[data-borrowings-page]")
            .forEach((button) => {
                button.addEventListener("click", () => {
                    loadBorrowings(
                        Number(button.dataset.borrowingsPage)
                    );
                });
            });
        document.querySelectorAll("[data-return-borrowing]")
            .forEach((button) => {
                button.addEventListener("click", () => {
                    confirmReturnBook(
                        button.dataset.returnBorrowing
                    );
                });
            });
    }

    function confirmReturnBook(borrowingId) {
        openConfirmModal({
            title: "Xác nhận trả sách?",
            message: "Hệ thống sẽ ghi nhận thời gian trả sách hiện tại.",
            confirmText: "Xác nhận trả",
            onConfirm: async (button) => {
                setButtonLoading(button, true, "Đang xử lý...");
                try {
                    await api(
                        `/api/borrowings/${borrowingId}/return`,
                        { method: "POST" }
                    );
                    closeModal();
                    toast("Trả sách thành công", "Cảm ơn bạn!");
                    await loadBorrowings(0);
                } catch (error) {
                    toast("Không thể trả sách", error.message, "error");
                    setButtonLoading(button, false);
                }
            }
        });
    }

    async function renderSystemPage() {
        const config = await api("/api/admin/system-config");
        state.maintenance = config;
        updateMaintenancePill(config.maintenanceMode);

        elements.pageContent.innerHTML = `
            <section class="page-section">
                <div class="section-heading">
                    <div>
                        <h2>Trạng thái hệ thống</h2>
                        <p>Kiểm soát khả năng truy cập API khi bảo trì.</p>
                    </div>
                </div>

                <div class="maintenance-card">
                    <div class="maintenance-info">
                        <span class="stat-icon">${icon("i-settings")}</span>
                        <div>
                            <h3>Chế độ bảo trì</h3>
                            <p>
                                Khi bật, toàn bộ API nghiệp vụ sẽ tạm dừng.
                                API đăng nhập và cấu hình vẫn hoạt động.
                            </p>
                        </div>
                    </div>
                    <label class="switch" title="Bật/tắt bảo trì">
                        <input id="maintenance-switch" type="checkbox"
                               ${config.maintenanceMode ? "checked" : ""}>
                        <span class="switch-slider"></span>
                    </label>
                </div>

                <div class="maintenance-guide" aria-label="Cách thức hoạt động">
                    <div class="maintenance-guide__intro">
                        <span class="section-kicker">Cách thức hoạt động</span>
                        <h2>Chế độ bảo trì được sử dụng như thế nào?</h2>
                        <p>
                            Dùng chế độ này khi nâng cấp ứng dụng, sửa dữ liệu
                            hoặc thực hiện công việc cần tạm dừng giao dịch.
                        </p>
                    </div>
                    <div class="maintenance-guide__steps">
                        <article>
                            <strong>01</strong>
                            <h3>Khi bật chế độ bảo trì</h3>
                            <p>
                                Các API sách, thành viên và mượn trả sẽ trả về
                                HTTP 503 Service Unavailable cùng thông báo ở
                                bên dưới.
                            </p>
                        </article>
                        <article>
                            <strong>02</strong>
                            <h3>API vẫn được phép</h3>
                            <p>
                                Đăng nhập, kiểm tra trạng thái và API cấu hình
                                vẫn hoạt động để quản trị viên có thể quay lại
                                trang này và tắt bảo trì.
                            </p>
                        </article>
                        <article>
                            <strong>03</strong>
                            <h3>Cách sử dụng an toàn</h3>
                            <p>
                                Nhập thông báo cho người dùng, bật công tắc rồi
                                nhấn Lưu cấu hình. Khi hoàn tất bảo trì, tắt
                                công tắc và lưu lại.
                            </p>
                        </article>
                    </div>
                </div>

                <div class="card">
                    <div class="card-heading">
                        <div>
                            <h2>Thông báo bảo trì</h2>
                            <p>Thông điệp trả về khi hệ thống tạm dừng.</p>
                        </div>
                    </div>
                    <div class="card-body">
                        <form id="maintenance-form" class="form-stack">
                            <label class="field">
                                <span>Nội dung thông báo</span>
                                <textarea name="message" maxlength="500"
                                    placeholder="Hệ thống đang được bảo trì...">${escapeHtml(
            t(config.maintenanceMessage || "")
        )}</textarea>
                            </label>
                            <div class="action-row">
                                <button type="submit"
                                        class="button button--primary">
                                    Lưu cấu hình
                                </button>
                            </div>
                            <p id="maintenance-form-error"
                               class="form-error hidden"></p>
                        </form>
                    </div>
                </div>

                <div class="card">
                    <div class="card-body">
                        <div class="detail-list">
                            <div class="detail-row">
                                <span>Cập nhật bởi</span>
                                <strong>${escapeHtml(
            config.updatedBy || "Hệ thống"
        )}</strong>
                            </div>
                            <div class="detail-row">
                                <span>Cập nhật lúc</span>
                                <strong>${formatDateTime(
            config.updatedAt
        )}</strong>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        `;

        document.querySelector("#maintenance-form")
            .addEventListener("submit", async (event) => {
                event.preventDefault();
                const button = event.currentTarget
                    .querySelector("[type='submit']");
                const enabled = document.querySelector(
                    "#maintenance-switch"
                ).checked;
                const message = event.currentTarget.elements.message.value;
                setButtonLoading(button, true, "Đang lưu...");
                try {
                    const updated = await api(
                        "/api/admin/system-config/maintenance",
                        {
                            method: "PUT",
                            body: JSON.stringify({ enabled, message })
                        }
                    );
                    state.maintenance = updated;
                    updateMaintenancePill(updated.maintenanceMode);
                    toast(
                        "Đã lưu cấu hình",
                        updated.maintenanceMode
                            ? "Hệ thống đang ở chế độ bảo trì."
                            : "Hệ thống hoạt động bình thường."
                    );
                } catch (error) {
                    showFormError("#maintenance-form-error", error);
                } finally {
                    setButtonLoading(button, false);
                }
            });
    }

    async function renderAccountPage() {
        const profile = await api("/api/profile");
        state.profile = profile;
        const profileIncomplete = !state.isAdmin
            && (
                profile.profileComplete === false
                || !profile.phone
                || !profile.dateOfBirth
            );

        elements.pageContent.innerHTML = `
            <section class="page-section">
                <div class="section-heading">
                    <div>
                        <h2>Thông tin và bảo mật</h2>
                        <p>Quản lý hồ sơ, email và mật khẩu đăng nhập.</p>
                    </div>
                </div>

                ${profileIncomplete ? `
                    <div class="profile-completion-alert" role="alert">
                        <span>${icon("i-alert")}</span>
                        <div>
                            <strong>Thông tin cá nhân chưa đầy đủ</strong>
                            <p>
                                Vui lòng cập nhật số điện thoại và ngày sinh
                                để thư viện có thể liên hệ và hỗ trợ việc
                                mượn trả sách.
                            </p>
                        </div>
                        <button type="button" class="button button--secondary"
                                data-focus-profile>
                            Cập nhật ngay
                        </button>
                    </div>
                ` : ""}

                <div class="settings-grid">
                    <div class="card account-profile-card">
                        <div class="card-heading">
                            <div>
                                <h2>Thông tin tài khoản</h2>
                                <p>
                                    Cập nhật thông tin hiển thị và thông tin
                                    liên hệ của bạn.
                                </p>
                            </div>
                        </div>
                        <div class="card-body">
                            <div class="profile-summary">
                                <span class="avatar avatar--person avatar--large">
                                    ${icon("i-user")}
                                </span>
                                <div>
                                    <h3>${escapeHtml(
            profile.fullName
            || profile.username
            || profile.email
        )}</h3>
                                    <p>${state.isAdmin
                ? "Quản trị viên hệ thống"
                : "Thành viên thư viện"}</p>
                                </div>
                            </div>
                            <form id="profile-form" class="form-grid">
                                <label class="field">
                                    <span>Tên người dùng *</span>
                                    <input name="username" required
                                           minlength="3" maxlength="50"
                                           autocomplete="username"
                                           value="${attribute(
                    profile.username
                )}">
                                </label>
                                <label class="field">
                                    <span>Email</span>
                                    <input type="email" readonly
                                           value="${attribute(profile.email)}">
                                </label>
                                ${state.isAdmin ? "" : `
                                    <label class="field">
                                        <span>Họ và tên</span>
                                        <input name="fullName" maxlength="150"
                                               autocomplete="name"
                                               value="${attribute(
                    profile.fullName
                )}">
                                    </label>
                                    <label class="field">
                                        <span>Số điện thoại *</span>
                                        <input name="phone" maxlength="20"
                                               autocomplete="tel"
                                               required
                                               value="${attribute(
                    profile.phone
                )}">
                                    </label>
                                    <label class="field">
                                        <span>Ngày sinh *</span>
                                        <input name="dateOfBirth" type="date"
                                               max="${today()}" required
                                               value="${attribute(
                    profile.dateOfBirth
                )}">
                                    </label>
                                    <label class="field">
                                        <span>Mã thành viên</span>
                                        <input readonly value="${attribute(
                    profile.membershipCode || "—"
                )}">
                                    </label>
                                    <label class="field field--full">
                                        <span>Địa chỉ</span>
                                        <textarea name="address"
                                                  maxlength="500">${escapeHtml(
                    profile.address || ""
                )}</textarea>
                                    </label>
                                `}
                                <div class="action-row field--full">
                                    <button type="submit"
                                            class="button button--primary">
                                        Lưu hồ sơ
                                    </button>
                                    <span class="profile-role">
                                        <span>Quyền</span>
                                        <strong>${state.isAdmin
                ? "ADMIN" : "USER"}</strong>
                                    </span>
                                </div>
                                <p id="profile-form-error"
                                   class="form-error field--full hidden"></p>
                            </form>
                        </div>
                    </div>

                    <div class="card">
                        <div class="card-heading">
                            <div>
                                <h2>Đổi mật khẩu</h2>
                                <p>Bạn sẽ cần đăng nhập lại sau khi đổi.</p>
                            </div>
                        </div>
                        <div class="card-body">
                            <form id="change-password-form"
                                  class="form-stack">
                                <label class="field">
                                    <span>Mật khẩu hiện tại</span>
                                    <input name="currentPassword"
                                           type="password" required>
                                </label>
                                <label class="field">
                                    <span>Mật khẩu mới</span>
                                    <input name="newPassword"
                                           type="password" required>
                                </label>
                                <button type="submit"
                                        class="button button--primary">
                                    Đổi mật khẩu
                                </button>
                                <p id="password-form-error"
                                   class="form-error hidden"></p>
                            </form>
                        </div>
                    </div>

                    <div class="card">
                        <div class="card-heading">
                            <div>
                                <h2>Đổi địa chỉ email</h2>
                                <p>Mã xác minh sẽ được gửi tới email mới.</p>
                            </div>
                        </div>
                        <div class="card-body">
                            <form id="email-request-form"
                                  class="form-stack">
                                <label class="field">
                                    <span>Email mới</span>
                                    <input name="newEmail" type="email"
                                           required>
                                </label>
                                <button type="submit"
                                        class="button button--primary">
                                    Gửi mã xác minh
                                </button>
                                <p id="email-request-error"
                                   class="form-error hidden"></p>
                            </form>
                        </div>
                    </div>

                    <div id="email-verification-card"
                         class="card hidden">
                        <div class="card-heading">
                            <div>
                                <h2>Xác minh email mới</h2>
                                <p>Nhập mã gồm 6 chữ số trong email.</p>
                            </div>
                        </div>
                        <div class="card-body">
                            <form id="email-verify-form"
                                  class="form-stack">
                                <label class="field">
                                    <span>Mã xác minh</span>
                                    <input name="code" inputmode="numeric"
                                           pattern="[0-9]{6}" maxlength="6"
                                           required placeholder="000000">
                                </label>
                                <button type="submit"
                                        class="button button--primary">
                                    Xác nhận đổi email
                                </button>
                                <p id="email-verify-error"
                                   class="form-error hidden"></p>
                            </form>
                        </div>
                    </div>
                </div>
            </section>
        `;

        bindAccountForms();
    }

    function bindAccountForms() {
        document.querySelector("[data-focus-profile]")
            ?.addEventListener("click", () => {
                document.querySelector("#profile-form")
                    ?.scrollIntoView({ behavior: "smooth", block: "center" });
                document.querySelector(
                    "#profile-form [name='phone']"
                )?.focus({ preventScroll: true });
            });

        document.querySelector("#profile-form")
            .addEventListener("submit", async (event) => {
                event.preventDefault();
                const form = event.currentTarget;
                const button = form.querySelector("[type='submit']");
                const raw = formDataToObject(form);
                const payload = {
                    username: raw.username.trim(),
                    fullName: emptyToNull(raw.fullName),
                    dateOfBirth: emptyToNull(raw.dateOfBirth),
                    phone: emptyToNull(raw.phone),
                    address: emptyToNull(raw.address)
                };
                setButtonLoading(button, true, t("Đang cập nhật..."));
                try {
                    const updated = await api("/api/profile", {
                        method: "PUT",
                        body: JSON.stringify(payload)
                    });
                    state.profile = updated;
                    state.user = {
                        ...state.user,
                        username: updated.username,
                        email: updated.email
                    };
                    hydrateUserIdentity();
                    toast(
                        t("Đã cập nhật hồ sơ"),
                        t("Thông tin cá nhân đã được lưu.")
                    );
                    await renderAccountPage();
                } catch (error) {
                    showFormError("#profile-form-error", error);
                    setButtonLoading(button, false);
                }
            });

        document.querySelector("#change-password-form")
            .addEventListener("submit", async (event) => {
                event.preventDefault();
                const button = event.currentTarget
                    .querySelector("[type='submit']");
                setButtonLoading(button, true, "Đang cập nhật...");
                try {
                    await api("/api/auth/change-password", {
                        method: "POST",
                        body: JSON.stringify(
                            formDataToObject(event.currentTarget)
                        )
                    });
                    toast(
                        "Đổi mật khẩu thành công",
                        "Vui lòng đăng nhập lại."
                    );
                    setTimeout(forceLogout, 700);
                } catch (error) {
                    showFormError("#password-form-error", error);
                    setButtonLoading(button, false);
                }
            });

        document.querySelector("#email-request-form")
            .addEventListener("submit", async (event) => {
                event.preventDefault();
                const form = event.currentTarget;
                const button = form
                    .querySelector("[type='submit']");
                setButtonLoading(button, true, "Đang gửi...");
                try {
                    const result = await api(
                        "/api/auth/change-email/request",
                        {
                            method: "POST",
                            body: JSON.stringify(
                                formDataToObject(form)
                            )
                        }
                    );
                    toast("Đã gửi mã xác minh", result.message);
                    form.reset();
                    const verificationCard = document.querySelector(
                        "#email-verification-card"
                    );
                    if (verificationCard) {
                        verificationCard.classList.remove("hidden");
                        verificationCard.scrollIntoView({
                            behavior: "smooth",
                            block: "center"
                        });
                        verificationCard.querySelector("[name='code']")
                            ?.focus({ preventScroll: true });
                    }
                } catch (error) {
                    showFormError("#email-request-error", error);
                } finally {
                    setButtonLoading(button, false);
                }
            });

        document.querySelector("#email-verify-form")
            .addEventListener("submit", async (event) => {
                event.preventDefault();
                const button = event.currentTarget
                    .querySelector("[type='submit']");
                setButtonLoading(button, true, "Đang xác minh...");
                try {
                    await api("/api/auth/change-email/verify", {
                        method: "POST",
                        body: JSON.stringify(
                            formDataToObject(event.currentTarget)
                        )
                    });
                    toast(
                        "Đổi email thành công",
                        "Vui lòng đăng nhập lại bằng email mới."
                    );
                    setTimeout(forceLogout, 700);
                } catch (error) {
                    showFormError("#email-verify-error", error);
                    setButtonLoading(button, false);
                }
            });
    }

    function openPasswordRecoveryModal(resetToken = "") {
        const token = typeof resetToken === "string"
            ? resetToken.trim()
            : "";
        const isLocalEnvironment = [
            "localhost",
            "127.0.0.1"
        ].includes(window.location.hostname);

        openModal({
            title: token
                ? "Đặt mật khẩu mới"
                : "Quên mật khẩu",
            subtitle: token
                ? "Liên kết đã được xác nhận. Hãy tạo mật khẩu mới."
                : "Nhập email đăng ký để nhận liên kết đặt lại mật khẩu.",
            body: token
                ? `
                    <form id="reset-form"
                          class="form-stack recovery-form">
                        <input name="token" type="hidden"
                               value="${attribute(token)}">
                        <label class="field">
                            <span>Mật khẩu mới</span>
                            <input name="newPassword" type="password"
                                   autocomplete="new-password"
                                   required placeholder="Nhập mật khẩu mới">
                            <small>
                                Gồm chữ hoa, chữ thường, số và ký tự đặc biệt.
                            </small>
                        </label>
                        <label class="field">
                            <span>Nhập lại mật khẩu mới</span>
                            <input name="confirmPassword" type="password"
                                   autocomplete="new-password"
                                   required placeholder="Nhập lại mật khẩu">
                        </label>
                        <button type="submit"
                                class="button button--primary button--full">
                            Cập nhật mật khẩu
                        </button>
                        <p id="reset-form-error"
                           class="form-error hidden"></p>
                    </form>
                `
                : `
                    <div class="recovery-steps">
                        <div><span>1</span> Nhập email đăng ký</div>
                        <div><span>2</span> Mở liên kết trong email</div>
                        <div><span>3</span> Tạo mật khẩu mới</div>
                    </div>
                    <form id="forgot-form" class="form-stack">
                        <label class="field">
                            <span>Email đăng ký</span>
                            <input name="email" type="email"
                                   autocomplete="email" required
                                   placeholder="email@example.com">
                        </label>
                        <button type="submit"
                                class="button button--primary button--full">
                            Gửi liên kết đặt lại mật khẩu
                        </button>
                        <p id="forgot-form-error"
                           class="form-error hidden"></p>
                    </form>
                    <div id="forgot-success"
                         class="recovery-success hidden">
                        <strong>Hãy kiểm tra hộp thư của bạn</strong>
                        <p>
                            Nếu email thuộc một tài khoản hợp lệ,
                            hệ thống đã gửi liên kết có hiệu lực trong 30 phút.
                            Kiểm tra cả thư mục spam hoặc thư rác.
                        </p>
                        ${isLocalEnvironment
                    ? `<a href="http://localhost:8025"
                                  target="_blank" rel="noopener">
                                   Mở hộp thư thử nghiệm Mailpit
                               </a>`
                    : ""}
                    </div>
                `,
            showFooter: false
        });

        document.querySelector("#forgot-form")
            ?.addEventListener("submit", async (event) => {
                event.preventDefault();
                const button = event.currentTarget
                    .querySelector("[type='submit']");
                setButtonLoading(button, true, "Đang gửi...");
                try {
                    await api(
                        "/api/auth/forgot-password",
                        {
                            method: "POST",
                            body: JSON.stringify(
                                formDataToObject(event.currentTarget)
                            )
                        },
                        false
                    );
                    event.currentTarget.classList.add("hidden");
                    document.querySelector("#forgot-success")
                        .classList.remove("hidden");
                    toast(
                        "Đã gửi hướng dẫn",
                        "Hãy kiểm tra email để tiếp tục."
                    );
                } catch (error) {
                    showFormError("#forgot-form-error", error);
                } finally {
                    setButtonLoading(button, false);
                }
            });

        document.querySelector("#reset-form")
            ?.addEventListener("submit", async (event) => {
                event.preventDefault();
                const button = event.currentTarget
                    .querySelector("[type='submit']");
                const data = formDataToObject(event.currentTarget);
                if (data.newPassword !== data.confirmPassword) {
                    showFormError(
                        "#reset-form-error",
                        { message: "Mật khẩu nhập lại không khớp." }
                    );
                    return;
                }
                delete data.confirmPassword;
                setButtonLoading(button, true, "Đang cập nhật...");
                try {
                    await api(
                        "/api/auth/reset-password",
                        {
                            method: "POST",
                            body: JSON.stringify(data)
                        },
                        false
                    );
                    closeModal();
                    setAuthMessage(
                        "Đặt lại mật khẩu thành công. "
                        + "Bạn có thể đăng nhập bằng mật khẩu mới.",
                        "success"
                    );
                } catch (error) {
                    showFormError("#reset-form-error", error);
                    setButtonLoading(button, false);
                }
            });
    }

    async function ensureCategories() {
        if (state.categories.length) {
            return;
        }
        state.categories = await api("/api/books/lookups/categories");
    }

    async function api(path, options = {}, allowRefresh = true) {
        const requestOptions = { ...options };
        const bodyIsFormData = requestOptions.body instanceof FormData;
        const headers = new Headers(requestOptions.headers || {});
        const accessToken = sessionStorage.getItem(TOKEN_KEY);

        if (!bodyIsFormData && requestOptions.body
            && !headers.has("Content-Type")) {
            headers.set("Content-Type", "application/json");
        }
        if (accessToken) {
            headers.set("Authorization", `Bearer ${accessToken}`);
        }
        requestOptions.headers = headers;

        let response;
        try {
            response = await fetch(path, requestOptions);
        } catch (error) {
            const networkError = new Error(
                "Không thể kết nối tới hệ thống. Hãy kiểm tra server."
            );
            networkError.status = 0;
            throw networkError;
        }

        if (response.status === 401 && allowRefresh
            && sessionStorage.getItem(REFRESH_KEY)
            && !path.includes("/api/auth/refresh")) {
            const refreshed = await refreshAccessToken();
            if (refreshed) {
                return api(path, options, false);
            }
        }

        if (response.status === 204) {
            return null;
        }

        const contentType = response.headers.get("content-type") || "";
        let payload = null;
        if (contentType.includes("application/json")) {
            payload = await response.json();
        } else {
            const text = await response.text();
            payload = text ? { message: text } : null;
        }

        if (!response.ok) {
            const error = new Error(
                t(
                    payload?.message
                    || `Yêu cầu thất bại (${response.status})`
                )
            );
            error.status = response.status;
            error.fieldErrors = Object.fromEntries(
                Object.entries(payload?.fieldErrors || {})
                    .map(([field, message]) => [
                        field,
                        t(message)
                    ])
            );
            error.payload = payload;
            throw error;
        }

        return payload;
    }

    async function safeApi(path) {
        try {
            return await api(path);
        } catch (error) {
            return null;
        }
    }

    async function refreshAccessToken() {
        const refreshToken = sessionStorage.getItem(REFRESH_KEY);
        if (!refreshToken) {
            return false;
        }
        try {
            const response = await fetch("/api/auth/refresh", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ refreshToken })
            });
            if (!response.ok) {
                throw new Error("Refresh token is invalid");
            }
            storeTokens(await response.json());
            return true;
        } catch (error) {
            forceLogout();
            return false;
        }
    }

    async function logout() {
        const refreshToken = sessionStorage.getItem(REFRESH_KEY);
        try {
            if (refreshToken) {
                await api("/api/auth/logout", {
                    method: "POST",
                    body: JSON.stringify({ refreshToken })
                }, false);
            }
        } catch (error) {
            // Local logout must still complete if the token already expired.
        } finally {
            forceLogout();
        }
    }

    function forceLogout() {
        clearTokens();
        state.user = null;
        state.profile = null;
        state.isAdmin = false;
        state.books.clear();
        state.members.clear();
        closeModal();
        showAuth();
    }

    function storeTokens(tokens) {
        sessionStorage.setItem(TOKEN_KEY, tokens.accessToken);
        sessionStorage.setItem(REFRESH_KEY, tokens.refreshToken);
    }

    function clearTokens() {
        sessionStorage.removeItem(TOKEN_KEY);
        sessionStorage.removeItem(REFRESH_KEY);
    }

    function openModal({
        title,
        subtitle = "",
        body,
        confirmText = "Xác nhận",
        onConfirm = null,
        showFooter = true,
        large = false,
        danger = false
    }) {
        elements.modalRoot.innerHTML = `
            <div class="modal-backdrop" role="presentation">
                <section class="modal ${large ? "modal--large" : ""}"
                         role="dialog" aria-modal="true"
                         aria-labelledby="modal-title">
                    <header class="modal-header">
                        <div>
                            <h2 id="modal-title">${escapeHtml(t(title))}</h2>
                            ${subtitle
                ? `<p>${escapeHtml(t(subtitle))}</p>`
                : ""}
                        </div>
                        <button class="icon-button" data-close-modal
                                aria-label="Đóng">
                            ${icon("i-close")}
                        </button>
                    </header>
                    <div class="modal-body">${body}</div>
                    ${showFooter ? `
                        <footer class="modal-footer">
                            <button class="button button--secondary"
                                    data-close-modal>
                                Hủy
                            </button>
                            ${onConfirm ? `
                                <button id="modal-confirm-button"
                                    class="button ${danger
                        ? "button--danger"
                        : "button--primary"}">
                                    ${escapeHtml(t(confirmText))}
                                </button>
                            ` : ""}
                        </footer>
                    ` : ""}
                </section>
            </div>
        `;

        elements.modalRoot.querySelectorAll("[data-close-modal]")
            .forEach((button) => {
                button.addEventListener("click", closeModal);
            });
        elements.modalRoot.querySelector(".modal-backdrop")
            .addEventListener("mousedown", (event) => {
                if (event.target === event.currentTarget) {
                    closeModal();
                }
            });
        if (onConfirm) {
            document.querySelector("#modal-confirm-button")
                .addEventListener("click", (event) => {
                    onConfirm(event.currentTarget);
                });
        }

        setTimeout(() => {
            elements.modalRoot.querySelector(
                "input:not([type='hidden']), select, textarea"
            )?.focus();
        });
    }

    function openConfirmModal({
        title,
        message,
        confirmText,
        onConfirm,
        danger = false
    }) {
        openModal({
            title,
            subtitle: message,
            body: `
                <div class="confirm-note">
                    <strong>Kiểm tra lại thông tin trước khi xác nhận.</strong>
                    <p>Thao tác sẽ được ghi nhận ngay trên hệ thống.</p>
                </div>
            `,
            confirmText,
            onConfirm,
            danger
        });
    }

    function closeModal() {
        elements.modalRoot.innerHTML = "";
    }

    function toast(title, message, type = "success") {
        const element = document.createElement("div");
        element.className = `toast ${type === "error"
            ? "toast--error" : ""}`;
        element.innerHTML = `
            <span class="toast-content">
                <strong>${escapeHtml(t(title))}</strong>
                <span>${escapeHtml(t(message || ""))}</span>
            </span>
        `;
        elements.toastRoot.appendChild(element);
        setTimeout(() => element.remove(), 4200);
    }

    function renderPageError(error) {
        elements.pageContent.innerHTML = `
            <section class="page-section">
                <div class="card">
                    ${emptyState(
            "i-alert",
            error.status === 503
                ? "Hệ thống đang bảo trì"
                : "Không thể tải dữ liệu",
            error.message
        )}
                </div>
                ${state.isAdmin && error.status === 503
                ? `<button class="button button--primary"
                              data-open-system>
                           Mở cấu hình hệ thống
                       </button>`
                : ""}
            </section>
        `;
        document.querySelector("[data-open-system]")
            ?.addEventListener("click", () => navigate("system"));
    }

    function inlineError(error) {
        return `
            <div class="card">
                ${emptyState(
            "i-alert",
            "Không thể tải dữ liệu",
            error.message
        )}
            </div>
        `;
    }

    function emptyState(iconName, title, message) {
        return `
            <div class="empty-state">
                <strong>${escapeHtml(t(title))}</strong>
                <p>${escapeHtml(t(message))}</p>
            </div>
        `;
    }

    function pageSkeleton(count = 2) {
        return `
            <div style="display:grid;gap:14px">
                ${Array.from({ length: count }, () =>
            '<div class="skeleton"></div>').join("")}
            </div>
        `;
    }

    function pagination(response, namespace) {
        if (response.totalPages <= 1) {
            return `
                <div class="pagination">
                    <span class="pagination-info">
                        ${response.totalElements} kết quả
                    </span>
                </div>
            `;
        }
        return `
            <div class="pagination">
                <span class="pagination-info">
                    Trang ${response.page + 1}/${response.totalPages}
                    · ${response.totalElements} kết quả
                </span>
                <span class="pagination-actions">
                    <button class="icon-button"
                            data-${namespace}-page="${response.page - 1}"
                            ${response.first ? "disabled" : ""}
                            aria-label="Trang trước">
                        ${icon("i-chevron-left")}
                    </button>
                    ${paginationNumbers(
            response.page,
            response.totalPages,
            namespace
        )}
                    <button class="icon-button"
                            data-${namespace}-page="${response.page + 1}"
                            ${response.last ? "disabled" : ""}
                            aria-label="Trang sau">
                        ${icon("i-chevron-right")}
                    </button>
                </span>
            </div>
        `;
    }

    function paginationNumbers(
        currentPage,
        totalPages,
        namespace
    ) {
        const visiblePages = new Set([
            0,
            totalPages - 1,
            currentPage - 1,
            currentPage,
            currentPage + 1
        ]);

        if (totalPages <= 7) {
            for (let page = 0; page < totalPages; page++) {
                visiblePages.add(page);
            }
        } else if (currentPage <= 3) {
            [0, 1, 2, 3, 4].forEach(
                (page) => visiblePages.add(page)
            );
        } else if (currentPage >= totalPages - 4) {
            for (
                let page = totalPages - 5;
                page < totalPages;
                page++
            ) {
                visiblePages.add(page);
            }
        }

        const pages = [...visiblePages]
            .filter((page) =>
                page >= 0 && page < totalPages
            )
            .sort((left, right) => left - right);

        const items = [];
        pages.forEach((page, index) => {
            if (index > 0 && page - pages[index - 1] > 1) {
                items.push(
                    '<span class="page-ellipsis">…</span>'
                );
            }
            items.push(`
                <button type="button"
                        class="page-number-button
                            ${page === currentPage ? "active" : ""}"
                        data-${namespace}-page="${page}"
                        aria-label="Trang ${page + 1}"
                        ${page === currentPage
                    ? 'aria-current="page"'
                    : ""}>
                    ${page + 1}
                </button>
            `);
        });
        return items.join("");
    }

    function availabilityBadge(book) {
        if (book.availableQuantity > 0 && book.active) {
            return badge(`${book.availableQuantity} cuốn có sẵn`, "success");
        }
        return badge("Tạm hết sách", "warning");
    }

    function memberStatus(member) {
        if (!member.accountNonLocked) {
            return badge("Đang khóa", "danger");
        }
        if (!member.enabled) {
            return badge("Vô hiệu hóa", "neutral");
        }
        if (!member.emailVerified) {
            return badge("Chưa xác minh", "warning");
        }
        return badge("Hoạt động", "success");
    }

    function borrowingStatus(item) {
        if (item.status === "RETURNED") {
            return badge("Đã trả", "neutral");
        }
        if (item.overdue) {
            return badge("Quá hạn", "danger");
        }
        return badge("Đang mượn", "info");
    }

    function badge(text, variant) {
        return `
            <span class="badge badge--${variant}">
                <span class="status-dot"></span>
                ${escapeHtml(t(text))}
            </span>
        `;
    }

    function authorNames(book) {
        const names = (book.authors || []).map((author) => author.name);
        return names.length
            ? names.join(", ")
            : t("Chưa cập nhật tác giả");
    }

    function setAuthMessage(message, type) {
        elements.authMessage.textContent = t(message);
        elements.authMessage.classList.remove("hidden");
        elements.authMessage.style.color =
            type === "error" ? "var(--danger)" : "var(--success)";
        elements.authMessage.style.background =
            type === "error" ? "var(--danger-soft)" : "var(--success-soft)";
        elements.authMessage.style.borderColor =
            type === "error" ? "#ffd5d9" : "#c9f0df";
    }

    function showFormError(selector, error) {
        const element = document.querySelector(selector);
        element.textContent = formatApiError(error);
        element.classList.remove("hidden");
    }

    function formatApiError(error) {
        const fieldMessages = Object.values(
            error.fieldErrors || {}
        ).map(t);
        if (fieldMessages.length) {
            return `${t(error.message)}: ${fieldMessages.join("; ")}`;
        }
        return t(error.message);
    }

    function formDataToObject(form) {
        return Object.fromEntries(new FormData(form).entries());
    }

    function queryFromForm(form) {
        const parameters = new URLSearchParams();
        new FormData(form).forEach((value, key) => {
            if (String(value).trim() !== "") {
                parameters.set(key, String(value).trim());
            }
        });
        return parameters;
    }

    function setButtonLoading(button, loading, label = "") {
        if (!button) {
            return;
        }
        if (loading) {
            button.dataset.originalLabel = button.innerHTML;
            button.disabled = true;
            button.textContent = t(label);
        } else {
            button.disabled = false;
            if (button.dataset.originalLabel) {
                button.innerHTML = button.dataset.originalLabel;
                delete button.dataset.originalLabel;
            }
        }
    }

    function updateMaintenancePill(enabled) {
        document.querySelector("#maintenance-pill")
            .classList.toggle("hidden", !enabled);
    }

    function openSidebar() {
        elements.sidebar.classList.add("open");
        elements.sidebarOverlay.classList.add("open");
    }

    function closeSidebar() {
        elements.sidebar.classList.remove("open");
        elements.sidebarOverlay.classList.remove("open");
    }

    function displayName() {
        return state.profile?.fullName
            || state.profile?.username
            || state.user?.username
            || state.user?.email
            || (state.locale === "en" ? "You" : "Bạn");
    }

    function icon(name) {
        return `<svg aria-hidden="true"><use href="#${name}"></use></svg>`;
    }
    // hàm xử lý hiển thị ảnh bìa sách, nếu không có ảnh bìa thì hiển thị ảnh mặc định
    function bookCover(book, modifier = "") {
        const fileKey = book.isbn
            ? String(book.isbn)
                .trim()
                .replace(/[^a-zA-Z0-9]/g, "")
            : String(book.id || "unknown")
                .trim()
                .replace(/[^a-zA-Z0-9_-]/g, "");
        // mã hóa fileKey để sử dụng trong URL
        const encodedKey = encodeURIComponent(fileKey);
        // danh sách các định dạng ảnh bìa sách được hỗ trợ
        const extensions = ["jpg", "jpeg", "png", "webp"];
        const candidates = extensions.flatMap((extension) => [
            `/assets/images/books/${encodedKey}.${extension}`
            + `?v=${COVER_ASSET_VERSION}`,
            `/assets/images/${encodedKey}.${extension}`
            + `?v=${COVER_ASSET_VERSION}`
        ]);
        return `
            <span class="book-cover-frame ${modifier}"
                  data-cover-frame
                  aria-label="Vị trí ảnh bìa sách">
                <img src="${candidates[0]}"
                      alt="Bìa sách ${attribute(book.title)}"
                      loading="lazy"
                      data-cover-index="0"
                      data-cover-candidates="${attribute(candidates.join("|"))}"
                      data-book-cover>
                <span class="book-cover-empty" aria-hidden="true"></span>
            </span>
        `;
    }

    function formatLongDate(value) {
        const formatted = new Intl.DateTimeFormat(
            state.locale === "en" ? "en-US" : "vi-VN", {
            weekday: "long",
            day: "2-digit",
            month: "long",
            year: "numeric"
        }).format(value);
        return formatted.charAt(0).toUpperCase() + formatted.slice(1);
    }

    function formatDate(value) {
        if (!value) {
            return "—";
        }
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return value;
        }
        return new Intl.DateTimeFormat(
            state.locale === "en" ? "en-US" : "vi-VN", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric"
        }).format(date);
    }

    function formatDateTime(value) {
        if (!value) {
            return "—";
        }
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return value;
        }
        return new Intl.DateTimeFormat(
            state.locale === "en" ? "en-US" : "vi-VN", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
            second: "2-digit",
            hour12: false
        }).format(date);
    }

    function today() {
        return new Date().toISOString().slice(0, 10);
    }

    function emptyToNull(value) {
        const normalized = String(value || "").trim();
        return normalized || null;
    }

    function attribute(value) {
        return escapeHtml(value == null ? "" : String(value));
    }

    function escapeHtml(value) {
        return String(value == null ? "" : value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }
})();
