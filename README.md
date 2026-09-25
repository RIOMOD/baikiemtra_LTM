# BÀI KIỂM TRA HỌC PHẦN: LẬP TRÌNH MẠNG (PTIT)
## ĐỀ TÀI: ỨNG DỤNG CHAT & TRUYỀN FILE ĐA LUỒNG (MULTITHREAD) QUA TCP VỚI JAVA SWING

- **Học viện:** Học Viện Công Nghệ Bưu Chính Viễn Thông (PTIT)
- **Khoa:** Công Nghệ Thông Tin
- **Giảng viên hướng dẫn:** Thầy Văn Tông Thành
- **Sinh viên thực hiện:** Nguyễn Công Trứ
- **Mã số sinh viên (MSSV):** N24DTCN088
- **Lớp học phần:** D24TXCNPM01-N
- **Ngôn ngữ & Thư viện:** Java 21 (JDK), Pure Java Swing (Modern Luxury Slate & Indigo UI), `java.net.ServerSocket` & `java.net.Socket`, TCP Data Streams.

---

## 📌 1. Giới thiệu Đề tài
Dự án được xây dựng hoàn chỉnh đáp ứng 100% các tiêu chí của đề bài kiểm tra:
1. **Giao thức mạng TCP:** Sử dụng kết nối Socket tin cậy, không mất gói tin, bảo đảm tính toàn vẹn dữ liệu.
2. **Kiến trúc Đa luồng (Multithread):** Server có khả năng phục vụ đồng thời hàng loạt Client kết nối song song mà không bị nghẽn (Non-blocking).
3. **Chức năng Chat:** Hỗ trợ chat công khai (Broadcast toàn phòng) và chat riêng tư (Whisper 1-1).
4. **Chức năng Truyền File nhị phân (File Transfer):** Cho phép Client đính kèm tệp tin bất kỳ (.png, .jpg, .docx, .pdf, .zip...) qua mạng TCP. Người nhận xem trước thông tin và lưu file về máy tính.
5. **Giao diện hiện đại (Modern UI):** Thiết kế Dark Mode phong cách Telegram/Discord sang trọng, bong bóng chat bo góc, avatar tròn, thanh cuộn mượt mà, thanh công cụ emoji nhanh.
6. **Bảng điều khiển Quản trị Server (Server Control Center):** Thống kê thời gian thực số lượng clients online, tổng số tin nhắn, tổng số file đã truyền, đồng hồ đếm Uptime, console log thời gian thực, bảng quản lý client kèm tính năng Kick người dùng và phát thanh thông báo (Broadcast).

---

## 📂 2. Cấu trúc Thư mục Dự án

```
cuoiky/
├── src/
│   ├── common/
│   │   ├── MessageType.java         # Định nghĩa mã Byte các loại gói tin (CONNECT, MSG, FILE...)
│   │   ├── Packet.java              # Đóng gói và giải mã gói tin nhị phân qua TCP Stream
│   │   └── Theme.java               # Hệ thống thiết kế giao diện Slate & Indigo, custom components
│   ├── server/
│   │   ├── ChatServer.java          # Bộ điều khiển Server TCP, quản lý danh sách Client, định tuyến tin nhắn
│   │   ├── ClientHandler.java       # Luồng độc lập (Worker Thread) phục vụ từng Client kết nối
│   │   └── ServerGUI.java           # Giao diện Mission Control Dashboard dành cho Quản trị viên
│   ├── client/
│   │   ├── ChatClient.java          # Bộ máy mạng Client, luồng lắng nghe nền (Background Listener)
│   │   └── ClientGUI.java           # Giao diện Chat phong cách Telegram/Discord (Login + Chat Room)
│   ├── test/
│   │   └── FunctionalTest.java      # Script kiểm thử tự động toàn diện các tính năng Socket & File
│   └── MainLauncher.java            # Bộ điều khiển trung tâm (All-In-One Launcher Portal)
├── bin/                             # Chứa toàn bộ các file .class đã biên dịch
├── run_all.bat                      # Khởi chạy Bộ điều khiển trung tâm (Launcher)
├── run_server.bat                   # Khởi chạy trực tiếp Server Control Center
├── run_client.bat                   # Khởi chạy trực tiếp Client
├── generate_report.py               # Script tạo tự động file báo cáo Word .docx
├── NguyenCongTru-N24DTCN088.docx    # File báo cáo Word hoàn chỉnh theo yêu cầu đề bài
└── README.md                        # Hướng dẫn chi tiết dự án
```

