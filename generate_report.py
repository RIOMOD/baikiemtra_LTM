# -*- coding: utf-8 -*-
"""
Script tự động tạo tài liệu báo cáo Word (.docx) chuẩn chỉnh học thuật
dành cho bài kiểm tra Lập Trình Mạng PTIT: CHAT + FILE TCP JAVA SWING.
"""

import os
import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn

def set_cell_background(cell, fill_hex):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{fill_hex}"/>')
    tcPr.append(shd)

def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = parse_xml(f'<w:tcMar {nsdecls("w")}><w:top w:w="{top}" w:type="dxa"/><w:bottom w:w="{bottom}" w:type="dxa"/><w:left w:w="{left}" w:type="dxa"/><w:right w:w="{right}" w:type="dxa"/></w:tcMar>')
    tcPr.append(tcMar)

def add_code_block(doc, code_text):
    tbl = doc.add_table(rows=1, cols=1)
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    cell = tbl.cell(0, 0)
    set_cell_background(cell, "1E293B")
    set_cell_margins(cell, top=140, bottom=140, left=180, right=180)
    
    p = cell.paragraphs[0]
    p.paragraph_format.space_before = Pt(2)
    p.paragraph_format.space_after = Pt(2)
    p.paragraph_format.line_spacing = 1.15
    run = p.add_run(code_text.strip())
    run.font.name = "Consolas"
    run.font.size = Pt(8.5)
    run.font.color.rgb = RGBColor(226, 232, 240)
    
    p_after = doc.add_paragraph()
    p_after.paragraph_format.space_after = Pt(6)

def add_callout(doc, title, text, color_hex="4F46E5"):
    tbl = doc.add_table(rows=1, cols=1)
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    cell = tbl.cell(0, 0)
    set_cell_background(cell, "F1F5F9")
    set_cell_margins(cell, top=120, bottom=120, left=160, right=160)
    
    p = cell.paragraphs[0]
    p.paragraph_format.space_before = Pt(2)
    p.paragraph_format.space_after = Pt(2)
    r_t = p.add_run(f"📌 {title}: ")
    r_t.bold = True
    r_t.font.name = "Segoe UI"
    r_t.font.size = Pt(10)
    r_t.font.color.rgb = RGBColor(79, 70, 229)
    
    r_b = p.add_run(text)
    r_b.font.name = "Segoe UI"
    r_b.font.size = Pt(9.5)
    r_b.font.color.rgb = RGBColor(30, 41, 59)
    
    p_after = doc.add_paragraph()
    p_after.paragraph_format.space_after = Pt(6)

def add_image_or_placeholder(doc, caption, image_file=None):
    base_dir = os.path.dirname(os.path.abspath(__file__))
    img_full_path = os.path.join(base_dir, "screenshots", image_file) if image_file else None
    if img_full_path and os.path.exists(img_full_path):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_img.paragraph_format.space_before = Pt(8)
        p_img.paragraph_format.space_after = Pt(4)
        run = p_img.add_run()
        run.add_picture(img_full_path, width=Inches(5.8))
        
        p_cap = doc.add_paragraph()
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_cap.paragraph_format.space_before = Pt(2)
        p_cap.paragraph_format.space_after = Pt(10)
        r_cap = p_cap.add_run(f"Hình: {caption}")
        r_cap.font.name = "Segoe UI"
        r_cap.font.size = Pt(9)
        r_cap.italic = True
        r_cap.font.color.rgb = RGBColor(100, 116, 139)
    else:
        add_image_placeholder(doc, caption)

