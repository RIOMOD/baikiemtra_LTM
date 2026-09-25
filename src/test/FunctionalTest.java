package test;

import client.ChatClient;
import common.Packet;
import server.ChatServer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FunctionalTest {
    public static void main(String[] args) throws Exception {
        System.out.println("=== BẮT ĐẦU KIỂM THỬ TÍCH HỢP TỰ ĐỘNG (TCP CHAT + FILE) ===");

        int testPort = 9999;
        ChatServer server = new ChatServer();
        server.start(testPort);
        System.out.println("1. Server đã khởi động trên cổng " + testPort);

        CountDownLatch client1Connected = new CountDownLatch(1);
        CountDownLatch client2Connected = new CountDownLatch(1);
        CountDownLatch textReceivedLatch = new CountDownLatch(1);
        CountDownLatch fileReceivedLatch = new CountDownLatch(1);

        ChatClient client1 = new ChatClient();
        client1.setEventListener(new ChatClient.ClientEventListener() {
            @Override public void onConnected(String username) { client1Connected.countDown(); }
            @Override public void onConnectionFailed(String reason) { System.err.println("Client 1 thất bại: " + reason); }
            @Override public void onDisconnected(String reason) {}
            @Override public void onTextMessageReceived(Packet packet) {}
            @Override public void onFileReceived(Packet packet) {}
            @Override public void onUserListUpdated(java.util.List<String> userList) {}
            @Override public void onNotification(String message) {}
            @Override public void onKicked(String reason) {}
        });

        ChatClient client2 = new ChatClient();
        final String[] receivedText = {""};
        final String[] receivedFileName = {""};
        final int[] receivedFileSize = {0};

        client2.setEventListener(new ChatClient.ClientEventListener() {
            @Override public void onConnected(String username) { client2Connected.countDown(); }
            @Override public void onConnectionFailed(String reason) { System.err.println("Client 2 thất bại: " + reason); }
            @Override public void onDisconnected(String reason) {}
            @Override public void onTextMessageReceived(Packet packet) {
                receivedText[0] = packet.getContent();
                textReceivedLatch.countDown();
            }
            @Override public void onFileReceived(Packet packet) {
                receivedFileName[0] = packet.getFileName();
                receivedFileSize[0] = packet.getFileData() != null ? packet.getFileData().length : 0;
                fileReceivedLatch.countDown();
            }
            @Override public void onUserListUpdated(java.util.List<String> userList) {}
            @Override public void onNotification(String message) {}
            @Override public void onKicked(String reason) {}
        });

        client1.connect("127.0.0.1", testPort, "Alice");
        client2.connect("127.0.0.1", testPort, "Bob");

        if (!client1Connected.await(3, TimeUnit.SECONDS) || !client2Connected.await(3, TimeUnit.SECONDS)) {
            throw new RuntimeException("Lỗi: Client kết nối quá hạn!");
        }
        System.out.println("2. Cả 2 client (Alice và Bob) đã kết nối thành công!");

        // 3. Test gửi tin nhắn
        client1.sendMessage("ALL", "Xin chào Bob từ Alice!");
        if (!textReceivedLatch.await(3, TimeUnit.SECONDS)) {
            throw new RuntimeException("Lỗi: Không nhận được tin nhắn!");
        }
        System.out.println("3. Bob đã nhận được tin nhắn: '" + receivedText[0] + "' -> ĐẠT!");

        // 4. Test gửi file
        File testFile = new File("test_transfer.txt");
        try (FileOutputStream fos = new FileOutputStream(testFile)) {
            fos.write("PTIT TEST FILE CHAT TCP 2026".getBytes("UTF-8"));
        }

        CountDownLatch fileSendLatch = new CountDownLatch(1);
        client1.sendFile("ALL", testFile, () -> fileSendLatch.countDown(), err -> System.err.println(err));

        if (!fileSendLatch.await(3, TimeUnit.SECONDS) || !fileReceivedLatch.await(3, TimeUnit.SECONDS)) {
            throw new RuntimeException("Lỗi: Không nhận được file!");
        }
        System.out.println("4. Bob đã nhận được tệp tin: '" + receivedFileName[0] + "' (Size: " + receivedFileSize[0] + " bytes) -> ĐẠT!");

        // Dọn dẹp
        testFile.delete();
        client1.disconnect();
        client2.disconnect();
        server.stop();
        System.out.println("=== TẤT CẢ CÁC BƯỚC KIỂM THỬ ĐỀU THÀNH CÔNG RỰC RỠ 100%! ===");
        System.exit(0);
    }
}
