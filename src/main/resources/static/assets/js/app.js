(() => {
    "use strict";

    const TOKEN_KEY = "sparkLibrary.accessToken";
    const REFRESH_KEY = "sparkLibrary.refreshToken";

    const state = {
        user: null,
        isAdmin: false,
        currentView: "dashboard",
        categories: [],
        books: new Map(),
        members: new Map(),
        maintenance: null
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
        dashboard: ["Tổng quan", "Dashboard"],
        books: ["Kho tài nguyên", "Sách"],
        members: ["Quản trị", "Thành viên"],
        borrowings: ["Hoạt động", "Mượn & trả sách"],
        system: ["Quản trị", "Cấu hình hệ thống"],
        account: ["Cá nhân", "Tài khoản của tôi"]
    };

    document.addEventListener("DOMContentLoaded", bootstrap);

    async function bootstrap() {
        bindGlobalEvents();

        const parameters = new URLSearchParams(window.location.search);
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
                enterApplication();
                return;
            } catch (error) {
                clearTokens();
            }
        }

        showAuth();
    }

    function bindGlobalEvents() {
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
            .addEventListener("click", openPasswordRecoveryModal);
        document.querySelector("#logout-button")
            .addEventListener("click", logout);
        document.querySelector("#topbar-profile")
            .addEventListener("click", () => navigate("account"));

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
                    input.focus({preventScroll: true});
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
            enterApplication();
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

    function enterApplication() {
        state.isAdmin = state.user.roles.includes("ROLE_ADMIN");
        elements.loading.classList.add("hidden");
        elements.authView.classList.add("hidden");
        elements.appView.classList.remove("hidden");
        hydrateUserIdentity();
        renderNavigation();
        navigate("dashboard");
    }

    function hydrateUserIdentity() {
        const name = state.user.username || state.user.email;
        const initial = name.slice(0, 1).toUpperCase();
        const role = state.isAdmin ? "Quản trị viên" : "Thành viên";

        document.querySelector("#sidebar-user-name").textContent = name;
        document.querySelector("#topbar-user-name").textContent = name;
        document.querySelector("#sidebar-user-role").textContent = role;
        document.querySelector("#sidebar-avatar").textContent = initial;
        document.querySelector("#topbar-avatar").textContent = initial;
    }

    function renderNavigation() {
        const generalItems = [
            navItem("dashboard", "Tổng quan"),
            navItem("books",
                state.isAdmin ? "Quản lý sách" : "Khám phá sách"),
            navItem("borrowings",
                state.isAdmin ? "Quản lý mượn trả" : "Sách đang mượn")
        ];

        const adminItems = state.isAdmin
            ? [
                `<p class="nav-section-label">Quản trị</p>`,
                navItem("members", "Thành viên"),
                navItem("system", "Cấu hình hệ thống")
            ]
            : [];

        elements.sidebarNav.innerHTML = `
            <p class="nav-section-label">Thư viện</p>
            ${generalItems.join("")}
            ${adminItems.join("")}
            <p class="nav-section-label">Cá nhân</p>
            ${navItem("account", "Tài khoản")}
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
        if (!state.isAdmin && ["members", "system"].includes(view)) {
            view = "dashboard";
        }

        state.currentView = view;
        const [eyebrow, title] = viewMeta[view];
        elements.pageEyebrow.textContent = eyebrow;
        elements.pageTitle.textContent = title;
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
            members: renderMembersPage,
            borrowings: renderBorrowingsPage,
            system: renderSystemPage,
            account: renderAccountPage
        };

        try {
            await renderers[view]();
        } catch (error) {
            renderPageError(error);
        }
    }

    async function renderDashboard() {
        const requests = [
            safeApi("/api/books?page=0&size=1&active=true"),
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
            <section class="page-section">
                <div class="welcome-banner">
                    <div>
                        <span class="eyebrow">
                            ${state.isAdmin ? "Bàn điều hành" : "Tài khoản thư viện"}
                        </span>
                        <h2>${state.isAdmin
                            ? "Tổng quan vận hành hôm nay"
                            : `Chào ${escapeHtml(displayName())}`}</h2>
                        <p>
                            ${state.isAdmin
                                ? "Theo dõi kho sách, thành viên và các giao dịch cần chú ý."
                                : "Tra cứu danh mục và theo dõi thời hạn trả sách của bạn."}
                        </p>
                    </div>
                    <time>${formatLongDate(new Date())}</time>
                </div>

                <div class="stat-grid">
                    ${stats.map(statCard).join("")}
                </div>

                <div class="dashboard-grid">
                    <div class="card">
                        <div class="card-heading">
                            <div>
                                <h2>Hoạt động gần đây</h2>
                                <p>Các lượt mượn và trả sách mới nhất.</p>
                            </div>
                            <button class="text-button"
                                    data-go-view="borrowings">
                                Xem tất cả
                            </button>
                        </div>
                        ${borrowingTable(
                            borrowings?.content || [],
                            { compact: true, canReturn: false }
                        )}
                    </div>

                    <div class="card">
                        <div class="card-heading">
                            <div>
                                <h2>Truy cập nhanh</h2>
                                <p>Thao tác thường dùng.</p>
                            </div>
                        </div>
                        <div class="card-body">
                            <div class="activity-list">
                                ${quickAction(
                                    state.isAdmin
                                        ? "Thêm sách mới"
                                        : "Tìm một cuốn sách",
                                    state.isAdmin
                                        ? "Cập nhật kho tài nguyên"
                                        : "Khám phá thư viện",
                                    "books"
                                )}
                                ${quickAction(
                                    state.isAdmin
                                        ? "Theo dõi mượn trả"
                                        : "Xem sách đang mượn",
                                    "Kiểm tra trạng thái hiện tại",
                                    "borrowings"
                                )}
                                ${state.isAdmin
                                    ? quickAction(
                                        "Tạo thành viên",
                                        "Thêm tài khoản thư viện",
                                        "members"
                                    )
                                    : quickAction(
                                        "Bảo mật tài khoản",
                                        "Đổi mật khẩu hoặc email",
                                        "account"
                                    )}
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        `;

        elements.pageContent.querySelectorAll("[data-go-view]")
            .forEach((button) => {
                button.addEventListener("click", () => {
                    navigate(button.dataset.goView);
                });
            });
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
                            ? "Quản lý kho sách"
                            : "Khám phá thư viện"}</h2>
                        <p>${state.isAdmin
                            ? "Tìm kiếm, cập nhật và nhập dữ liệu sách."
                            : "Tìm sách theo tên, tác giả hoặc danh mục."}</p>
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

                <form id="book-filter-form" class="filter-panel">
                    <label class="field">
                        <span>Từ khóa</span>
                        <span class="search-input-wrap">
                            ${icon("i-search")}
                            <input name="keyword"
                                   placeholder="Tên, ISBN, tác giả...">
                        </span>
                    </label>
                    <label class="field">
                        <span>Danh mục</span>
                        <select name="categoryId">
                            <option value="">Tất cả danh mục</option>
                            ${state.categories.map((category) => `
                                <option value="${category.id}">
                                    ${escapeHtml(category.name)}
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
                        <span>Trạng thái</span>
                        <select name="availableOnly">
                            <option value="">Tất cả</option>
                            <option value="true">Còn sách</option>
                            <option value="false">Hết sách</option>
                        </select>
                    </label>
                    <button type="submit" class="button button--primary">
                        Tìm kiếm
                    </button>
                </form>

                <div id="books-result">${pageSkeleton(3)}</div>
            </section>
        `;

        document.querySelector("#book-filter-form")
            .addEventListener("submit", (event) => {
                event.preventDefault();
                loadBooks(0);
            });

        document.querySelector("#create-book-button")
            ?.addEventListener("click", () => openBookModal());
        document.querySelector("#import-books-button")
            ?.addEventListener("click", openImportModal);

        await loadBooks(0);
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
            parameters.set("size", 9);
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
            <div class="book-grid">
                ${response.content.map((book) => `
                    <article class="book-card">
                        <div class="book-card-top">
                            <span class="book-cover">
                                ${escapeHtml(bookInitials(book.title))}
                            </span>
                            ${availabilityBadge(book)}
                        </div>
                        <h3 title="${escapeHtml(book.title)}">
                            ${escapeHtml(book.title)}
                        </h3>
                        <p class="book-authors">
                            ${escapeHtml(authorNames(book))}
                        </p>
                        <div class="book-meta">
                            <div>
                                <small>Danh mục</small>
                                <strong>${escapeHtml(
                                    book.category?.name || "Khác"
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
                                        <span class="table-primary">
                                            <strong>${escapeHtml(book.title)}</strong>
                                            <small>${escapeHtml(book.isbn)}
                                                · ${escapeHtml(authorNames(book))}
                                            </small>
                                        </span>
                                    </td>
                                    <td>${escapeHtml(
                                        book.category?.name || "—"
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
    }

    async function borrowBook(bookId, button) {
        setButtonLoading(button, true, "Đang mượn...");
        try {
            const result = await api("/api/borrowings", {
                method: "POST",
                body: JSON.stringify({bookId: Number(bookId)})
            });
            toast(
                "Mượn sách thành công",
                `Hạn trả: ${formatDate(result.dueAt)}`
            );
            await loadBooks(0);
        } catch (error) {
            toast("Không thể mượn sách", error.message, "error");
            setButtonLoading(button, false);
        }
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
                                    ${escapeHtml(category.name)}
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
                                <th>Trạng thái</th>
                                <th class="text-right">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${response.content.map((member) => `
                                <tr>
                                    <td>
                                        <span class="table-primary">
                                            <strong>${escapeHtml(
                                                member.fullName || member.username
                                            )}</strong>
                                            <small>${escapeHtml(member.email)}</small>
                                        </span>
                                    </td>
                                    <td>${escapeHtml(
                                        member.membershipCode || "—"
                                    )}</td>
                                    <td>${formatDate(member.dateOfBirth)}</td>
                                    <td>${escapeHtml(member.phone || "—")}</td>
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
                <div id="borrowings-result">${pageSkeleton(3)}</div>
            </section>
        `;
        document.querySelector("[data-go-books]")
            ?.addEventListener("click", () => navigate("books"));
        await loadBorrowings(0);
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
                            ${state.isAdmin ? "<th>Thành viên</th>" : ""}
                            <th>Ngày mượn</th>
                            <th>Hạn trả</th>
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
                                        <span class="table-primary">
                                            <strong>${escapeHtml(
                                                item.memberName || "—"
                                            )}</strong>
                                            <small>${escapeHtml(
                                                item.membershipCode || "—"
                                            )}</small>
                                        </span>
                                    </td>
                                ` : ""}
                                <td>${formatDate(item.borrowedAt)}</td>
                                <td>${formatDate(item.dueAt)}</td>
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
                        {method: "POST"}
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
                                        config.maintenanceMessage || ""
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
                            body: JSON.stringify({enabled, message})
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
        elements.pageContent.innerHTML = `
            <section class="page-section">
                <div class="section-heading">
                    <div>
                        <h2>Thông tin và bảo mật</h2>
                        <p>Quản lý email và mật khẩu đăng nhập.</p>
                    </div>
                </div>

                <div class="settings-grid">
                    <div class="card">
                        <div class="card-heading">
                            <div>
                                <h2>Thông tin tài khoản</h2>
                                <p>Dữ liệu trong access token hiện tại.</p>
                            </div>
                        </div>
                        <div class="card-body">
                            <div class="profile-summary">
                                <span class="avatar">
                                    ${escapeHtml(
                                        displayName().slice(0, 1)
                                    )}
                                </span>
                                <div>
                                    <h3>${escapeHtml(displayName())}</h3>
                                    <p>${state.isAdmin
                                        ? "Quản trị viên hệ thống"
                                        : "Thành viên thư viện"}</p>
                                </div>
                            </div>
                            <div class="detail-list">
                                <div class="detail-row">
                                    <span>Tên đăng nhập</span>
                                    <strong>${escapeHtml(
                                        state.user.username
                                    )}</strong>
                                </div>
                                <div class="detail-row">
                                    <span>Email</span>
                                    <strong>${escapeHtml(
                                        state.user.email
                                    )}</strong>
                                </div>
                                <div class="detail-row">
                                    <span>Quyền</span>
                                    <strong>${state.isAdmin
                                        ? "ADMIN" : "USER"}</strong>
                                </div>
                            </div>
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

                    <div class="card">
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
                const button = event.currentTarget
                    .querySelector("[type='submit']");
                setButtonLoading(button, true, "Đang gửi...");
                try {
                    const result = await api(
                        "/api/auth/change-email/request",
                        {
                            method: "POST",
                            body: JSON.stringify(
                                formDataToObject(event.currentTarget)
                            )
                        }
                    );
                    toast("Đã gửi mã xác minh", result.message);
                    event.currentTarget.reset();
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
        openModal({
            title: "Khôi phục mật khẩu",
            subtitle: "Yêu cầu email đặt lại hoặc nhập token đã nhận.",
            body: `
                <div class="form-stack">
                    <form id="forgot-form" class="form-stack">
                        <label class="field">
                            <span>Email đăng ký</span>
                            <input name="email" type="email" required
                                   placeholder="email@example.com">
                        </label>
                        <button type="submit"
                                class="button button--secondary">
                            Gửi email khôi phục
                        </button>
                        <p id="forgot-form-error"
                           class="form-error hidden"></p>
                    </form>
                    <hr style="border:0;border-top:1px solid var(--border);width:100%">
                    <form id="reset-form" class="form-stack">
                        <label class="field">
                            <span>Token đặt lại mật khẩu</span>
                            <input name="token" required
                                   value="${attribute(resetToken)}">
                        </label>
                        <label class="field">
                            <span>Mật khẩu mới</span>
                            <input name="newPassword" type="password"
                                   required>
                        </label>
                        <button type="submit"
                                class="button button--primary">
                            Đặt lại mật khẩu
                        </button>
                        <p id="reset-form-error"
                           class="form-error hidden"></p>
                    </form>
                </div>
            `,
            showFooter: false
        });

        document.querySelector("#forgot-form")
            .addEventListener("submit", async (event) => {
                event.preventDefault();
                const button = event.currentTarget
                    .querySelector("[type='submit']");
                setButtonLoading(button, true, "Đang gửi...");
                try {
                    const result = await api(
                        "/api/auth/forgot-password",
                        {
                            method: "POST",
                            body: JSON.stringify(
                                formDataToObject(event.currentTarget)
                            )
                        },
                        false
                    );
                    toast("Đã tiếp nhận yêu cầu", result.message);
                } catch (error) {
                    showFormError("#forgot-form-error", error);
                } finally {
                    setButtonLoading(button, false);
                }
            });

        document.querySelector("#reset-form")
            .addEventListener("submit", async (event) => {
                event.preventDefault();
                const button = event.currentTarget
                    .querySelector("[type='submit']");
                setButtonLoading(button, true, "Đang cập nhật...");
                try {
                    const result = await api(
                        "/api/auth/reset-password",
                        {
                            method: "POST",
                            body: JSON.stringify(
                                formDataToObject(event.currentTarget)
                            )
                        },
                        false
                    );
                    closeModal();
                    setAuthMessage(result.message, "success");
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
        const requestOptions = {...options};
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
            payload = text ? {message: text} : null;
        }

        if (!response.ok) {
            const error = new Error(
                payload?.message || `Yêu cầu thất bại (${response.status})`
            );
            error.status = response.status;
            error.fieldErrors = payload?.fieldErrors || {};
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
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({refreshToken})
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
                    body: JSON.stringify({refreshToken})
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
                            <h2 id="modal-title">${escapeHtml(title)}</h2>
                            ${subtitle
                                ? `<p>${escapeHtml(subtitle)}</p>`
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
                                    ${escapeHtml(confirmText)}
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
                <strong>${escapeHtml(title)}</strong>
                <span>${escapeHtml(message || "")}</span>
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
                <strong>${escapeHtml(title)}</strong>
                <p>${escapeHtml(message)}</p>
            </div>
        `;
    }

    function pageSkeleton(count = 2) {
        return `
            <div style="display:grid;gap:14px">
                ${Array.from({length: count}, () =>
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
                    <span class="page-number">${response.page + 1}</span>
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
                ${escapeHtml(text)}
            </span>
        `;
    }

    function authorNames(book) {
        const names = (book.authors || []).map((author) => author.name);
        return names.length ? names.join(", ") : "Chưa cập nhật tác giả";
    }

    function setAuthMessage(message, type) {
        elements.authMessage.textContent = message;
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
        const fieldMessages = Object.values(error.fieldErrors || {});
        if (fieldMessages.length) {
            return `${error.message}: ${fieldMessages.join("; ")}`;
        }
        return error.message;
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
            button.textContent = label;
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
        return state.user?.username || state.user?.email || "Bạn";
    }

    function icon(name) {
        return `<svg aria-hidden="true"><use href="#${name}"></use></svg>`;
    }

    function bookInitials(title) {
        const words = String(title || "Sách")
            .trim()
            .split(/\s+/)
            .filter(Boolean);
        if (!words.length) {
            return "S";
        }
        return words.slice(0, 2)
            .map((word) => word.charAt(0))
            .join("")
            .toUpperCase();
    }

    function formatLongDate(value) {
        const formatted = new Intl.DateTimeFormat("vi-VN", {
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
        return new Intl.DateTimeFormat("vi-VN", {
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
        return new Intl.DateTimeFormat("vi-VN", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit"
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