def add_image_placeholder(doc, caption):
    tbl = doc.add_table(rows=1, cols=1)
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    cell = tbl.cell(0, 0)
    set_cell_background(cell, "F8FAFC")
    set_cell_margins(cell, top=200, bottom=200, left=200, right=200)
    
    p = cell.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r1 = p.add_run("[ 🖼️ DÁN HÌNH ẢNH CHỤP MÀN HÌNH MINH HỌA TẠI ĐÂY ]\n")
    r1.bold = True
    r1.font.name = "Segoe UI"
    r1.font.size = Pt(10.5)
    r1.font.color.rgb = RGBColor(100, 116, 139)
    
    r2 = p.add_run(caption)
    r2.italic = True
    r2.font.name = "Segoe UI"
    r2.font.size = Pt(9.5)
    r2.font.color.rgb = RGBColor(71, 85, 105)
    
    p_cap = doc.add_paragraph()
    p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_cap.paragraph_format.space_before = Pt(4)
    p_cap.paragraph_format.space_after = Pt(10)
    r_cap = p_cap.add_run(f"Hình: {caption}")
    r_cap.font.name = "Segoe UI"
    r_cap.font.size = Pt(9)
    r_cap.italic = True
    r_cap.font.color.rgb = RGBColor(100, 116, 139)

def read_source_file(rel_path):
    base_dir = os.path.dirname(os.path.abspath(__file__))
    full_path = os.path.join(base_dir, rel_path)
    if os.path.exists(full_path):
        with open(full_path, "r", encoding="utf-8") as f:
            return f.read()
    return f"// Không tìm thấy file {rel_path}"

