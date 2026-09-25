package common;

/**
 * Định nghĩa các loại thông điệp (Packet Type) trong giao thức TCP Chat + File.
 */
public class MessageType {
    public static final byte CONNECT = 0x01;       // Client gửi yêu cầu kết nối kèm username
    public static final byte CONNECT_ACK = 0x02;   // Server phản hồi chấp nhận/từ chối kết nối
    public static final byte DISCONNECT = 0x03;    // Client thông báo ngắt kết nối
    public static final byte USER_LIST = 0x04;     // Server cập nhật danh sách người dùng online
    public static final byte TEXT_MSG = 0x05;      // Tin nhắn văn bản (broadcast hoặc private)
    public static final byte FILE_MSG = 0x06;      // Truyền tệp tin nhị phân (ảnh, tài liệu, zip...)
    public static final byte NOTIFICATION = 0x07;  // Thông báo hệ thống (tham gia, rời phòng, kick...)
    public static final byte KICK = 0x08;          // Server kick một client
}
