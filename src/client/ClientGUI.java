package client;

import common.Packet;
import common.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Giao diện Ứng dụng Chat & Truyền Tệp phía Client (Modern Slate & Indigo).
 * Tách biệt hoàn toàn luồng tin nhắn Phòng Chung (Broadcast) và Trò Chuyện Riêng Tư (Whisper).
 */
public class ClientGUI extends JFrame implements ChatClient.ClientEventListener {
    private final ChatClient client;
    private final CardLayout cardLayout;
    private final JPanel rootCardPanel;

    // Quản lý Hội thoại Độc lập (Conversation Management)
    // Key: "ALL" cho phòng chung, hoặc Tên User cho chat riêng
    private final Map<String, List<Packet>> conversationHistory = new ConcurrentHashMap<>();
    private final Map<String, Integer> unreadCounts = new ConcurrentHashMap<>();
    private String activeTarget = "ALL";
    private final List<String> currentOnlineUsers = new ArrayList<>();
    private boolean isUpdatingCombo = false;
    private boolean isUpdatingSidebar = false;

    // Login View Components
    private JTextField txtHost;
    private JTextField txtPort;
    private JTextField txtUsername;
    private JButton btnConnect;
    private JLabel lblLoginStatus;

    // Main Chat View Components
    private JLabel lblMyAvatar;
    private JLabel lblMyName;
    private DefaultListModel<String> userListModel;
    private JList<String> userJList;
    private JComboBox<String> cbRecipient;
    private JLabel lblChatHeader;

    private JPanel chatContainerPanel;
    private JScrollPane scrollChat;
    private JTextField txtInput;
    private JButton btnSend;
    private JButton btnSendFile;
    private JButton btnDisconnect;

    private final Color[] AVATAR_COLORS = {
            new Color(217, 119, 6), new Color(180, 83, 9), new Color(16, 185, 129),
            new Color(14, 165, 233), new Color(245, 158, 11), new Color(168, 85, 247)
    };

    public ClientGUI() {
        this.client = new ChatClient();
        this.client.setEventListener(this);

        this.cardLayout = new CardLayout();
        this.rootCardPanel = new JPanel(cardLayout);

        initWindow();
        initUI();
    }

