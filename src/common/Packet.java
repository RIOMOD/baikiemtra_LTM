package common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Đóng gói gói tin TCP (Packet) dùng chung giữa Client và Server.
 * Hỗ trợ giao tiếp văn bản tiếng Việt UTF-8 và truyền dữ liệu nhị phân file dung lượng lớn.
 */
public class Packet {
    private byte type;
    private String sender = "";
    private String recipient = "ALL";
    private String content = "";
    private String timestamp = "";
    private String fileName = "";
    private long fileSize = 0;
    private byte[] fileData = null;
    private List<String> userList = new ArrayList<>();
    private boolean statusOk = true;

    public Packet(byte type) {
        this.type = type;
        this.timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    // Factory methods
    public static Packet createConnect(String username) {
        Packet p = new Packet(MessageType.CONNECT);
        p.sender = username;
        return p;
    }

    public static Packet createConnectAck(boolean ok, String msg) {
        Packet p = new Packet(MessageType.CONNECT_ACK);
        p.statusOk = ok;
        p.content = msg;
        return p;
    }

    public static Packet createTextMessage(String sender, String recipient, String content) {
        Packet p = new Packet(MessageType.TEXT_MSG);
        p.sender = sender;
        p.recipient = (recipient == null || recipient.trim().isEmpty()) ? "ALL" : recipient;
        p.content = content;
        return p;
    }

    public static Packet createFileMessage(String sender, String recipient, String fileName, byte[] data) {
        Packet p = new Packet(MessageType.FILE_MSG);
        p.sender = sender;
        p.recipient = (recipient == null || recipient.trim().isEmpty()) ? "ALL" : recipient;
        p.fileName = fileName;
        p.fileData = data;
        p.fileSize = data != null ? data.length : 0;
        return p;
    }

    public static Packet createUserList(List<String> users) {
        Packet p = new Packet(MessageType.USER_LIST);
        p.userList = new ArrayList<>(users);
        return p;
    }

    public static Packet createNotification(String message) {
        Packet p = new Packet(MessageType.NOTIFICATION);
        p.content = message;
        return p;
    }

    public static Packet createDisconnect(String username) {
        Packet p = new Packet(MessageType.DISCONNECT);
        p.sender = username;
        return p;
    }

    public static Packet createKick(String targetUser, String reason) {
        Packet p = new Packet(MessageType.KICK);
        p.recipient = targetUser;
        p.content = reason;
        return p;
    }

    /**
     * Ghi gói tin vào DataOutputStream qua TCP
     */
    public void writeTo(DataOutputStream out) throws IOException {
        synchronized (out) {
            out.writeByte(type);
            switch (type) {
                case MessageType.CONNECT:
                    out.writeUTF(sender != null ? sender : "");
                    break;

                case MessageType.CONNECT_ACK:
                    out.writeBoolean(statusOk);
                    out.writeUTF(content != null ? content : "");
                    break;

                case MessageType.DISCONNECT:
                    out.writeUTF(sender != null ? sender : "");
                    break;

                case MessageType.USER_LIST:
                    out.writeInt(userList.size());
                    for (String u : userList) {
                        out.writeUTF(u);
                    }
                    break;

                case MessageType.TEXT_MSG:
                    out.writeUTF(sender != null ? sender : "");
                    out.writeUTF(recipient != null ? recipient : "ALL");
                    out.writeUTF(timestamp != null ? timestamp : "");
                    out.writeUTF(content != null ? content : "");
                    break;

                case MessageType.FILE_MSG:
                    out.writeUTF(sender != null ? sender : "");
                    out.writeUTF(recipient != null ? recipient : "ALL");
                    out.writeUTF(timestamp != null ? timestamp : "");
                    out.writeUTF(fileName != null ? fileName : "unnamed_file");
                    out.writeLong(fileSize);
                    if (fileData != null && fileSize > 0) {
                        out.write(fileData);
                    }
                    break;

                case MessageType.NOTIFICATION:
                    out.writeUTF(timestamp != null ? timestamp : "");
                    out.writeUTF(content != null ? content : "");
                    break;

                case MessageType.KICK:
                    out.writeUTF(recipient != null ? recipient : "");
                    out.writeUTF(content != null ? content : "");
                    break;

                default:
                    break;
            }
            out.flush();
        }
    }

    /**
     * Đọc gói tin từ DataInputStream qua TCP
     */
    public static Packet readFrom(DataInputStream in) throws IOException {
        byte type = in.readByte();
        Packet p = new Packet(type);
        switch (type) {
            case MessageType.CONNECT:
                p.sender = in.readUTF();
                break;

            case MessageType.CONNECT_ACK:
                p.statusOk = in.readBoolean();
                p.content = in.readUTF();
                break;

            case MessageType.DISCONNECT:
                p.sender = in.readUTF();
                break;

            case MessageType.USER_LIST:
                int count = in.readInt();
                p.userList = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    p.userList.add(in.readUTF());
                }
                break;

            case MessageType.TEXT_MSG:
                p.sender = in.readUTF();
                p.recipient = in.readUTF();
                p.timestamp = in.readUTF();
                p.content = in.readUTF();
                break;

            case MessageType.FILE_MSG:
                p.sender = in.readUTF();
                p.recipient = in.readUTF();
                p.timestamp = in.readUTF();
                p.fileName = in.readUTF();
                p.fileSize = in.readLong();
                if (p.fileSize > 0) {
                    if (p.fileSize > 100 * 1024 * 1024) { // Giới hạn an toàn 100MB
                        throw new IOException("File kích thước quá lớn (>100MB): " + p.fileSize);
                    }
                    p.fileData = new byte[(int) p.fileSize];
                    in.readFully(p.fileData);
                }
                break;

            case MessageType.NOTIFICATION:
                p.timestamp = in.readUTF();
                p.content = in.readUTF();
                break;

            case MessageType.KICK:
                p.recipient = in.readUTF();
                p.content = in.readUTF();
                break;

            default:
                throw new IOException("Gói tin không xác định: " + type);
        }
        return p;
    }

    // Getters and Setters
    public byte getType() { return type; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }
    public List<String> getUserList() { return userList; }
    public void setUserList(List<String> userList) { this.userList = userList; }
    public boolean isStatusOk() { return statusOk; }
    public void setStatusOk(boolean statusOk) { this.statusOk = statusOk; }

    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
