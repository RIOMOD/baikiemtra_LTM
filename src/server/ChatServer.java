package server;

import common.Packet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Lớp điều khiển Server TCP Đa luồng (Multi-threaded TCP Chat Server).
 * Quản lý kết nối, định tuyến tin nhắn, chuyển tiếp file nhị phân và thống kê thời gian thực.
 */
public class ChatServer {
    public interface ServerEventListener {
        void onLog(String logMessage);
        void onClientsUpdated(List<ClientHandler> activeClients);
        void onStatsUpdated(int activeCount, long totalMsg, long totalFiles);
        void onStatusChanged(boolean running, String message);
    }

    private int port = 8888;
    private ServerSocket serverSocket;
    private Thread acceptThread;
    private volatile boolean isRunning = false;

    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final AtomicLong totalMessages = new AtomicLong(0);
    private final AtomicLong totalFiles = new AtomicLong(0);
    private long startTimeMillis = 0;

    private ServerEventListener listener;

    public ChatServer() {}

    public void setEventListener(ServerEventListener listener) {
        this.listener = listener;
    }

    /**
     * Bắt đầu lắng nghe kết nối TCP trên cổng chỉ định
     */
    public synchronized void start(int port) throws IOException {
        if (isRunning) return;
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        this.isRunning = true;
        this.startTimeMillis = System.currentTimeMillis();

        notifyStatus(true, "Server đang chạy trên cổng " + port);
        log("[SERVER] Bắt đầu lắng nghe kết nối tại cổng " + port + "...");

        acceptThread = new Thread(() -> {
            while (isRunning && !serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(this, clientSocket);
                    // Mỗi client được cấp 1 Thread xử lý riêng biệt (Multi-threaded Architecture)
                    new Thread(handler, "ClientThread-" + clientSocket.getRemoteSocketAddress()).start();
                } catch (IOException e) {
                    if (isRunning) {
                        log("[LỖI ACCEPT] " + e.getMessage());
                    }
                }
            }
        }, "Server-Accept-Thread");
        acceptThread.start();
    }

    /**
     * Dừng Server và đóng toàn bộ kết nối hiện hành
     */
    public synchronized void stop() {
        if (!isRunning) return;
        isRunning = false;

        log("[SERVER] Đang dừng máy chủ...");
        broadcastNotification("⚠️ Server đang tắt. Tất cả kết nối sẽ bị đóng.");

        // Đóng toàn bộ client
        for (ClientHandler ch : clients) {
            ch.close();
        }
        clients.clear();

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}

        if (acceptThread != null && acceptThread.isAlive()) {
            acceptThread.interrupt();
        }

        notifyStatus(false, "Server đã dừng.");
        notifyClientsUpdated();
        notifyStatsUpdated();
        log("[SERVER] Máy chủ đã dừng an toàn.");
    }

    /**
     * Kiểm tra xem username đã tồn tại trong phòng chat chưa
     */
    public boolean isUsernameTaken(String username) {
        for (ClientHandler ch : clients) {
            if (ch.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Đăng ký client mới sau khi xác thực thành công
     */
    public void registerClient(ClientHandler ch) {
        clients.add(ch);
        notifyClientsUpdated();
        notifyStatsUpdated();
    }

    /**
     * Hủy đăng ký client khi ngắt kết nối
     */
    public void unregisterClient(ClientHandler ch) {
        if (clients.remove(ch)) {
            String name = ch.getUsername();
            if (name != null && !name.isEmpty()) {
                log("[RỜI PHÒNG] '" + name + "' đã ngắt kết nối.");
                broadcastNotification("Người dùng [" + name + "] đã rời phòng chat.");
                broadcastUserList();
            }
            notifyClientsUpdated();
            notifyStatsUpdated();
        }
    }

    /**
     * Xử lý gói tin tin nhắn văn bản từ client
     */
    public void handleTextMessage(ClientHandler sender, Packet packet) {
        totalMessages.incrementAndGet();
        notifyStatsUpdated();

        String recipient = packet.getRecipient();
        if (recipient == null || recipient.equalsIgnoreCase("ALL")) {
            // Chat chung - Broadcast tới tất cả client
            log("[CHAT GROUP] " + sender.getUsername() + ": " + packet.getContent());
            for (ClientHandler ch : clients) {
                ch.sendPacket(packet);
            }
        } else {
            // Chat riêng (Private Whisper)
            log("[CHAT RIÊNG] " + sender.getUsername() + " -> " + recipient + ": " + packet.getContent());
            boolean delivered = false;
            for (ClientHandler ch : clients) {
                if (ch.getUsername().equalsIgnoreCase(recipient)) {
                    ch.sendPacket(packet);
                    delivered = true;
                    break;
                }
            }
            // Gửi lại 1 bản sao xác nhận cho chính người gửi
            sender.sendPacket(packet);

            if (!delivered) {
                sender.sendPacket(Packet.createNotification("⚠️ Không tìm thấy người nhận: " + recipient));
            }
        }
    }

    /**
     * Xử lý gói tin truyền file từ client
     */
    public void handleFileMessage(ClientHandler sender, Packet packet) {
        totalFiles.incrementAndGet();
        notifyStatsUpdated();

        String recipient = packet.getRecipient();
        String sizeStr = Packet.formatFileSize(packet.getFileSize());
        if (recipient == null || recipient.equalsIgnoreCase("ALL")) {
            log("[CHUYỂN FILE] " + sender.getUsername() + " đã gửi tệp '" + packet.getFileName() + "' (" + sizeStr + ") cho tất cả mọi người.");
            for (ClientHandler ch : clients) {
                ch.sendPacket(packet);
            }
        } else {
            log("[CHUYỂN FILE RIÊNG] " + sender.getUsername() + " -> " + recipient + " (" + packet.getFileName() + ", " + sizeStr + ")");
            boolean delivered = false;
            for (ClientHandler ch : clients) {
                if (ch.getUsername().equalsIgnoreCase(recipient)) {
                    ch.sendPacket(packet);
                    delivered = true;
                    break;
                }
            }
            sender.sendPacket(packet);

            if (!delivered) {
                sender.sendPacket(Packet.createNotification("⚠️ Không tìm thấy người nhận để gửi file: " + recipient));
            }
        }
    }

    /**
     * Broadcast danh sách người dùng online cập nhật
     */
    public void broadcastUserList() {
        List<String> userList = new ArrayList<>();
        for (ClientHandler ch : clients) {
            if (!ch.getUsername().isEmpty()) {
                userList.add(ch.getUsername());
            }
        }
        Packet listPacket = Packet.createUserList(userList);
        for (ClientHandler ch : clients) {
            ch.sendPacket(listPacket);
        }
    }

    /**
     * Broadcast thông báo hệ thống tới mọi client
     */
    public void broadcastNotification(String message) {
        Packet notif = Packet.createNotification(message);
        for (ClientHandler ch : clients) {
            ch.sendPacket(notif);
        }
    }

    /**
     * Kick một người dùng ra khỏi phòng
     */
    public boolean kickUser(String username, String reason) {
        for (ClientHandler ch : clients) {
            if (ch.getUsername().equalsIgnoreCase(username)) {
                ch.sendPacket(Packet.createKick(username, reason));
                ch.sendPacket(Packet.createNotification("[QUẢN TRỊ VIÊN] Bạn đã bị mời ra khỏi phòng. Lý do: " + reason));
                ch.close();
                log("[KICK] Đã loại bỏ client '" + username + "' - Lý do: " + reason);
                broadcastNotification("Quản trị viên đã mời [" + username + "] rời khỏi phòng.");
                return true;
            }
        }
        return false;
    }

    public void log(String msg) {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String fullLog = "[" + time + "] " + msg;
        System.out.println(fullLog);
        if (listener != null) {
            listener.onLog(fullLog);
        }
    }

    private void notifyClientsUpdated() {
        if (listener != null) {
            listener.onClientsUpdated(new ArrayList<>(clients));
        }
    }

    private void notifyStatsUpdated() {
        if (listener != null) {
            listener.onStatsUpdated(clients.size(), totalMessages.get(), totalFiles.get());
        }
    }

    private void notifyStatus(boolean running, String msg) {
        if (listener != null) {
            listener.onStatusChanged(running, msg);
        }
    }

    public boolean isRunning() { return isRunning; }
    public int getPort() { return port; }
    public List<ClientHandler> getClients() { return clients; }
    public long getTotalMessages() { return totalMessages.get(); }
    public long getTotalFiles() { return totalFiles.get(); }
    public long getStartTimeMillis() { return startTimeMillis; }
}