---

## 🚀 3. Hướng dẫn Khởi chạy Ứng dụng

### Cách 1: Sử dụng Bộ điều khiển Trung tâm (Khuyên dùng)
Chỉ cần nhấp đúp chuột vào file:
👉 `run_all.bat`
Hệ thống sẽ tự động biên dịch và mở cửa sổ Launcher. Bạn có thể nhấn:
- **"1. Mở Server Control Center"**: Mở màn hình quản trị Server.
- **"2. Mở Cửa Sổ Client Mới"**: Mở một cửa sổ Client mới.
- **"3. Khởi Chạy Bộ Thử Nghiệm Nhanh"**: Tự động mở Server và 2 Client cùng lúc để test ngay lập tức!

### Cách 2: Khởi chạy riêng lẻ từng ứng dụng
- Mở Server: Nhấp đúp `run_server.bat`
- Mở Client: Nhấp đúp `run_client.bat`

---

## 🧪 4. Kịch bản Thử nghiệm (Test Walkthrough)
1. **Khởi động Server:**
   - Mở Server, giữ nguyên cổng mặc định `8888`, nhấn `▶ Khởi động Server`.
   - Trạng thái chuyển sang `● ĐANG CHẠY (PORT 8888)` với màu xanh lá rực rỡ.
2. **Đăng nhập Client 1 (Alice):**
   - Mở Client, nhập IP `127.0.0.1`, Port `8888`, Tên: `Alice`.
   - Nhấn `🚀 Tham Gia Phòng Chat`. Cửa sổ chuyển sang giao diện phòng chat.
3. **Đăng nhập Client 2 (Bob):**
   - Mở Client thứ 2, nhập Tên: `Bob`.
   - Nhấn `🚀 Tham Gia Phòng Chat`. Cả Alice và Bob sẽ thấy avatar và tên của nhau xuất hiện trên danh sách online ở Sidebar.
4. **Chat công khai & Chat riêng:**
   - Alice gõ tin nhắn `"Xin chào cả phòng!"` -> Bob lập tức nhận được.
   - Bob chọn người nhận `"Alice"` trong ô "Gửi tới:", gõ `"Đây là tin nhắn bí mật"` -> Tin nhắn gửi riêng tư chỉ có Alice và Bob thấy.
5. **Gửi File qua mạng TCP:**
   - Alice bấm `📎 Gửi File`, chọn một tài liệu hoặc hình ảnh bất kỳ.
   - Phía Bob xuất hiện File Card màu xanh nổi bật hiển thị tên file và dung lượng.
   - Bob bấm `💾 Lưu Tệp...` để lưu file về máy tính.
6. **Chức năng Quản trị Server:**
   - Quản trị viên nhập thông báo hệ thống ở thanh dưới cùng -> Gửi tới tất cả Client.
   - Quản trị viên chọn tên Client trong bảng và bấm `⛔ Kick Client` -> Client bị đăng xuất tức thì.

---

## 📄 5. Báo cáo Word (`NguyenCongTru-N24DTCN088.docx`)
File báo cáo `NguyenCongTru-N24DTCN088.docx` đã được biên soạn đầy đủ theo chuẩn học thuật của Học viện PTIT:
- Trang bìa chuẩn có tên Giảng viên hướng dẫn: Thầy Văn Tông Thành.
- Thông tin Sinh viên thực hiện: **Nguyễn Công Trứ** - MSSV: **N24DTCN088** - Lớp: **D24TXCNPM01-N**.
- Mục lục 6 chương chi tiết: Tổng quan, Cơ sở lý thuyết TCP/Đa luồng, Thiết kế giao thức, Toàn bộ Source code có định dạng đẹp mắt, Các kịch bản thực nghiệm và Kết luận.
- Đã chèn sẵn hình ảnh minh họa thực nghiệm thực tế từ thư mục `screenshots/`.
- Chạy lại `python generate_report.py` bất cứ lúc nào để tái tạo lại file báo cáo Word.
