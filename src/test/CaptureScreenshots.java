package test;

import client.ClientGUI;
import common.Packet;
import server.ServerGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class CaptureScreenshots {
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        File outDir = new File("screenshots");
        if (!outDir.exists()) outDir.mkdirs();

        // 1. Capture ServerGUI
        SwingUtilities.invokeAndWait(() -> {
            try {
                ServerGUI server = new ServerGUI();
                server.pack();
                server.setSize(1080, 720);
                server.setLocationRelativeTo(null);
                server.setVisible(true);

                server.onLog("[15:00:00] [SERVER] Khởi động thành công trên cổng 8888...");
                server.onLog("[15:00:05] [KẾT NỐI] Client 'NguyenVanA' đăng nhập từ 127.0.0.1:51234");
                server.onLog("[15:00:10] [KẾT NỐI] Client 'TranThiB' đăng nhập từ 127.0.0.1:51235");
                server.onLog("[15:00:15] [CHAT GROUP] NguyenVanA: Chào bạn B!");
                server.onLog("[15:00:20] [FILE GROUP] TranThiB gửi tệp 'BaoCao_LTM.pdf' (1.4 MB)");
                server.onStatsUpdated(2, 14, 3);
                server.onStatusChanged(true, "Đang chạy");

                BufferedImage img = new BufferedImage(server.getWidth(), server.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();
                server.paint(g2);
                g2.dispose();

                ImageIO.write(img, "png", new File(outDir, "screenshot_server.png"));
                server.dispose();
                System.out.println("Captured screenshot_server.png");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 2. Capture ServerGUI
        SwingUtilities.invokeAndWait(() -> {
            try {
                ServerGUI server = new ServerGUI();
                server.pack();
                server.setSize(1080, 720);
                server.setLocationRelativeTo(null);
                server.setVisible(true);

                server.onLog("[15:00:00] [SERVER] Khởi động thành công trên cổng 8888...");
                server.onLog("[15:00:05] [KẾT NỐI] Client 'NguyenVanA' đăng nhập từ 127.0.0.1:51234");
                server.onLog("[15:00:10] [KẾT NỐI] Client 'TranThiB' đăng nhập từ 127.0.0.1:51235");
                server.onLog("[15:00:15] [CHAT GROUP] NguyenVanA: Chào bạn B!");
                server.onLog("[15:00:20] [FILE GROUP] TranThiB gửi tệp 'BaoCao_LTM.pdf' (1.4 MB)");
                server.onStatsUpdated(2, 14, 3);
                server.onStatusChanged(true, "Đang chạy");

                BufferedImage img = new BufferedImage(server.getWidth(), server.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();
                server.paint(g2);
                g2.dispose();

                ImageIO.write(img, "png", new File(outDir, "screenshot_server.png"));
                server.dispose();
                System.out.println("Captured screenshot_server.png");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 3. Capture ClientGUI Login & Chat
        SwingUtilities.invokeAndWait(() -> {
            try {
                ClientGUI client = new ClientGUI();
                client.pack();
                client.setSize(1000, 680);
                client.setLocationRelativeTo(null);
                client.setVisible(true);

                // Capture Login screen
                BufferedImage imgLogin = new BufferedImage(client.getWidth(), client.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = imgLogin.createGraphics();
                client.paint(g2);
                g2.dispose();
                ImageIO.write(imgLogin, "png", new File(outDir, "screenshot_client_login.png"));
                System.out.println("Captured screenshot_client_login.png");

                // Simulate Connected state
                client.onConnected("HoangGiaVIP");
                client.onUserListUpdated(java.util.Arrays.asList("HoangGiaVIP", "TranThiB", "NguyenVanA"));
                client.onTextMessageReceived(Packet.createTextMessage("NguyenVanA", "ALL", "Xin chào toàn thể phòng chat!"));
                client.onTextMessageReceived(Packet.createTextMessage("HoangGiaVIP", "ALL", "Giao diện Hoàng Gia Vàng Kim sang trọng tuyệt đẹp!"));
                client.onTextMessageReceived(Packet.createTextMessage("TranThiB", "HoangGiaVIP", "Chào bạn, gửi tài liệu môn Lập Trình Mạng cho mình nhé."));
                client.onFileReceived(Packet.createFileMessage("TranThiB", "ALL", "DeCuongOnTap_PTIT.docx", new byte[1024 * 350]));

                // Wait layout
                client.revalidate();
                client.repaint();

                BufferedImage imgChat = new BufferedImage(client.getWidth(), client.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g3 = imgChat.createGraphics();
                client.paint(g3);
                g3.dispose();
                ImageIO.write(imgChat, "png", new File(outDir, "screenshot_client_chat.png"));
                System.out.println("Captured screenshot_client_chat.png");

                client.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        System.out.println("ALL SCREENSHOTS CAPTURED SUCCESSFULLY!");
        System.exit(0);
    }
}
