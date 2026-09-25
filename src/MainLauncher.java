import client.ClientGUI;
import common.Theme;
import server.ServerGUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Trình khởi chạy Trung tâm (All-In-One Launcher Portal).
 * Hỗ trợ khởi động Server và nhiều Client thử nghiệm tức thì với giao diện chuẩn tiếng Việt.
 */
public class MainLauncher extends JFrame {

    public MainLauncher() {
        setTitle("PTIT TCP CHAT & FILE - BỘ ĐIỀU KHIỂN TRUNG TÂM");
        setSize(580, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(Theme.BG_MAIN);
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("BÀI KIỂM TRA LẬP TRÌNH MẠNG", SwingConstants.CENTER);
        lblTitle.setFont(Theme.FONT_TITLE);
        lblTitle.setForeground(Theme.GOLD_LIGHT);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Ứng Dụng Chat & Truyền Tệp Đa Luồng TCP (Java Swing)", SwingConstants.CENTER);
        lblSub.setFont(Theme.FONT_REGULAR);
        lblSub.setForeground(Theme.TEXT_PRIMARY);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblCredit = new JLabel("Học Viện Công Nghệ Bưu Chính Viễn Thông (PTIT)", SwingConstants.CENTER);
        lblCredit.setFont(Theme.FONT_SMALL);
        lblCredit.setForeground(Theme.GOLD_BRIGHT);
        lblCredit.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(lblTitle);
        header.add(Box.createVerticalStrut(6));
        header.add(lblSub);
        header.add(Box.createVerticalStrut(4));
        header.add(lblCredit);

        // Buttons Panel inside a Luxury Gold Card
        JPanel centerCard = Theme.createCard(16, Theme.BG_CARD, new Color(217, 119, 6, 120));
        centerCard.setLayout(new BorderLayout());
        centerCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 12, 12));
        centerPanel.setOpaque(false);

        JButton btnOpenServer = Theme.createButton("1. Mở Server Control Center", Theme.PRIMARY, Color.WHITE, 10);
        btnOpenServer.setFont(Theme.FONT_BOLD);
        btnOpenServer.addActionListener(e -> launchServer());

        JButton btnOpenClient = Theme.createButton("2. Mở Cửa Sổ Client Mới", new Color(180, 83, 9), Color.WHITE, 10);
        btnOpenClient.setFont(Theme.FONT_BOLD);
        btnOpenClient.addActionListener(e -> launchClient());

        JButton btnAutoDemo = Theme.createButton("3. Khởi Chạy Bộ Thử Nghiệm Nhanh (Server + 2 Clients)", new Color(20, 83, 45), Theme.GOLD_LIGHT, 10);
        btnAutoDemo.setFont(Theme.FONT_BOLD);
        btnAutoDemo.addActionListener(e -> launchAutoDemo());

        JButton btnExit = Theme.createButton("Thoát", Theme.BORDER_COLOR, Theme.TEXT_PRIMARY, 10);
        btnExit.addActionListener(e -> dispose());

        centerPanel.add(btnOpenServer);
        centerPanel.add(btnOpenClient);
        centerPanel.add(btnAutoDemo);
        centerPanel.add(btnExit);

        centerCard.add(centerPanel, BorderLayout.CENTER);

        // Footer
        JLabel lblFooter = new JLabel("© 2026 PTIT - SVTH: Nguyễn Công Trứ (N24DTCN088) - GVHD: Thầy Văn Tông Thành", SwingConstants.CENTER);
        lblFooter.setFont(Theme.FONT_SMALL);
        lblFooter.setForeground(Theme.TEXT_HINT);

        root.add(header, BorderLayout.NORTH);
        root.add(centerCard, BorderLayout.CENTER);
        root.add(lblFooter, BorderLayout.SOUTH);

        add(root);
    }

    private void launchServer() {
        SwingUtilities.invokeLater(() -> {
            ServerGUI serverGUI = new ServerGUI();
            serverGUI.setVisible(true);
        });
    }

    private void launchClient() {
        SwingUtilities.invokeLater(() -> {
            ClientGUI clientGUI = new ClientGUI();
            clientGUI.setVisible(true);
        });
    }

    private void launchAutoDemo() {
        SwingUtilities.invokeLater(() -> {
            // 1. Mở Server
            ServerGUI server = new ServerGUI();
            server.setLocation(80, 80);
            server.setVisible(true);

            // 2. Mở Client 1
            ClientGUI client1 = new ClientGUI();
            client1.setLocation(620, 80);
            client1.setVisible(true);

            // 3. Mở Client 2
            ClientGUI client2 = new ClientGUI();
            client2.setLocation(720, 180);
            client2.setVisible(true);
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            MainLauncher launcher = new MainLauncher();
            launcher.setVisible(true);
        });
    }
}