def build_report():
    doc = docx.Document()
    
    # Page Margins: 2.54 cm standard
    for sec in doc.sections:
        sec.top_margin = Inches(1.0)
        sec.bottom_margin = Inches(1.0)
        sec.left_margin = Inches(1.0)
        sec.right_margin = Inches(1.0)
        
    # Styles
    style_normal = doc.styles['Normal']
    style_normal.font.name = 'Times New Roman'
    style_normal.font.size = Pt(12)
    style_normal.font.color.rgb = RGBColor(30, 41, 59)
    style_normal.paragraph_format.line_spacing = 1.25
    style_normal.paragraph_format.space_after = Pt(4)

    # =========================================================================
    # TRANG BÌA
    # =========================================================================
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(2)
    r = p.add_run("HỌC VIỆN CÔNG NGHỆ BƯU CHÍNH VIỄN THÔNG")
    r.bold = True
    r.font.size = Pt(13)
    r.font.color.rgb = RGBColor(15, 23, 42)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(18)
    r = p.add_run("KHOA CÔNG NGHỆ THÔNG TIN")
    r.bold = True
    r.font.size = Pt(12)
    r.font.color.rgb = RGBColor(79, 70, 229)

    doc.add_paragraph().paragraph_format.space_after = Pt(36)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(10)
    r = p.add_run("BÁO CÁO BÀI KIỂM TRA KẾT THÚC HỌC PHẦN")
    r.bold = True
    r.font.size = Pt(15)
    r.font.color.rgb = RGBColor(100, 116, 139)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(14)
    r = p.add_run("MÔN HỌC: LẬP TRÌNH MẠNG")
    r.bold = True
    r.font.size = Pt(18)
    r.font.color.rgb = RGBColor(15, 23, 42)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(30)
    r = p.add_run("ĐỀ TÀI:\nXÂY DỰNG ỨNG DỤNG CHAT VÀ TRUYỀN TỆP TIN ĐA LUỒNG (MULTITHREAD)\nSỬ DỤNG GIAO THỨC TCP VỚI GIAO DIỆN JAVA SWING HIỆN ĐẠI")
    r.bold = True
    r.font.size = Pt(14)
    r.font.color.rgb = RGBColor(79, 70, 229)

    doc.add_paragraph().paragraph_format.space_after = Pt(40)

    # Student Info Table on Cover
    info_table = doc.add_table(rows=4, cols=2)
    info_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    data_info = [
        ("Giảng viên giảng dạy:", "Thầy Văn Tông Thành"),
        ("Sinh viên thực hiện:", "Nguyễn Công Trứ"),
        ("Mã số sinh viên (MSSV):", "N24DTCN088"),
        ("Lớp học phần:", "D24TXCNPM01-N")
    ]
    for idx, (label, val) in enumerate(data_info):
        r_c1 = info_table.cell(idx, 0).paragraphs[0].add_run(label)
        r_c1.bold = True
        r_c1.font.size = Pt(11.5)
        r_c2 = info_table.cell(idx, 1).paragraphs[0].add_run(val)
        r_c2.font.size = Pt(11.5)
        if idx in [1, 2]:
            r_c2.bold = True
            r_c2.font.color.rgb = RGBColor(79, 70, 229)

    doc.add_paragraph().paragraph_format.space_after = Pt(60)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("TP. HỒ CHÍ MINH - NĂM 2026")
    r.bold = True
    r.font.size = Pt(11.5)
    r.font.color.rgb = RGBColor(100, 116, 139)

    doc.add_page_break()

    # =========================================================================
    # NỘI DUNG CHÍNH
    # =========================================================================
    def add_h1(text):
        h = doc.add_heading(level=1)
        h.paragraph_format.space_before = Pt(16)
        h.paragraph_format.space_after = Pt(6)
        r = h.add_run(text)
        r.bold = True
        r.font.name = "Segoe UI"
        r.font.size = Pt(15)
        r.font.color.rgb = RGBColor(15, 23, 42)
        return h

    def add_h2(text):
        h = doc.add_heading(level=2)
        h.paragraph_format.space_before = Pt(12)
        h.paragraph_format.space_after = Pt(4)
        r = h.add_run(text)
        r.bold = True
        r.font.name = "Segoe UI"
        r.font.size = Pt(13)
        r.font.color.rgb = RGBColor(79, 70, 229)
        return h

    def add_p(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_after = Pt(5)
        p.paragraph_format.line_spacing = 1.25
        r = p.add_run(text)
        r.font.name = "Times New Roman"
        r.font.size = Pt(12)
        return p

    # --- CHƯƠNG 1 ---
    add_h1("CHƯƠNG 1: TỔNG QUAN VÀ YÊU CẦU ĐỀ BÀI")
    add_h2("1.1. Mục tiêu đề tài")
    add_p("Trong khuôn khổ học phần Lập Trình Mạng tại Học viện Công nghệ Bưu chính Viễn thông (PTIT), đề tài yêu cầu xây dựng một ứng dụng hoàn chỉnh giao tiếp Client - Server theo thời gian thực sử dụng giao thức truyền vận tin cậy TCP (Transmission Control Protocol) và thư viện đồ họa Java Swing.")
    add_p("Hệ thống giải quyết trọn vẹn hai bài toán trọng tâm của mạng máy tính:")
    add_p("1. Truyền thông tin nhắn văn bản tức thời (Instant Messaging): Cho phép nhiều Client đồng thời kết nối vào phòng chat chung hoặc nhắn tin riêng biệt (Whisper), bảo đảm tốc độ truyền tải cao và đồng bộ trạng thái.")
    add_p("2. Truyền tải tệp tin nhị phân dung lượng lớn (Binary File Transfer): Cho phép người dùng đính kèm và gửi các tệp tài liệu, hình ảnh, file nén (.docx, .pdf, .png, .zip) qua luồng mạng TCP một cách an toàn mà không làm ảnh hưởng đến luồng giao tiếp văn bản.")

    add_h2("1.2. Yêu cầu kỹ thuật chi tiết")
    add_p("Theo đề bài kiểm tra, chương trình phải đáp ứng các tiêu chuẩn khắt khe sau:")
    add_p("• Giao thức TCP: Sử dụng java.net.ServerSocket tại Server và java.net.Socket tại Client để thiết lập kênh liên lạc hướng kết nối (Connection-Oriented), đảm bảo tính toàn vẹn và không thất thoát dữ liệu.")
    add_p("• Xử lý Đa luồng (Multithread): Server phải có khả năng tiếp nhận và phục vụ đồng thời nhiều Client cùng một lúc mà không gây nghẽn (non-blocking). Mỗi kết nối được gán một luồng thực thi độc lập (ClientHandler).")
    add_p("• Gửi File: Tích hợp chức năng chọn tệp từ đĩa cứng người gửi, đóng gói dữ liệu nhị phân qua TCP, chuyển tiếp qua Server và hiển thị card tải tệp tại phía người nhận kèm nút bấm Lưu tệp.")
    add_p("• Thiết kế giao diện đẹp: Toàn bộ giao diện Java Swing được thiết kế theo ngôn ngữ thiết kế hiện đại Modern Luxury Slate & Indigo UI (Dark Mode), bo góc mượt mà, bong bóng chat phân biệt người gửi/người nhận, chỉ báo trạng thái online, bảng điều khiển Server trực quan.")

    # --- CHƯƠNG 2 ---
    add_h1("CHƯƠNG 2: CƠ SỞ LÝ THUYẾT VÀ CÔNG NGHỆ ÁP DỤNG")
    add_h2("2.1. Giao thức truyền vận tin cậy TCP (Transmission Control Protocol)")
    add_p("Giao thức TCP hoạt động ở Tầng Giao vận (Transport Layer) trong mô hình OSI và TCP/IP. Khác với UDP (truyền không kết nối, có thể mất gói), TCP cung cấp cơ chế bắt tay ba bước (Three-way Handshake) để thiết lập phiên, kiểm soát luồng (Flow Control), kiểm soát tắc nghẽn (Congestion Control) và xác nhận ACK đảm bảo dữ liệu đến đích chính xác, đúng thứ tự.")
    add_p("Đối với ứng dụng nhắn tin và truyền file, việc lựa chọn TCP là bắt buộc bởi vì:")
    add_p("• Không chấp nhận sai lệch dữ liệu: Nếu một byte dữ liệu trong file Word hay Zip bị lỗi, file sẽ bị hỏng hoàn toàn. Cơ chế Checksum và tự động truyền lại (Retransmission) của TCP loại bỏ rủi ro này.")
    add_p("• Đảm bảo đúng thứ tự gói tin: Tin nhắn đến sau không bao giờ bị hiển thị trước tin nhắn đến trước.")

    add_h2("2.2. Kỹ thuật Lập trình Đa luồng (Multithreading)")
    add_p("Trong mô hình Server truyền thống đơn luồng, hàm ServerSocket.accept() là hàm chặn (blocking call). Nếu chỉ có một luồng duy nhất, khi Server đang phục vụ Client A, các Client B và C sẽ bị treo không thể kết nối. Để khắc phục, chương trình áp dụng mô hình Worker Thread Pattern:")
    add_p("1. Luồng chính (Accept Thread): Chạy vòng lặp vô tận chỉ để lắng nghe và chấp thuận kết nối mới.")
    add_p("2. Luồng công nhân (ClientHandler Thread): Mỗi khi có kết nối mới được tạo, Server khởi tạo một đối tượng ClientHandler thực thi Runnable và chuyển sang một Thread riêng biệt. Luồng này chuyên trách việc đọc/ghi dữ liệu của client đó.")
    add_p("3. Luồng lắng nghe phía Client (Client Listener Thread): Phía Client tách riêng luồng giao diện (Event Dispatch Thread - EDT) và luồng nhận tin mạng, giúp giao diện không bị giật lag khi có lượng lớn dữ liệu đổ về.")

    add_h2("2.3. Cơ chế truyền File nhị phân qua DataInputStream và DataOutputStream")
    add_p("Để truyền file nhị phân qua luồng mạng mà không làm lỗi cú pháp văn bản UTF-8, chương trình xây dựng cấu trúc gói tin Packet chứa:")
    add_p("• Byte phân loại Type (0x06 = FILE_MSG)")
    add_p("• Tên tệp tin (String UTF-8)")
    add_p("• Kích thước tệp tin (long - 8 bytes)")
    add_p("• Mảng byte nhị phân dữ liệu thô (raw byte array) được đọc qua readFully(byte[])")

    # --- CHƯƠNG 3 ---
    add_h1("CHƯƠNG 3: THIẾT KẾ KIẾN TRÚC VÀ GIAO THỨC TRUYỀN THÔNG")
    add_h2("3.1. Cấu trúc gói tin truyền thông (Packet Specification)")
    add_p("Hệ thống chuẩn hóa giao tiếp giữa Client và Server qua lớp Packet (src/common/Packet.java) với các mã lệnh định danh:")
    
    # Table of Packet Types
    pkt_table = doc.add_table(rows=8, cols=3)
    pkt_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    headers = ["Mã Byte (Hex)", "Tên thông điệp", "Mục đích sử dụng"]
    for i, h in enumerate(headers):
        cell = pkt_table.cell(0, i)
        set_cell_background(cell, "334155")
        p = cell.paragraphs[0]
        r = p.add_run(h)
        r.bold = True
        r.font.color.rgb = RGBColor(255, 255, 255)
        
    rows_data = [
        ("0x01", "CONNECT", "Client gửi tên đăng nhập để yêu cầu tham gia phòng chat."),
        ("0x02", "CONNECT_ACK", "Server phản hồi chấp nhận hoặc từ chối (trùng tên, lỗi)."),
        ("0x03", "DISCONNECT", "Client thông báo chủ động ngắt kết nối và thoát phòng."),
        ("0x04", "USER_LIST", "Server đồng bộ danh sách toàn bộ các thành viên đang online."),
        ("0x05", "TEXT_MSG", "Tin nhắn văn bản trò chuyện (gửi chung hoặc gửi riêng)."),
        ("0x06", "FILE_MSG", "Truyền tệp tin nhị phân kèm metadata (tên file, dung lượng, mảng byte)."),
        ("0x07", "NOTIFICATION", "Thông báo trạng thái hệ thống (vào phòng, rời phòng, broadcast admin).")
    ]
    for row_idx, data in enumerate(rows_data, start=1):
        for col_idx, text in enumerate(data):
            cell = pkt_table.cell(row_idx, col_idx)
            if row_idx % 2 == 1:
                set_cell_background(cell, "F8FAFC")
            p = cell.paragraphs[0]
            p.add_run(text)

    add_callout(doc, "Tính Độc lập và An toàn", "Giao thức nhị phân qua DataOutputStream bảo đảm tính đóng gói (encapsulation), không bị nhầm lẫn giữa chuỗi văn bản thông thường và các byte đặc biệt của file nén/ảnh.")

    add_h2("3.2. Sơ đồ quy trình bắt tay và truyền nhận")
    add_p("Quy trình 1: Đăng nhập và Handshake")
    add_p("Client tạo kết nối TCP tới IP:Port -> Gửi gói CONNECT(username) -> Server kiểm tra trùng tên -> Nếu hợp lệ, Server lưu ClientHandler vào danh sách và gửi CONNECT_ACK(true), sau đó broadcast USER_LIST mới tới toàn bộ phòng.")
    add_p("Quy trình 2: Gửi tin nhắn văn bản (Text Chat)")
    add_p("Client nhập nội dung -> Gói TEXT_MSG được tạo và gửi tới Server -> Server kiểm tra trường Recipient: Nếu là 'ALL', Server duyệt danh sách client và chuyển tiếp (Broadcast); nếu là tên một user cụ thể, Server định tuyến gói tin riêng tới user đó và gửi bản sao cho người gửi.")
    add_p("Quy trình 3: Gửi và Lưu tệp tin (File Transfer)")
    add_p("Client chọn file qua JFileChooser -> Đọc byte từ đĩa cứng vào mảng byte[] trên luồng nền (Worker Thread) -> Gửi gói FILE_MSG qua TCP -> Server chuyển tiếp tới người nhận -> Người nhận nhìn thấy File Card hiện đại -> Nhấn 'Lưu tệp...' để ghi dữ liệu nhị phân xuống thư mục đích tùy chọn trên máy.")

    # --- CHƯƠNG 4 ---
    add_h1("CHƯƠNG 4: TOÀN BỘ MÃ NGUỒN VÀ GIẢI THÍCH CHI TIẾT")
    add_p("Toàn bộ dự án được tổ chức khoa học theo mô hình phân lớp rõ ràng:")
    add_p("• src/common/: Định nghĩa giao thức gói tin, hằng số và hệ thống giao diện chuẩn.")
    add_p("• src/server/: Máy chủ đa luồng ChatServer, bộ xử lý kết nối ClientHandler và giao diện điều khiển ServerGUI.")
    add_p("• src/client/: Trình điều khiển kết nối mạng ChatClient và giao diện người dùng ClientGUI.")
    add_p("• src/MainLauncher.java: Bộ điều phối khởi chạy trung tâm đa năng.")

    add_h2("4.1. File src/common/MessageType.java")
    add_code_block(doc, read_source_file("src/common/MessageType.java"))

    add_h2("4.2. File src/common/Packet.java")
    add_code_block(doc, read_source_file("src/common/Packet.java"))

    add_h2("4.3. File src/common/Theme.java")
    add_code_block(doc, read_source_file("src/common/Theme.java"))

    add_h2("4.4. File src/server/ClientHandler.java")
    add_code_block(doc, read_source_file("src/server/ClientHandler.java"))

    add_h2("4.5. File src/server/ChatServer.java")
    add_code_block(doc, read_source_file("src/server/ChatServer.java"))

    add_h2("4.6. File src/server/ServerGUI.java")
    add_code_block(doc, read_source_file("src/server/ServerGUI.java"))

    add_h2("4.7. File src/client/ChatClient.java")
    add_code_block(doc, read_source_file("src/client/ChatClient.java"))

    add_h2("4.8. File src/client/ClientGUI.java")
    add_code_block(doc, read_source_file("src/client/ClientGUI.java"))

    add_h2("4.9. File src/MainLauncher.java")
    add_code_block(doc, read_source_file("src/MainLauncher.java"))

    # --- CHƯƠNG 5 ---
    add_h1("CHƯƠNG 5: KẾT QUẢ THỰC NGHIỆM VÀ HÌNH ẢNH MINH HỌA")
    add_p("Chương trình đã được kiểm thử thực tế trên hệ thống máy tính với đầy đủ các kịch bản:")

    add_h2("5.1. Kịch bản 1: Khởi động Server Control Center")
    add_p("Server khởi động tại Port mặc định 8888. Giao diện hiển thị trực quan các thông số: Số lượng Clients kết nối = 0, Tổng tin nhắn = 0, Tệp đã truyền = 0, Đồng hồ thời gian hoạt động Uptime đếm giây chính xác.")
    add_image_or_placeholder(doc, "Minh họa 1: Giao diện Server Control Center khi vừa khởi động lắng nghe kết nối trên Port 8888", "screenshot_server.png")

    add_h2("5.2. Kịch bản 2: Đăng nhập nhiều Client đồng thời (Đa luồng)")
    add_p("Mở 2 cửa sổ Client với nickname 'Alice' và 'Bob'. Sau khi nhấn 'Tham Gia Phòng Chat', giao diện chuyển mượt mà sang phòng chat. Bảng danh sách thành viên online tại Sidebar của từng Client và trên Server tự động cập nhật tên và avatar.")
    add_image_or_placeholder(doc, "Minh họa 2: Màn hình đăng nhập phong cách Hoàng Gia Vàng Kim sang trọng", "screenshot_client_login.png")

    add_h2("5.3. Kịch bản 3: Thử nghiệm Chat công khai và Chat riêng")
    add_p("Alice gửi tin nhắn 'Xin chào cả phòng!'. Bob lập tức nhận được tin nhắn trong bong bóng chat bên trái kèm tên và thời gian gửi. Alice gửi tin nhắn riêng cho Bob bằng cách chọn người nhận trong ComboBox hoặc click vào tên Bob trong danh sách.")
    add_image_or_placeholder(doc, "Minh họa 3: Bong bóng chat phân chia rõ ràng bên phải (người gửi) và bên trái (người nhận)", "screenshot_client_chat.png")

    add_h2("5.4. Kịch bản 4: Thử nghiệm Truyền tệp tin (File Transfer)")
    add_p("Alice nhấn nút 'Gửi File', chọn một tài liệu Word/PDF/ảnh từ máy tính. Tệp tin được mã hóa nhị phân và gửi qua TCP. Phía Bob hiển thị một File Card nổi bật có biểu tượng tệp, tên tệp và dung lượng chính xác. Bob nhấn nút 'Lưu tệp...' và chọn thư mục lưu trữ thành công.")
    add_image_or_placeholder(doc, "Minh họa 4: Thẻ tệp tin (File Card) gửi từ Alice và hộp thoại lưu tệp thành công trên máy Bob", "screenshot_client_chat.png")

    add_h2("5.5. Kịch bản 5: Chức năng Quản trị Server (Kick Client & Phát thông báo)")
    add_p("Người quản trị tại Server Control Center gõ thông báo 'Chuẩn bị bảo trì' và nhấn 'Gửi thông báo'. Cả hai Client đều nhận được thông báo hệ thống màu nổi bật. Quản trị viên chọn client 'Bob' trong bảng và nhấn 'Kick Client', client bị ngắt kết nối an toàn kèm lý do hiển thị rõ ràng.")
    add_image_or_placeholder(doc, "Minh họa 5: Server gửi thông báo hệ thống toàn mạng và quản lý danh sách Client kết nối", "screenshot_server.png")

    # --- CHƯƠNG 6 ---
    add_h1("CHƯƠNG 6: KẾT LUẬN VÀ ĐÁNH GIÁ")
    add_p("Sau quá trình nghiên cứu và thực hiện, đề tài đã đạt được toàn diện các mục tiêu đề ra:")
    add_p("1. Đáp ứng 100% yêu cầu đề bài: Xây dựng thành công ứng dụng Chat và Truyền File TCP có giao diện Java Swing đẹp mắt, áp dụng đa luồng (Multithread) hiệu quả.")
    add_p("2. Giao diện trực quan, thẩm mỹ cao: Áp dụng tư duy thiết kế hiện đại (Modern Dark UI), bố cục khoa học, chuyển cảnh mượt mà giữa các màn hình.")
    add_p("3. Kiến trúc code chuẩn mực: Phân tách rõ ràng giữa tầng Mạng (Networking Engine) và tầng Giao diện (Presentation Layer), dễ bảo trì và mở rộng.")

    primary_filename = "NguyenCongTru-N24DTCN088.docx"
    output_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), primary_filename)
    try:
        doc.save(output_path)
        print(f"[THÀNH CÔNG] Đã tạo file báo cáo Word: {output_path}")
    except PermissionError:
        alt_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "NguyenCongTru-N24DTCN088_updated.docx")
        doc.save(alt_path)
        print(f"[THÔNG BÁO] File '{primary_filename}' đang được mở trong Word.")
        print(f"[THÀNH CÔNG] Đã lưu bản cập nhật mới nhất vào: {alt_path}")

    # Đồng bộ sang các file báo cáo khác
    for legacy_name in ["hovaten-mssv.docx", "hovaten-mssv_updated.docx"]:
        try:
            legacy_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), legacy_name)
            doc.save(legacy_path)
        except Exception:
            pass

if __name__ == "__main__":
    build_report()
