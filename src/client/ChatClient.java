package client;

import common.MessageType;
import common.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * Lớp xử lý mạng TCP Socket phía Client (Network Client Engine).
 * Chạy luồng nền nhận dữ liệu liên tục và các phương thức gửi tin nhắn/file đa luồng.
 */
public class ChatClient {
    public interface ClientEventListener {
        void onConnected(String username);
        void onConnectionFailed(String reason);
        void onDisconnected(String reason);
        void onTextMessageReceived(Packet packet);
        void onFileReceived(Packet packet);
        void onUserListUpdated(List<String> userList);
        void onNotification(String message);
        void onKicked(String reason);
    }

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Thread listenerThread;
    private volatile boolean isConnected = false;

    private String username = "";
    private String host = "localhost";
    private int port = 8888;

    private ClientEventListener listener;

    public ChatClient() {}

    public void setEventListener(ClientEventListener listener) {
        this.listener = listener;
    }

    /**
     * Bắt đầu kết nối tới TCP Server và thực hiện Handshake
     */
    public void connect(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;

        new Thread(() -> {
            try {
                socket = new Socket(host, port);
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());

                // Gửi gói CONNECT kèm tên người dùng
                Packet connectPacket = Packet.createConnect(username);
                connectPacket.writeTo(dos);

                // Chờ phản hồi CONNECT_ACK từ Server
                Packet ack = Packet.readFrom(dis);
                if (ack.getType() == MessageType.CONNECT_ACK) {
                    if (ack.isStatusOk()) {
                        isConnected = true;
                        if (listener != null) {
                            listener.onConnected(username);
                        }
                        // Bắt đầu luồng nghe dữ liệu liên tục từ Server
                        startListenerThread();
                    } else {
                        String reason = ack.getContent();
                        closeResources();
                        if (listener != null) {
                            listener.onConnectionFailed(reason);
                        }
                    }
                } else {
                    closeResources();
                    if (listener != null) {
                        listener.onConnectionFailed("Phản hồi máy chủ không hợp lệ!");
                    }
                }
            } catch (IOException e) {
                closeResources();
                if (listener != null) {
                    listener.onConnectionFailed("Không thể kết nối đến " + host + ":" + port + " (" + e.getMessage() + ")");
                }
            }
        }, "Client-Connect-Thread").start();
    }

    /**
     * Luồng nền lắng nghe gói tin từ Server
     */
    private void startListenerThread() {
        listenerThread = new Thread(() -> {
            while (isConnected && socket != null && !socket.isClosed()) {
                try {
                    Packet packet = Packet.readFrom(dis);
                    switch (packet.getType()) {
                        case MessageType.TEXT_MSG:
                            if (listener != null) listener.onTextMessageReceived(packet);
                            break;

                        case MessageType.FILE_MSG:
                            if (listener != null) listener.onFileReceived(packet);
                            break;

                        case MessageType.USER_LIST:
                            if (listener != null) listener.onUserListUpdated(packet.getUserList());
                            break;

                        case MessageType.NOTIFICATION:
                            if (listener != null) listener.onNotification(packet.getContent());
                            break;

                        case MessageType.KICK:
                            isConnected = false;
                            if (listener != null) listener.onKicked(packet.getContent());
                            closeResources();
                            return;

                        default:
                            break;
                    }
                } catch (IOException e) {
                    if (isConnected) {
                        isConnected = false;
                        if (listener != null) {
                            listener.onDisconnected("Đứt kết nối máy chủ: " + e.getMessage());
                        }
                    }
                    break;
                }
            }
            closeResources();
        }, "Client-Listener-Thread");
        listenerThread.start();
    }

    /**
     * Gửi tin nhắn văn bản (Broadcast hoặc riêng)
     */
    public void sendMessage(String recipient, String message) {
        if (!isConnected || dos == null) return;
        new Thread(() -> {
            try {
                Packet p = Packet.createTextMessage(username, recipient, message);
                p.writeTo(dos);
            } catch (IOException e) {
                if (listener != null) {
                    listener.onNotification("❌ Lỗi gửi tin nhắn: " + e.getMessage());
                }
            }
        }, "Send-Message-Worker").start();
    }

    /**
     * Gửi file nhị phân qua TCP trên luồng riêng biệt để tránh đơ giao diện
     */
    public void sendFile(String recipient, File file, Runnable onSuccess, java.util.function.Consumer<String> onError) {
        if (!isConnected || dos == null) {
            if (onError != null) onError.accept("Chưa kết nối tới máy chủ!");
            return;
        }

        new Thread(() -> {
            try {
                long length = file.length();
                if (length > 100 * 1024 * 1024) { // 100MB
                    if (onError != null) onError.accept("Tệp quá lớn (> 100MB). Vui lòng chọn tệp nhỏ hơn.");
                    return;
                }

                byte[] data = new byte[(int) length];
                try (FileInputStream fis = new FileInputStream(file)) {
                    int bytesRead = 0;
                    while (bytesRead < length) {
                        int read = fis.read(data, bytesRead, (int) (length - bytesRead));
                        if (read == -1) break;
                        bytesRead += read;
                    }
                }

                Packet filePacket = Packet.createFileMessage(username, recipient, file.getName(), data);
                filePacket.writeTo(dos);

                if (onSuccess != null) {
                    onSuccess.run();
                }
            } catch (Exception e) {
                if (onError != null) {
                    onError.accept("Lỗi khi gửi tệp: " + e.getMessage());
                }
            }
        }, "Send-File-Worker").start();
    }

    /**
     * Chủ động ngắt kết nối
     */
    public void disconnect() {
        if (!isConnected) return;
        isConnected = false;
        new Thread(() -> {
            try {
                if (dos != null) {
                    Packet p = Packet.createDisconnect(username);
                    p.writeTo(dos);
                }
            } catch (IOException ignored) {}
            closeResources();
            if (listener != null) {
                listener.onDisconnected("Bạn đã rời phòng chat.");
            }
        }).start();
    }

    private void closeResources() {
        try {
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    public boolean isConnected() { return isConnected; }
    public String getUsername() { return username; }
    public String getHost() { return host; }
    public int getPort() { return port; }
}
