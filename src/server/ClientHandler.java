package server;

import common.MessageType;
import common.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Luồng xử lý độc lập (Multi-threaded Worker) cho từng kết nối TCP Client tại Server.
 */
public class ClientHandler implements Runnable {
    private final ChatServer server;
    private final Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String username = "";
    private final String clientIp;
    private final int clientPort;
    private final String connectTime;
    private volatile boolean isRunning = true;

    public ClientHandler(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
        this.clientIp = socket.getInetAddress().getHostAddress();
        this.clientPort = socket.getPort();
        this.connectTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    @Override
    public void run() {
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            // 1. Bước xác thực Handshake
            Packet initPacket = Packet.readFrom(dis);
            if (initPacket.getType() == MessageType.CONNECT) {
                String requestedName = initPacket.getSender() != null ? initPacket.getSender().trim() : "";
                if (requestedName.isEmpty()) {
                    sendPacket(Packet.createConnectAck(false, "Tên đăng nhập không được để trống!"));
                    close();
                    return;
                }

                if (server.isUsernameTaken(requestedName)) {
                    sendPacket(Packet.createConnectAck(false, "Tên '" + requestedName + "' đã có người sử dụng!"));
                    close();
                    return;
                }

                this.username = requestedName;
                sendPacket(Packet.createConnectAck(true, "Chào mừng " + username + " tham gia phòng chat!"));

                // Đăng ký client thành công vào Server
                server.registerClient(this);
                server.log("[KẾT NỐI] Client '" + username + "' đã đăng nhập từ " + clientIp + ":" + clientPort);
                server.broadcastNotification("Người dùng [" + username + "] vừa tham gia phòng chat.");
                server.broadcastUserList();
            } else {
                sendPacket(Packet.createConnectAck(false, "Gói tin bắt đầu không hợp lệ!"));
                close();
                return;
            }

            // 2. Vòng lặp nhận dữ liệu liên tục từ Client
            while (isRunning && !socket.isClosed()) {
                Packet packet = Packet.readFrom(dis);
                switch (packet.getType()) {
                    case MessageType.TEXT_MSG:
                        server.handleTextMessage(this, packet);
                        break;

                    case MessageType.FILE_MSG:
                        server.handleFileMessage(this, packet);
                        break;

                    case MessageType.DISCONNECT:
                        server.log("[NGẮT KẾT NỐI] Client '" + username + "' chủ động rời phòng.");
                        isRunning = false;
                        break;

                    default:
                        server.log("[CẢNH BÁO] Nhận gói tin lạ (type=" + packet.getType() + ") từ " + username);
                        break;
                }
            }
        } catch (IOException e) {
            if (isRunning) {
                server.log("[MẤT KẾT NỐI] Đứt kết nối với '" + username + "' (" + clientIp + ":" + clientPort + ")");
            }
        } finally {
            close();
        }
    }

    /**
     * Gửi gói tin an toàn sang Client qua luồng đầu ra
     */
    public synchronized void sendPacket(Packet p) {
        if (dos != null && !socket.isClosed()) {
            try {
                p.writeTo(dos);
            } catch (IOException e) {
                server.log("[LỖI GỬI] Không thể gửi gói tin tới " + username + ": " + e.getMessage());
            }
        }
    }

    /**
     * Đóng kết nối an toàn và giải phóng tài nguyên
     */
    public void close() {
        isRunning = false;
        try {
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}

        server.unregisterClient(this);
    }

    // Getters
    public String getUsername() { return username; }
    public String getClientIp() { return clientIp; }
    public int getClientPort() { return clientPort; }
    public String getConnectTime() { return connectTime; }
    public Socket getSocket() { return socket; }
}