    private void initWindow() {
        setTitle("PTIT TCP CHAT & FILE TRANSFER - CLIENT");
        setSize(1000, 680);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });
    }

    private void initUI() {
        rootCardPanel.add(buildLoginPanel(), "LOGIN");
        rootCardPanel.add(buildChatPanel(), "CHAT");
        cardLayout.show(rootCardPanel, "LOGIN");

        add(rootCardPanel);
    }

    // =========================================================================
    // 1. MÀN HÌNH ĐĂNG NHẬP (LOGIN VIEW)
    // =========================================================================
    private JPanel buildLoginPanel() {
        JPanel bgPanel = new JPanel(new GridBagLayout());
        bgPanel.setBackground(Theme.BG_MAIN);

        JPanel card = Theme.createCard(16, Theme.BG_CARD, Theme.BORDER_GOLD);
        card.setPreferredSize(new Dimension(420, 480));
        card.setLayout(new BorderLayout(16, 16));
        card.setBorder(new EmptyBorder(28, 32, 28, 32));

        // Top Header
        JPanel topHeader = new JPanel();
        topHeader.setLayout(new BoxLayout(topHeader, BoxLayout.Y_AXIS));
        topHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("PTIT TCP CHAT", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Theme.GOLD_LIGHT);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Đăng nhập để vào phòng chat & chia sẻ tệp", SwingConstants.CENTER);
        lblSubtitle.setFont(Theme.FONT_REGULAR);
        lblSubtitle.setForeground(Theme.TEXT_MUTED);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        topHeader.add(Box.createVerticalStrut(10));
        topHeader.add(lblTitle);
        topHeader.add(Box.createVerticalStrut(6));
        topHeader.add(lblSubtitle);

        // Center Form
        JPanel formPanel = new JPanel(new GridLayout(6, 1, 6, 6));
        formPanel.setOpaque(false);

        JLabel lblHostTitle = new JLabel("Địa chỉ Máy chủ (IP / Host):");
        lblHostTitle.setFont(Theme.FONT_BOLD);
        lblHostTitle.setForeground(Theme.TEXT_PRIMARY);

        txtHost = Theme.createTextField("127.0.0.1", 8);
        txtHost.setText("127.0.0.1");

        JLabel lblPortTitle = new JLabel("Cổng kết nối (Port):");
        lblPortTitle.setFont(Theme.FONT_BOLD);
        lblPortTitle.setForeground(Theme.TEXT_PRIMARY);

        txtPort = Theme.createTextField("8888", 8);
        txtPort.setText("8888");

        JLabel lblUserTitle = new JLabel("Tên hiển thị (Nickname):");
        lblUserTitle.setFont(Theme.FONT_BOLD);
        lblUserTitle.setForeground(Theme.TEXT_PRIMARY);

        txtUsername = Theme.createTextField("Nhập tên của bạn...", 8);

        formPanel.add(lblHostTitle);
        formPanel.add(txtHost);
        formPanel.add(lblPortTitle);
        formPanel.add(txtPort);
        formPanel.add(lblUserTitle);
        formPanel.add(txtUsername);

        // Bottom Actions
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);

        lblLoginStatus = new JLabel(" ", SwingConstants.CENTER);
        lblLoginStatus.setFont(Theme.FONT_SMALL);
        lblLoginStatus.setForeground(Theme.DANGER);
        lblLoginStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnConnect = Theme.createButton("Tham Gia Phòng Chat", Theme.PRIMARY, Color.WHITE, 10);
        btnConnect.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnConnect.setPreferredSize(new Dimension(320, 44));
        btnConnect.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConnect.addActionListener(e -> performLogin());

        // Press Enter to login
        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) performLogin();
            }
        };
        txtHost.addKeyListener(enterKey);
        txtPort.addKeyListener(enterKey);
        txtUsername.addKeyListener(enterKey);

        bottomPanel.add(lblLoginStatus);
        bottomPanel.add(Box.createVerticalStrut(8));
        bottomPanel.add(btnConnect);

        card.add(topHeader, BorderLayout.NORTH);
        card.add(formPanel, BorderLayout.CENTER);
        card.add(bottomPanel, BorderLayout.SOUTH);

        bgPanel.add(card);
        return bgPanel;
    }

    private void performLogin() {
        String host = txtHost.getText().trim();
        String portStr = txtPort.getText().trim();
        String user = txtUsername.getText().trim();

        if (host.isEmpty() || portStr.isEmpty() || user.isEmpty()) {
            lblLoginStatus.setText("Vui lòng điền đầy đủ các thông tin!");
            return;
        }

        try {
            int port = Integer.parseInt(portStr);
            lblLoginStatus.setText("Đang kết nối tới " + host + ":" + port + "...");
            lblLoginStatus.setForeground(Theme.ACCENT);
            btnConnect.setEnabled(false);

            client.connect(host, port, user);
        } catch (NumberFormatException e) {
            lblLoginStatus.setText("Số Port phải là số nguyên!");
            lblLoginStatus.setForeground(Theme.DANGER);
        }
    }

    // =========================================================================
    // 2. MÀN HÌNH CHAT CHÍNH (CHAT ROOM VIEW)
    // =========================================================================
    private JPanel buildChatPanel() {
        JPanel mainChat = new JPanel(new BorderLayout());
        mainChat.setBackground(Theme.BG_MAIN);

        mainChat.add(buildSidebar(), BorderLayout.WEST);
        mainChat.add(buildChatWorkspace(), BorderLayout.CENTER);

        return mainChat;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(8, 8));
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBackground(Theme.BG_CARD);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_COLOR));

        // Top My Profile
        JPanel profilePanel = new JPanel(new BorderLayout(10, 10));
        profilePanel.setOpaque(false);
        profilePanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        lblMyAvatar = new JLabel();
        lblMyAvatar.setIcon(Theme.createAvatarIcon("?", 42, Theme.PRIMARY));

        JPanel nameBox = new JPanel(new GridLayout(2, 1));
        nameBox.setOpaque(false);
        lblMyName = new JLabel("Người dùng");
        lblMyName.setFont(Theme.FONT_BOLD);
        lblMyName.setForeground(Theme.TEXT_PRIMARY);

        JLabel lblMyStatus = new JLabel("● Đang trực tuyến");
        lblMyStatus.setFont(Theme.FONT_SMALL);
        lblMyStatus.setForeground(Theme.SUCCESS);

        nameBox.add(lblMyName);
        nameBox.add(lblMyStatus);

        profilePanel.add(lblMyAvatar, BorderLayout.WEST);
        profilePanel.add(nameBox, BorderLayout.CENTER);

        // Center: Online list
        JPanel listContainer = new JPanel(new BorderLayout(6, 6));
        listContainer.setOpaque(false);
        listContainer.setBorder(new EmptyBorder(4, 12, 10, 12));

        JLabel lblListHeader = new JLabel("DANH SÁCH HỘI THOẠI");
        lblListHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblListHeader.setForeground(Theme.TEXT_MUTED);

        userListModel = new DefaultListModel<>();
        userJList = new JList<>(userListModel);
        userJList.setBackground(Theme.BG_CARD);
        userJList.setForeground(Theme.TEXT_PRIMARY);
        userJList.setFont(Theme.FONT_REGULAR);
        userJList.setSelectionBackground(new Color(180, 83, 9, 100));
        userJList.setSelectionForeground(Theme.GOLD_LIGHT);
        userJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userJList.setFixedCellHeight(44);
        userJList.setCellRenderer(new UserListCellRenderer());

        // Bắt sự kiện chuột nhấn xuống lập tức (Instant Click Detection)
        userJList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int index = userJList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    java.awt.Rectangle bounds = userJList.getCellBounds(index, index);
                    if (bounds != null && bounds.contains(e.getPoint())) {
                        selectConversationByIndex(index);
                    }
                }
            }
        });

        // Bắt sự kiện khi di chuyển bằng phím mũi tên
        userJList.addListSelectionListener(e -> {
            if (isUpdatingSidebar || e.getValueIsAdjusting()) return;
            int idx = userJList.getSelectedIndex();
            if (idx >= 0) {
                selectConversationByIndex(idx);
            }
        });

        JScrollPane scrollList = new JScrollPane(userJList);
        scrollList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        Theme.applyModernScrollbar(scrollList);

        listContainer.add(lblListHeader, BorderLayout.NORTH);
        listContainer.add(scrollList, BorderLayout.CENTER);

        // Bottom Disconnect Button
        JPanel bottomSidebar = new JPanel(new BorderLayout());
        bottomSidebar.setOpaque(false);
        bottomSidebar.setBorder(new EmptyBorder(10, 14, 14, 14));

        btnDisconnect = Theme.createButton("Rời phòng chat", Theme.DANGER, Color.WHITE, 8);
        btnDisconnect.setPreferredSize(new Dimension(0, 36));
        btnDisconnect.addActionListener(e -> client.disconnect());

        bottomSidebar.add(btnDisconnect, BorderLayout.CENTER);

        sidebar.add(profilePanel, BorderLayout.NORTH);
        sidebar.add(listContainer, BorderLayout.CENTER);
        sidebar.add(bottomSidebar, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel buildChatWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(Theme.BG_MAIN);

        // 1. TOP HEADER BAR
        JPanel headerBar = new JPanel(new BorderLayout(10, 10));
        headerBar.setBackground(Theme.BG_CARD);
        headerBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR),
                new EmptyBorder(12, 16, 12, 16)
        ));

        lblChatHeader = new JLabel("PHÒNG CHAT CHUNG (TẤT CẢ THÀNH VIÊN)");
        lblChatHeader.setFont(Theme.FONT_BOLD);
        lblChatHeader.setForeground(Theme.GOLD_LIGHT);

        JPanel recipientBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        recipientBox.setOpaque(false);

        JLabel lblTo = new JLabel("Gửi tới:");
        lblTo.setFont(Theme.FONT_BOLD);
        lblTo.setForeground(Theme.TEXT_PRIMARY);

        cbRecipient = new JComboBox<>();
        cbRecipient.addItem("Tất cả mọi người");
        cbRecipient.setFont(Theme.FONT_BOLD);
        cbRecipient.setPreferredSize(new Dimension(200, 34));
        cbRecipient.setBackground(Theme.BG_INPUT);
        cbRecipient.setForeground(Theme.TEXT_PRIMARY);
        cbRecipient.setFocusable(false);
        cbRecipient.setBorder(BorderFactory.createLineBorder(Theme.BORDER_GOLD, 1, true));

        // Tùy biến màu sắc ComboBox để chữ vàng/trắng nổi bật trên nền tối hoàng gia
        cbRecipient.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setFont(Theme.FONT_BOLD);
                lbl.setBorder(new EmptyBorder(8, 12, 8, 12));
                if (isSelected) {
                    lbl.setBackground(Theme.PRIMARY); // Vàng Hoàng Gia
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(Theme.BG_INPUT);  // Dark Obsidian Slate
                    lbl.setForeground(Theme.TEXT_PRIMARY); // Chữ trắng ngà rõ nét
                }
                lbl.setOpaque(true);
                return lbl;
            }
        });

        cbRecipient.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton("▼");
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                btn.setForeground(Theme.GOLD_LIGHT);
                btn.setBackground(Theme.BG_INPUT);
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setOpaque(false);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.BG_INPUT);
                g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                g2.dispose();
            }
        });

        cbRecipient.addActionListener(e -> {
            if (isUpdatingCombo) return;
            String sel = (String) cbRecipient.getSelectedItem();
            if (sel != null) {
                if (sel.equals("Tất cả mọi người")) {
                    switchConversation("ALL");
                } else {
                    switchConversation(sel);
                }
            }
        });

        recipientBox.add(lblTo);
        recipientBox.add(cbRecipient);

        headerBar.add(lblChatHeader, BorderLayout.CENTER);
        headerBar.add(recipientBox, BorderLayout.EAST);

        // 2. CENTER: SCROLLABLE CHAT TIMELINE
        chatContainerPanel = new JPanel();
        chatContainerPanel.setLayout(new BoxLayout(chatContainerPanel, BoxLayout.Y_AXIS));
        chatContainerPanel.setBackground(Theme.BG_MAIN);
        chatContainerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel chatScrollWrapper = new JPanel(new BorderLayout());
        chatScrollWrapper.setBackground(Theme.BG_MAIN);
        chatScrollWrapper.add(chatContainerPanel, BorderLayout.NORTH);

        scrollChat = new JScrollPane(chatScrollWrapper);
        scrollChat.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollChat.getViewport().setBackground(Theme.BG_MAIN);
        scrollChat.setBorder(BorderFactory.createEmptyBorder());
        Theme.applyModernScrollbar(scrollChat);

        // 3. BOTTOM: COMPOSER (EMOTICONS, FILE, TEXT INPUT, SEND BUTTON)
        JPanel composerPanel = new JPanel(new BorderLayout(8, 8));
        composerPanel.setBackground(Theme.BG_CARD);
        composerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR),
                new EmptyBorder(10, 14, 10, 14)
        ));

        // Quick Emoticons (Không dùng font Emoji màu để tránh lỗi ô vuông [])
        JPanel emoticonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        emoticonBar.setOpaque(false);
        String[] emoticons = {":)", ":D", "<3", "(y)", "^o^", "-_-", ":P", "(ok)"};
        for (String emo : emoticons) {
            JButton btnEmo = new JButton(emo);
            btnEmo.setFont(new Font("Consolas", Font.BOLD, 12));
            btnEmo.setForeground(Theme.ACCENT);
            btnEmo.setOpaque(false);
            btnEmo.setContentAreaFilled(false);
            btnEmo.setBorderPainted(false);
            btnEmo.setFocusPainted(false);
            btnEmo.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnEmo.addActionListener(e -> {
                txtInput.setText(txtInput.getText() + " " + emo + " ");
                txtInput.requestFocus();
            });
            emoticonBar.add(btnEmo);
        }

        // Input Line
        JPanel inputLine = new JPanel(new BorderLayout(8, 0));
        inputLine.setOpaque(false);

        btnSendFile = Theme.createButton("Gửi File", Theme.BORDER_COLOR, Theme.TEXT_PRIMARY, 8);
        btnSendFile.setPreferredSize(new Dimension(100, 38));
        btnSendFile.addActionListener(e -> chooseAndSendFile());

        txtInput = Theme.createTextField("Nhập tin nhắn... (Nhấn Enter để gửi)", 8);
        txtInput.setPreferredSize(new Dimension(0, 38));
        txtInput.addActionListener(e -> sendTextMessage());

        btnSend = Theme.createButton("Gửi", Theme.PRIMARY, Color.WHITE, 8);
        btnSend.setPreferredSize(new Dimension(80, 38));
        btnSend.addActionListener(e -> sendTextMessage());

        inputLine.add(btnSendFile, BorderLayout.WEST);
        inputLine.add(txtInput, BorderLayout.CENTER);
        inputLine.add(btnSend, BorderLayout.EAST);

        composerPanel.add(emoticonBar, BorderLayout.NORTH);
        composerPanel.add(inputLine, BorderLayout.CENTER);

        workspace.add(headerBar, BorderLayout.NORTH);
        workspace.add(scrollChat, BorderLayout.CENTER);
        workspace.add(composerPanel, BorderLayout.SOUTH);

        return workspace;
    }

    // =========================================================================
    // LOGIC CHUYỂN ĐỔI VÀ QUẢN LÝ HỘI THOẠI (CONVERSATION SWITCHER)
    // =========================================================================
    private synchronized void switchConversation(String newTarget) {
        if (newTarget == null || newTarget.trim().isEmpty()) {
            newTarget = "ALL";
        }
        this.activeTarget = newTarget;
        unreadCounts.put(newTarget.toUpperCase(), 0);
        unreadCounts.put(newTarget, 0);

        // Cập nhật tiêu đề và combobox
        isUpdatingCombo = true;
        if (activeTarget.equalsIgnoreCase("ALL")) {
            lblChatHeader.setText("PHÒNG CHAT CHUNG (TẤT CẢ THÀNH VIÊN)");
            lblChatHeader.setForeground(Theme.GOLD_LIGHT);
            cbRecipient.setSelectedItem("Tất cả mọi người");
        } else {
            lblChatHeader.setText("ĐANG CHAT RIÊNG VỚI: " + activeTarget);
            lblChatHeader.setForeground(Theme.GOLD_BRIGHT);
            cbRecipient.setSelectedItem(activeTarget);
        }
        isUpdatingCombo = false;

        // Tải lại toàn bộ tin nhắn thuộc hội thoại này
        refreshChatTimeline();
        updateSidebarList();
    }

    private synchronized void refreshChatTimeline() {
        chatContainerPanel.removeAll();
        List<Packet> history = getHistory(activeTarget);
        for (Packet p : history) {
            if (p.getType() == common.MessageType.TEXT_MSG) {
                renderTextMessageBubble(p);
            } else if (p.getType() == common.MessageType.FILE_MSG) {
                renderFileMessageCard(p);
            } else if (p.getType() == common.MessageType.NOTIFICATION) {
                renderNotificationPill(p.getContent());
            }
        }
        chatContainerPanel.revalidate();
        chatContainerPanel.repaint();
        scrollToBottom();
    }

    private synchronized List<Packet> getHistory(String target) {
        return conversationHistory.computeIfAbsent(target.toUpperCase(), k -> new ArrayList<>());
    }

    private void selectConversationByIndex(int index) {
        if (index == 0) {
            switchConversation("ALL");
        } else if (index - 1 < currentOnlineUsers.size()) {
            switchConversation(currentOnlineUsers.get(index - 1));
        }
    }

    private void updateSidebarList() {
        SwingUtilities.invokeLater(() -> {
            isUpdatingSidebar = true;
            try {
                int targetIndex = 0;
                if (!activeTarget.equalsIgnoreCase("ALL")) {
                    for (int i = 0; i < currentOnlineUsers.size(); i++) {
                        if (currentOnlineUsers.get(i).equalsIgnoreCase(activeTarget)) {
                            targetIndex = i + 1;
                            break;
                        }
                    }
                }

                // Xóa số tin chưa đọc của hội thoại hiện đang mở
                if (activeTarget.equalsIgnoreCase("ALL")) {
                    unreadCounts.put("ALL", 0);
                } else {
                    unreadCounts.put(activeTarget.toUpperCase(), 0);
                    unreadCounts.put(activeTarget, 0);
                }

                // 1. Mục phòng chung
                int unreadAll = activeTarget.equalsIgnoreCase("ALL") ? 0 : unreadCounts.getOrDefault("ALL", 0);
                String allLabel = "Phòng Chung (Tất cả)" + (unreadAll > 0 ? " (" + unreadAll + " tin mới)" : "");
                if (userListModel.isEmpty()) {
                    userListModel.addElement(allLabel);
                } else {
                    userListModel.set(0, allLabel);
                }

                // 2. Đồng bộ các thành viên online (cập nhật in-place, không clear để giữ tương tác chuột)
                int desiredSize = currentOnlineUsers.size() + 1;
                while (userListModel.size() > desiredSize) {
                    userListModel.remove(userListModel.size() - 1);
                }

                for (int i = 0; i < currentOnlineUsers.size(); i++) {
                    String u = currentOnlineUsers.get(i);
                    int unread = u.equalsIgnoreCase(activeTarget) ? 0 : unreadCounts.getOrDefault(u.toUpperCase(), 0);
                    String label = u + (unread > 0 ? " (" + unread + " tin mới)" : "");
                    int modelIdx = i + 1;
                    if (modelIdx < userListModel.size()) {
                        userListModel.set(modelIdx, label);
                    } else {
                        userListModel.addElement(label);
                    }
                }

                if (targetIndex >= 0 && targetIndex < userListModel.size()) {
                    userJList.setSelectedIndex(targetIndex);
                }
            } finally {
                isUpdatingSidebar = false;
            }
        });
    }

    private void sendTextMessage() {
        String text = txtInput.getText().trim();
        if (text.isEmpty()) return;

        client.sendMessage(activeTarget, text);
        txtInput.setText("");
    }

    private void chooseAndSendFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn tệp tin muốn gửi tới: " + (activeTarget.equals("ALL") ? "Tất cả mọi người" : activeTarget));
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            if (selectedFile != null && selectedFile.exists()) {
                btnSendFile.setEnabled(false);
                btnSendFile.setText("Đang gửi...");

                client.sendFile(
                        activeTarget,
                        selectedFile,
                        () -> SwingUtilities.invokeLater(() -> {
                            btnSendFile.setEnabled(true);
                            btnSendFile.setText("Gửi File");
                        }),
                        errorMsg -> SwingUtilities.invokeLater(() -> {
                            btnSendFile.setEnabled(true);
                            btnSendFile.setText("Gửi File");
                            Theme.showMessageDialog(this, errorMsg, "Lỗi gửi file", JOptionPane.ERROR_MESSAGE);
                        })
                );
            }
        }
    }

    private void handleExit() {
        if (client.isConnected()) {
            int opt = Theme.showConfirmDialog(
                    this,
                    "Bạn có muốn ngắt kết nối và đóng phòng chat không?",
                    "Xác nhận thoát",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (opt == JOptionPane.YES_OPTION) {
                client.disconnect();
                dispose();
            }
        } else {
            dispose();
        }
    }

    // =========================================================================
    // RENDERING MESSAGE BUBBLES
    // =========================================================================
    private void renderTextMessageBubble(Packet packet) {
        boolean isSelf = packet.getSender().equalsIgnoreCase(client.getUsername());
        boolean isPrivate = !packet.getRecipient().equalsIgnoreCase("ALL");

        JPanel wrapper = new JPanel(new FlowLayout(isSelf ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel bubble = Theme.createCard(14, isSelf ? Theme.BUBBLE_SELF : Theme.BUBBLE_OTHER, isSelf ? Theme.BORDER_GOLD : Theme.BORDER_COLOR);
        bubble.setLayout(new BorderLayout(4, 4));
        bubble.setBorder(new EmptyBorder(8, 12, 8, 12));

        // Header: Sender + Time
        JPanel headerLine = new JPanel(new BorderLayout(8, 0));
        headerLine.setOpaque(false);

        String senderLabel = isSelf ? "Bạn" : packet.getSender();
        if (isPrivate) {
            senderLabel += (isSelf ? " -> [" + packet.getRecipient() + "]" : " [Nhắn riêng]");
        }

        JLabel lblSender = new JLabel(senderLabel);
        lblSender.setFont(Theme.FONT_BOLD);
        lblSender.setForeground(isSelf ? Theme.GOLD_LIGHT : Theme.GOLD_BRIGHT);

        JLabel lblTime = new JLabel(packet.getTimestamp());
        lblTime.setFont(Theme.FONT_SMALL);
        lblTime.setForeground(new Color(203, 213, 225));

        headerLine.add(lblSender, BorderLayout.WEST);
        headerLine.add(lblTime, BorderLayout.EAST);

        // Content
        JTextArea contentArea = new JTextArea(packet.getContent());
        contentArea.setFont(Theme.FONT_REGULAR);
        contentArea.setForeground(Theme.TEXT_PRIMARY);
        contentArea.setOpaque(false);
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setMaximumSize(new Dimension(460, Integer.MAX_VALUE));

        bubble.add(headerLine, BorderLayout.NORTH);
        bubble.add(contentArea, BorderLayout.CENTER);

        wrapper.add(bubble);
        chatContainerPanel.add(wrapper);
        chatContainerPanel.add(Box.createVerticalStrut(6));
    }

    private void renderFileMessageCard(Packet packet) {
        boolean isSelf = packet.getSender().equalsIgnoreCase(client.getUsername());
        boolean isPrivate = !packet.getRecipient().equalsIgnoreCase("ALL");

        JPanel wrapper = new JPanel(new FlowLayout(isSelf ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel bubble = Theme.createCard(14, Theme.BUBBLE_FILE, Theme.BORDER_GOLD);
        bubble.setLayout(new BorderLayout(6, 6));
        bubble.setBorder(new EmptyBorder(10, 14, 10, 14));
        bubble.setPreferredSize(new Dimension(340, 95));

        // Header: Sender + Time
        JPanel headerLine = new JPanel(new BorderLayout(8, 0));
        headerLine.setOpaque(false);

        String senderLabel = isSelf ? "Bạn gửi tệp" : packet.getSender() + " gửi tệp";
        if (isPrivate) {
            senderLabel += (isSelf ? " -> [" + packet.getRecipient() + "]" : " [Riêng]");
        }

        JLabel lblSender = new JLabel(senderLabel);
        lblSender.setFont(Theme.FONT_BOLD);
        lblSender.setForeground(Theme.GOLD_LIGHT);

        JLabel lblTime = new JLabel(packet.getTimestamp());
        lblTime.setFont(Theme.FONT_SMALL);
        lblTime.setForeground(new Color(203, 213, 225));

        headerLine.add(lblSender, BorderLayout.WEST);
        headerLine.add(lblTime, BorderLayout.EAST);

        // Body: File info + Save Button
        JPanel body = new JPanel(new BorderLayout(8, 0));
        body.setOpaque(false);

        JLabel lblIcon = new JLabel("[FILE]");
        lblIcon.setFont(new Font("Consolas", Font.BOLD, 14));
        lblIcon.setForeground(Theme.GOLD_BRIGHT);

        JPanel nameAndSize = new JPanel(new GridLayout(2, 1, 0, 2));
        nameAndSize.setOpaque(false);

        JLabel lblFileName = new JLabel(packet.getFileName());
        lblFileName.setFont(Theme.FONT_BOLD);
        lblFileName.setForeground(Color.WHITE);

        JLabel lblSize = new JLabel(Packet.formatFileSize(packet.getFileSize()));
        lblSize.setFont(Theme.FONT_SMALL);
        lblSize.setForeground(Theme.TEXT_MUTED);

        nameAndSize.add(lblFileName);
        nameAndSize.add(lblSize);

        body.add(lblIcon, BorderLayout.WEST);
        body.add(nameAndSize, BorderLayout.CENTER);

        JButton btnDownload = Theme.createButton("Lưu Tệp...", Theme.PRIMARY, Color.WHITE, 6);
        btnDownload.setFont(Theme.FONT_SMALL);
        btnDownload.setPreferredSize(new Dimension(95, 26));
        btnDownload.addActionListener(e -> saveFileToDisk(packet));

        body.add(btnDownload, BorderLayout.EAST);

        bubble.add(headerLine, BorderLayout.NORTH);
        bubble.add(body, BorderLayout.CENTER);

        wrapper.add(bubble);
        chatContainerPanel.add(wrapper);
        chatContainerPanel.add(Box.createVerticalStrut(6));
    }

    private void renderNotificationPill(String text) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pill = Theme.createCard(12, new Color(24, 34, 50, 230), new Color(217, 119, 6, 140));
        pill.setBorder(new EmptyBorder(5, 14, 5, 14));

        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.GOLD_LIGHT);

        pill.add(lbl);
        wrapper.add(pill);
        chatContainerPanel.add(wrapper);
        chatContainerPanel.add(Box.createVerticalStrut(6));
    }

    private void saveFileToDisk(Packet packet) {
        if (packet.getFileData() == null || packet.getFileData().length == 0) {
            Theme.showMessageDialog(this, "Dữ liệu tệp tin rỗng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(packet.getFileName()));
        chooser.setDialogTitle("Chọn vị trí lưu tệp tin");

        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File dest = chooser.getSelectedFile();
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                fos.write(packet.getFileData());
                Theme.showMessageDialog(this, "Đã lưu tệp thành công vào:\n" + dest.getAbsolutePath(), "Lưu tệp thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                Theme.showMessageDialog(this, "Lỗi ghi tệp ra đĩa: " + e.getMessage(), "Lỗi ghi tệp", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar v = scrollChat.getVerticalScrollBar();
            v.setValue(v.getMaximum());
        });
    }

    // =========================================================================
    // LIST CELL RENDERER CHO SIDEBAR ONLINE USERS (ROYAL GOLD THEME)
    // =========================================================================
    private class UserListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JPanel p = new JPanel(new BorderLayout(8, 0));
            p.setOpaque(true);
            p.setBackground(isSelected ? new Color(180, 83, 9, 85) : Theme.BG_CARD);

            if (isSelected) {
                p.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 0, 0, Theme.GOLD_BRIGHT),
                        new EmptyBorder(6, 7, 6, 8)
                ));
            } else {
                p.setBorder(new EmptyBorder(6, 10, 6, 8));
            }

            String label = (String) value;
            if (index == 0) {
                JLabel lblAvatar = new JLabel(Theme.createAvatarIcon("A", 28, Theme.PRIMARY));

                JLabel lblName = new JLabel(label);
                lblName.setFont(label.contains("tin mới") || isSelected ? Theme.FONT_BOLD : Theme.FONT_REGULAR);
                lblName.setForeground(isSelected ? Theme.GOLD_LIGHT : (label.contains("tin mới") ? Theme.WARNING : Theme.TEXT_PRIMARY));

                JLabel lblDot = new JLabel("●");
                lblDot.setFont(new Font("Segoe UI", Font.BOLD, 10));
                lblDot.setForeground(Theme.SUCCESS);

                p.add(lblAvatar, BorderLayout.WEST);
                p.add(lblName, BorderLayout.CENTER);
                p.add(lblDot, BorderLayout.EAST);
            } else {
                String rawName = label.replaceAll("\\s*\\(.*\\)$", "").trim();
                int colorIndex = Math.abs(rawName.hashCode()) % AVATAR_COLORS.length;
                JLabel lblAvatar = new JLabel(Theme.createAvatarIcon(rawName, 28, AVATAR_COLORS[colorIndex]));

                JLabel lblName = new JLabel(label);
                lblName.setFont(label.contains("tin mới") || isSelected ? Theme.FONT_BOLD : Theme.FONT_REGULAR);
                lblName.setForeground(isSelected ? Theme.GOLD_LIGHT : (label.contains("tin mới") ? Theme.WARNING : Theme.TEXT_PRIMARY));

                JLabel lblDot = new JLabel("●");
                lblDot.setFont(new Font("Segoe UI", Font.BOLD, 10));
                lblDot.setForeground(Theme.SUCCESS);

                p.add(lblAvatar, BorderLayout.WEST);
                p.add(lblName, BorderLayout.CENTER);
                p.add(lblDot, BorderLayout.EAST);
            }
            return p;
        }
    }

    // =========================================================================
    // CALLBACKS TỪ CLIENT NETWORK ENGINE
    // =========================================================================
    @Override
    public void onConnected(String username) {
        SwingUtilities.invokeLater(() -> {
            lblMyName.setText(username);
            int colorIdx = Math.abs(username.hashCode()) % AVATAR_COLORS.length;
            lblMyAvatar.setIcon(Theme.createAvatarIcon(username, 42, AVATAR_COLORS[colorIdx]));
            cardLayout.show(rootCardPanel, "CHAT");
            setTitle("PTIT TCP CHAT - Đã kết nối: " + username + " (" + client.getHost() + ":" + client.getPort() + ")");

            Packet welcome = Packet.createNotification("Bạn đã tham gia phòng chat. Chúc bạn có trải nghiệm tuyệt vời!");
            getHistory("ALL").add(welcome);
            switchConversation("ALL");
        });
    }

    @Override
    public void onConnectionFailed(String reason) {
        SwingUtilities.invokeLater(() -> {
            btnConnect.setEnabled(true);
            lblLoginStatus.setText(reason);
            lblLoginStatus.setForeground(Theme.DANGER);
        });
    }

    @Override
    public void onDisconnected(String reason) {
        SwingUtilities.invokeLater(() -> {
            cardLayout.show(rootCardPanel, "LOGIN");
            btnConnect.setEnabled(true);
            lblLoginStatus.setText(reason);
            lblLoginStatus.setForeground(Theme.WARNING);
            conversationHistory.clear();
            unreadCounts.clear();
            chatContainerPanel.removeAll();
            userListModel.clear();
            currentOnlineUsers.clear();
            activeTarget = "ALL";
            setTitle("PTIT TCP CHAT & FILE TRANSFER - CLIENT");
        });
    }

    @Override
    public void onTextMessageReceived(Packet packet) {
        SwingUtilities.invokeLater(() -> {
            String targetKey;
            if (packet.getRecipient().equalsIgnoreCase("ALL")) {
                targetKey = "ALL";
            } else {
                targetKey = packet.getSender().equalsIgnoreCase(client.getUsername())
                        ? packet.getRecipient()
                        : packet.getSender();
            }

            getHistory(targetKey).add(packet);

            if (activeTarget.equalsIgnoreCase(targetKey)) {
                renderTextMessageBubble(packet);
                chatContainerPanel.revalidate();
                chatContainerPanel.repaint();
                scrollToBottom();
                unreadCounts.put(targetKey.toUpperCase(), 0);
                unreadCounts.put(targetKey, 0);
                updateSidebarList();
            } else {
                int unread = unreadCounts.getOrDefault(targetKey.toUpperCase(), 0) + 1;
                unreadCounts.put(targetKey.toUpperCase(), unread);
                updateSidebarList();
            }
        });
    }

    @Override
    public void onFileReceived(Packet packet) {
        SwingUtilities.invokeLater(() -> {
            String targetKey;
            if (packet.getRecipient().equalsIgnoreCase("ALL")) {
                targetKey = "ALL";
            } else {
                targetKey = packet.getSender().equalsIgnoreCase(client.getUsername())
                        ? packet.getRecipient()
                        : packet.getSender();
            }

            getHistory(targetKey).add(packet);

            if (activeTarget.equalsIgnoreCase(targetKey)) {
                renderFileMessageCard(packet);
                chatContainerPanel.revalidate();
                chatContainerPanel.repaint();
                scrollToBottom();
                unreadCounts.put(targetKey.toUpperCase(), 0);
                unreadCounts.put(targetKey, 0);
                updateSidebarList();
            } else {
                int unread = unreadCounts.getOrDefault(targetKey.toUpperCase(), 0) + 1;
                unreadCounts.put(targetKey.toUpperCase(), unread);
                updateSidebarList();
            }
        });
    }

    @Override
    public void onUserListUpdated(List<String> userList) {
        SwingUtilities.invokeLater(() -> {
            currentOnlineUsers.clear();
            for (String u : userList) {
                if (!u.equalsIgnoreCase(client.getUsername())) {
                    currentOnlineUsers.add(u);
                }
            }

            isUpdatingCombo = true;
            cbRecipient.removeAllItems();
            cbRecipient.addItem("Tất cả mọi người");

            for (String u : currentOnlineUsers) {
                cbRecipient.addItem(u);
            }

            if (activeTarget.equalsIgnoreCase("ALL")) {
                cbRecipient.setSelectedItem("Tất cả mọi người");
            } else if (currentOnlineUsers.contains(activeTarget)) {
                cbRecipient.setSelectedItem(activeTarget);
            } else {
                // Người dùng vừa rời phòng
                switchConversation("ALL");
            }
            isUpdatingCombo = false;

            updateSidebarList();
        });
    }

    @Override
    public void onNotification(String message) {
        SwingUtilities.invokeLater(() -> {
            Packet notif = Packet.createNotification(message);
            getHistory("ALL").add(notif);
            if (activeTarget.equalsIgnoreCase("ALL")) {
                renderNotificationPill(message);
                chatContainerPanel.revalidate();
                chatContainerPanel.repaint();
                scrollToBottom();
                unreadCounts.put("ALL", 0);
                updateSidebarList();
            } else {
                int unread = unreadCounts.getOrDefault("ALL", 0) + 1;
                unreadCounts.put("ALL", unread);
                updateSidebarList();
            }
        });
    }

    @Override
    public void onKicked(String reason) {
        SwingUtilities.invokeLater(() -> {
            Theme.showMessageDialog(this, "Bạn đã bị Quản trị viên mời ra khỏi phòng!\nLý do: " + reason, "Bị kick", JOptionPane.WARNING_MESSAGE);
            onDisconnected("Bạn đã bị Quản trị viên kick.");
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            ClientGUI clientGUI = new ClientGUI();
            clientGUI.setVisible(true);
        });
    }
}
