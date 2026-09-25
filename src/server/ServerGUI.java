package server;

import common.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * Giao diện Quản trị viên Trung tâm Máy chủ (Server Control Center GUI).
 * Thiết kế phong cách Luxury Dark Dashboard, giám sát lưu lượng và điều khiển thời gian thực.
 */
public class ServerGUI extends JFrame implements ChatServer.ServerEventListener {
    private final ChatServer server;

    // UI Elements
    private JTextField txtPort;
    private JButton btnToggleServer;
    private JLabel lblStatusBadge;

    // KPI Metrics Labels
    private JLabel lblMetricClients;
    private JLabel lblMetricMessages;
    private JLabel lblMetricFiles;
    private JLabel lblMetricUptime;

    // Log Console
    private JTextArea txtLog;
    private JScrollPane scrollLog;

    // Clients Table
    private DefaultTableModel clientTableModel;
    private JTable clientTable;
    private JButton btnKick;

    // Broadcast input
    private JTextField txtBroadcast;
    private JButton btnBroadcast;

    // Timer for uptime
    private Timer uptimeTimer;

    public ServerGUI() {
        this.server = new ChatServer();
        this.server.setEventListener(this);

        initWindow();
        initUI();
        initUptimeTimer();
    }

    private void initWindow() {
        setTitle("PTIT TCP CHAT SERVER CONTROL CENTER - D24TXCNPM01-N");
        setSize(1080, 720);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (server.isRunning()) {
                    int opt = Theme.showConfirmDialog(
                            ServerGUI.this,
                            "Server đang hoạt động. Bạn có chắc muốn dừng Server và đóng không?",
                            "Xác nhận thoát",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );
                    if (opt == JOptionPane.YES_OPTION) {
                        server.stop();
                        dispose();
                    }
                } else {
                    dispose();
                }
            }
        });
        getContentPane().setBackground(Theme.BG_MAIN);
    }

    private void initUI() {
        setLayout(new BorderLayout(16, 16));
        JPanel rootPanel = new JPanel(new BorderLayout(14, 14));
        rootPanel.setBackground(Theme.BG_MAIN);
        rootPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. TOP HEADER + CONTROL BAR
        rootPanel.add(buildTopHeader(), BorderLayout.NORTH);

        // 2. CENTER: METRICS CARDS + MAIN CONTENT (LOGS & CLIENTS)
        JPanel centerPanel = new JPanel(new BorderLayout(14, 14));
        centerPanel.setOpaque(false);
        centerPanel.add(buildMetricsRow(), BorderLayout.NORTH);
        centerPanel.add(buildMainSplitPanel(), BorderLayout.CENTER);

        rootPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. BOTTOM: SYSTEM BROADCAST BAR
        rootPanel.add(buildBottomBroadcastBar(), BorderLayout.SOUTH);

        add(rootPanel, BorderLayout.CENTER);
    }

    private JPanel buildTopHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        // HÀNG 1: Tiêu đề máy chủ & Trạng thái hoạt động
        JPanel row1 = new JPanel(new BorderLayout(12, 0));
        row1.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 2, 2));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("PTIT TCP CHAT SERVER CONTROL CENTER");
        lblTitle.setFont(Theme.FONT_TITLE);
        lblTitle.setForeground(Theme.GOLD_LIGHT);

        JLabel lblSub = new JLabel("Hệ thống Máy chủ Chat Đa Luồng (Multithread) & Truyền Tệp Tin Nhị Phân TCP");
        lblSub.setFont(Theme.FONT_REGULAR);
        lblSub.setForeground(Theme.TEXT_MUTED);

        titlePanel.add(lblTitle);
        titlePanel.add(lblSub);

        lblStatusBadge = new JLabel("● ĐANG DỪNG (OFFLINE)");
        lblStatusBadge.setFont(Theme.FONT_BOLD);
        lblStatusBadge.setForeground(Theme.DANGER);
        lblStatusBadge.setHorizontalAlignment(SwingConstants.RIGHT);

        row1.add(titlePanel, BorderLayout.CENTER);
        row1.add(lblStatusBadge, BorderLayout.EAST);

        // HÀNG 2: Thanh công cụ cấu hình Cổng & Nút bấm
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        row2.setOpaque(false);

        JLabel lblPort = new JLabel("Cổng (Port):");
        lblPort.setFont(Theme.FONT_BOLD);
        lblPort.setForeground(Theme.TEXT_PRIMARY);

        txtPort = Theme.createTextField("8888", 8);
        txtPort.setText("8888");
        txtPort.setPreferredSize(new Dimension(80, 34));
        txtPort.setHorizontalAlignment(JTextField.CENTER);

        btnToggleServer = Theme.createButton("Bắt đầu Server", Theme.PRIMARY, Color.WHITE, 8);
        btnToggleServer.setPreferredSize(new Dimension(150, 34));
        btnToggleServer.addActionListener(e -> toggleServer());

        JButton btnClearLog = Theme.createButton("Xóa Log", Theme.BORDER_COLOR, Theme.TEXT_PRIMARY, 8);
        btnClearLog.setPreferredSize(new Dimension(95, 34));
        btnClearLog.addActionListener(e -> txtLog.setText(""));

        row2.add(lblPort);
        row2.add(txtPort);
        row2.add(btnToggleServer);
        row2.add(btnClearLog);

        headerPanel.add(row1);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(row2);
        return headerPanel;
    }

    private JPanel buildMetricsRow() {
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 14, 14));
        metricsPanel.setOpaque(false);

        lblMetricClients = new JLabel("0", SwingConstants.CENTER);
        lblMetricMessages = new JLabel("0", SwingConstants.CENTER);
        lblMetricFiles = new JLabel("0", SwingConstants.CENTER);
        lblMetricUptime = new JLabel("00:00:00", SwingConstants.CENTER);

        metricsPanel.add(createMetricCard("CLIENTS KẾT NỐI", lblMetricClients, Theme.GOLD_BRIGHT));
        metricsPanel.add(createMetricCard("TỔNG TIN NHẮN", lblMetricMessages, Theme.SUCCESS));
        metricsPanel.add(createMetricCard("TỆP ĐÃ TRUYỀN", lblMetricFiles, Theme.PRIMARY_HOVER));
        metricsPanel.add(createMetricCard("THỜI GIAN CHẠY", lblMetricUptime, Theme.GOLD_LIGHT));

        return metricsPanel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = Theme.createCard(12, Theme.BG_CARD, Theme.BORDER_COLOR);
        card.setLayout(new BorderLayout(4, 4));
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(Theme.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildMainSplitPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 14, 14));
        mainPanel.setOpaque(false);

        // LEFT: LIVE SERVER LOG CONSOLE
        JPanel logCard = Theme.createCard(12, Theme.BG_CARD, new Color(217, 119, 6, 100));
        logCard.setLayout(new BorderLayout(8, 8));
        logCard.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel lblLogTitle = new JLabel("NHẬT KÝ HOẠT ĐỘNG THỜI GIAN THỰC (SERVER LOG)");
        lblLogTitle.setFont(Theme.FONT_BOLD);
        lblLogTitle.setForeground(Theme.GOLD_LIGHT);

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(Theme.FONT_MONO);
        txtLog.setBackground(Theme.BG_INPUT);
        txtLog.setForeground(new Color(254, 243, 199));
        txtLog.setCaretColor(Theme.GOLD_BRIGHT);
        txtLog.setBorder(new EmptyBorder(8, 8, 8, 8));

        scrollLog = new JScrollPane(txtLog);
        Theme.applyModernScrollbar(scrollLog);

        logCard.add(lblLogTitle, BorderLayout.NORTH);
        logCard.add(scrollLog, BorderLayout.CENTER);

        // RIGHT: ACTIVE CLIENTS TABLE & MANAGEMENT
        JPanel clientsCard = Theme.createCard(12, Theme.BG_CARD, new Color(217, 119, 6, 100));
        clientsCard.setLayout(new BorderLayout(8, 8));
        clientsCard.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel headerClients = new JPanel(new BorderLayout());
        headerClients.setOpaque(false);
        JLabel lblClientTitle = new JLabel("DANH SÁCH CLIENTS ĐANG HOẠT ĐỘNG");
        lblClientTitle.setFont(Theme.FONT_BOLD);
        lblClientTitle.setForeground(Theme.GOLD_LIGHT);

        btnKick = Theme.createButton("Kick Client", Theme.DANGER, Color.WHITE, 6);
        btnKick.setFont(Theme.FONT_SMALL);
        btnKick.setPreferredSize(new Dimension(100, 28));
        btnKick.addActionListener(e -> kickSelectedClient());

        headerClients.add(lblClientTitle, BorderLayout.WEST);
        headerClients.add(btnKick, BorderLayout.EAST);

        // Table Model
        String[] columns = {"Tên Client", "Địa chỉ IP", "Cổng", "Giờ vào"};
        clientTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        clientTable = new JTable(clientTableModel);
        clientTable.setBackground(Theme.BG_INPUT);
        clientTable.setForeground(Theme.TEXT_PRIMARY);
        clientTable.setFont(Theme.FONT_REGULAR);
        clientTable.setRowHeight(28);
        clientTable.setSelectionBackground(Theme.PRIMARY);
        clientTable.setSelectionForeground(Color.WHITE);
        clientTable.setShowGrid(false);
        clientTable.getTableHeader().setBackground(Theme.BG_MAIN);
        clientTable.getTableHeader().setForeground(Theme.GOLD_LIGHT);
        clientTable.getTableHeader().setFont(Theme.FONT_BOLD);
        clientTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_GOLD));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 1; i < columns.length; i++) {
            clientTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollTable = new JScrollPane(clientTable);
        Theme.applyModernScrollbar(scrollTable);

        clientsCard.add(headerClients, BorderLayout.NORTH);
        clientsCard.add(scrollTable, BorderLayout.CENTER);

        mainPanel.add(logCard);
        mainPanel.add(clientsCard);
        return mainPanel;
    }

    private JPanel buildBottomBroadcastBar() {
        JPanel bar = Theme.createCard(10, Theme.BG_CARD, new Color(217, 119, 6, 100));
        bar.setLayout(new BorderLayout(10, 10));
        bar.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel lblAnnounce = new JLabel("Phát thông báo hệ thống:");
        lblAnnounce.setFont(Theme.FONT_BOLD);
        lblAnnounce.setForeground(Theme.GOLD_LIGHT);

        txtBroadcast = Theme.createTextField("Nhập thông báo gửi toàn bộ các client...", 8);

        btnBroadcast = Theme.createButton("Gửi thông báo", Theme.PRIMARY, Color.WHITE, 8);
        btnBroadcast.setPreferredSize(new Dimension(140, 36));
        btnBroadcast.addActionListener(e -> sendBroadcastMessage());

        txtBroadcast.addActionListener(e -> sendBroadcastMessage());

        bar.add(lblAnnounce, BorderLayout.WEST);
        bar.add(txtBroadcast, BorderLayout.CENTER);
        bar.add(btnBroadcast, BorderLayout.EAST);
        return bar;
    }

    private void toggleServer() {
        if (!server.isRunning()) {
            try {
                int port = Integer.parseInt(txtPort.getText().trim());
                if (port < 1024 || port > 65535) {
                    Theme.showMessageDialog(this, "Vui lòng nhập Port hợp lệ (1024 - 65535)", "Lỗi Port", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                server.start(port);
            } catch (NumberFormatException ex) {
                Theme.showMessageDialog(this, "Số cổng Port phải là số nguyên!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                Theme.showMessageDialog(this, "Không thể khởi động Server: " + ex.getMessage(), "Lỗi Socket", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            server.stop();
        }
    }

    private void sendBroadcastMessage() {
        if (!server.isRunning()) {
            Theme.showMessageDialog(this, "Server chưa hoạt động! Vui lòng khởi động server trước.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String msg = txtBroadcast.getText().trim();
        if (msg.isEmpty()) return;

        server.broadcastNotification("[QUẢN TRỊ VIÊN]: " + msg);
        server.log("[THÔNG BÁO ADMIN] " + msg);
        txtBroadcast.setText("");
    }

    private void kickSelectedClient() {
        int row = clientTable.getSelectedRow();
        if (row < 0) {
            Theme.showMessageDialog(this, "Vui lòng chọn một client trong bảng để kick!", "Chọn Client", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String username = (String) clientTableModel.getValueAt(row, 0);
        String reason = Theme.showInputDialog(this, "Nhập lý do loại bỏ '" + username + "':", "Kick Client", JOptionPane.QUESTION_MESSAGE);
        if (reason != null && !reason.trim().isEmpty()) {
            server.kickUser(username, reason.trim());
        }
    }

    private void initUptimeTimer() {
        uptimeTimer = new Timer(1000, e -> {
            if (server.isRunning() && server.getStartTimeMillis() > 0) {
                long diff = (System.currentTimeMillis() - server.getStartTimeMillis()) / 1000;
                long h = diff / 3600;
                long m = (diff % 3600) / 60;
                long s = diff % 60;
                lblMetricUptime.setText(String.format("%02d:%02d:%02d", h, m, s));
            } else {
                lblMetricUptime.setText("00:00:00");
            }
        });
        uptimeTimer.start();
    }

    // --- ServerEventListener Callbacks ---
    @Override
    public void onLog(String logMessage) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(logMessage + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }

    @Override
    public void onClientsUpdated(List<ClientHandler> activeClients) {
        SwingUtilities.invokeLater(() -> {
            clientTableModel.setRowCount(0);
            for (ClientHandler ch : activeClients) {
                clientTableModel.addRow(new Object[]{
                        ch.getUsername(),
                        ch.getClientIp(),
                        ch.getClientPort(),
                        ch.getConnectTime()
                });
            }
            lblMetricClients.setText(String.valueOf(activeClients.size()));
        });
    }

    @Override
    public void onStatsUpdated(int activeCount, long totalMsg, long totalFiles) {
        SwingUtilities.invokeLater(() -> {
            lblMetricClients.setText(String.valueOf(activeCount));
            lblMetricMessages.setText(String.valueOf(totalMsg));
            lblMetricFiles.setText(String.valueOf(totalFiles));
        });
    }

    @Override
    public void onStatusChanged(boolean running, String message) {
        SwingUtilities.invokeLater(() -> {
            if (running) {
                lblStatusBadge.setText("● ĐANG CHẠY (PORT " + server.getPort() + ")");
                lblStatusBadge.setForeground(Theme.SUCCESS);
                btnToggleServer.setText("Dừng Server");
                btnToggleServer.setBackground(Theme.DANGER);
                txtPort.setEnabled(false);
            } else {
                lblStatusBadge.setText("● ĐANG DỪNG (OFFLINE)");
                lblStatusBadge.setForeground(Theme.DANGER);
                btnToggleServer.setText("Bắt đầu Server");
                btnToggleServer.setBackground(Theme.PRIMARY);
                txtPort.setEnabled(true);
            }
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            ServerGUI gui = new ServerGUI();
            gui.setVisible(true);
        });
    }
}
