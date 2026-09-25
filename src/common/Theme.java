package common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;

/**
 * Hệ thống giao diện Hoàng gia Cao cấp (Royal Obsidian & Gold Design System).
 * Tông màu Đen Huyền Bí (Deep Obsidian) kết hợp Vàng Hoàng Gia (Imperial Gold) sang trọng, đẳng cấp.
 */
public class Theme {
    // Bảng màu chuẩn Luxury Obsidian & Gold
    public static final Color BG_MAIN       = new Color(11, 15, 25);      // Deep Obsidian Black #0B0F19
    public static final Color BG_CARD       = new Color(19, 26, 38);      // Luxury Charcoal #131A26
    public static final Color BG_INPUT      = new Color(24, 34, 50);      // Deep Charcoal Input #182232
    public static final Color BORDER_COLOR  = new Color(42, 54, 73);      // Subtle Border #2A3649
    public static final Color BORDER_GOLD   = new Color(217, 119, 6);     // Amber Gold Border #D97706
    public static final Color BORDER_FOCUS  = new Color(251, 191, 36);    // Radiant Gold Glow #FBBF24

    // Hệ màu Vàng Hoàng Gia (Gold Palette)
    public static final Color PRIMARY       = new Color(217, 119, 6);     // Royal Amber Gold #D97706
    public static final Color PRIMARY_HOVER = new Color(245, 158, 11);    // Luminous Amber #F59E0B
    public static final Color PRIMARY_DARK  = new Color(180, 83, 9);      // Deep Bronze Gold #B45309
    public static final Color GOLD_LIGHT    = new Color(252, 211, 77);    // Champagne Gold #FCD34D
    public static final Color GOLD_BRIGHT   = new Color(251, 191, 36);    // Bright Gold #FBBF24
    public static final Color ACCENT        = new Color(251, 191, 36);    // Gold Accent

    public static final Color SUCCESS       = new Color(16, 185, 129);    // Emerald #10B981
    public static final Color DANGER        = new Color(239, 68, 68);     // Ruby Rose #EF4444
    public static final Color WARNING       = new Color(245, 158, 11);    // Amber #F59E0B

    // Typography Colors
    public static final Color TEXT_PRIMARY  = new Color(255, 251, 235);   // Warm Ivory White #FFFBEB
    public static final Color TEXT_MUTED    = new Color(156, 163, 175);   // Silver Slate #9CA3AF
    public static final Color TEXT_HINT     = new Color(107, 114, 128);   // Muted Slate #6B7280
    public static final Color TEXT_GOLD     = new Color(252, 211, 77);    // Gold Highlight Text #FCD34D

    // Chat Bubbles
    public static final Color BUBBLE_SELF   = new Color(180, 83, 9);      // Deep Royal Amber #B45309
    public static final Color BUBBLE_OTHER  = new Color(27, 36, 50);      // Obsidian Slate #1B2432
    public static final Color BUBBLE_FILE   = new Color(30, 41, 59);      // Luxury Card Slate #1E293B

    // Fonts chuẩn
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 12);

    /**
     * Tạo nút bấm bo góc Hoàng Gia mạ vàng (Gold Luxury Button) với hiệu ứng bóng mờ & đổi màu
     */
    public static JButton createButton(String text, Color bgColor, Color fgColor, int radius) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color currentBg = bgColor;
                if (getModel().isPressed()) {
                    currentBg = bgColor.darker();
                } else if (getModel().isRollover()) {
                    currentBg = (bgColor.equals(PRIMARY)) ? PRIMARY_HOVER : bgColor.brighter();
                }

                // Nếu là nút chính Primary Gold, đổ dải màu Gradient ánh kim lộng lẫy
                if (bgColor.equals(PRIMARY)) {
                    GradientPaint gp = new GradientPaint(
                            0, 0, currentBg,
                            0, getHeight(), currentBg.darker()
                    );
                    g2.setPaint(gp);
                } else {
                    g2.setColor(currentBg);
                }

                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));

                // Viền mạ vàng thanh lịch cho các nút
                if (bgColor.equals(PRIMARY) || bgColor.equals(PRIMARY_DARK)) {
                    g2.setColor(BORDER_FOCUS);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
                } else if (bgColor.equals(BORDER_COLOR)) {
                    g2.setColor(new Color(217, 119, 6, 120)); // Viền vàng kim tinh tế
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    /**
     * Tạo panel thẻ Card bo góc sang trọng có viền ánh kim
     */
    public static JPanel createCard(int radius, Color bg, Color border) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
                if (border != null) {
                    g2.setColor(border);
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        return p;
    }

    /**
     * Tạo ô nhập liệu bo góc với viền sáng vàng khi Focus (Gold Glow Border)
     */
    public static JTextField createTextField(String placeholder, int radius) {
        JTextField tf = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));

                Color bc = isFocusOwner() ? BORDER_FOCUS : BORDER_COLOR;
                g2.setColor(bc);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 1.5f : 1.0f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
                g2.dispose();

                super.paintComponent(g);

                if (getText().isEmpty() && !isFocusOwner() && placeholder != null) {
                    Graphics2D gText = (Graphics2D) g.create();
                    gText.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    gText.setFont(FONT_REGULAR);
                    gText.setColor(TEXT_HINT);
                    Insets insets = getInsets();
                    FontMetrics fm = gText.getFontMetrics();
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    gText.drawString(placeholder, insets.left, y);
                    gText.dispose();
                }
            }
        };
        tf.setOpaque(false);
        tf.setFont(FONT_REGULAR);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(GOLD_BRIGHT);
        tf.setBorder(new EmptyBorder(8, 12, 8, 12));
        return tf;
    }

    /**
     * Tạo Avatar tròn có viền chỉ vàng kim (Gold Ring Avatar)
     */
    public static ImageIcon createAvatarIcon(String name, int size, Color bg) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Nền Avatar
        g2.setColor(bg);
        g2.fillOval(0, 0, size - 1, size - 1);

        // Viền vàng kim thanh lịch
        g2.setColor(new Color(251, 191, 36, 180));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawOval(0, 0, size - 1, size - 1);

        // Chữ cái đầu
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, (int) (size * 0.5)));
        FontMetrics fm = g2.getFontMetrics();
        String initial = (name != null && !name.trim().isEmpty()) ? name.trim().substring(0, 1).toUpperCase() : "?";
        int x = (size - fm.stringWidth(initial)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(initial, x, y);
        g2.dispose();
        return new ImageIcon(img);
    }

    /**
     * Gắn thanh cuộn ánh kim mạ vàng (Gold Scrollbar) cho JScrollPane
     */
    public static void applyModernScrollbar(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_MAIN);
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(55, 65, 81);
                this.trackColor = BG_MAIN;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setMinimumSize(new Dimension(0, 0));
                b.setMaximumSize(new Dimension(0, 0));
                return b;
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isThumbRollover() ? BORDER_GOLD : thumbColor);
                g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 6, 6);
                g2.dispose();
            }
        });
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    }

    // =========================================================================
    // HỘP THOẠI HIỆN ĐẠI (CUSTOM LUXURY DIALOGS)
    // =========================================================================

    public static void showMessageDialog(Component parent, String message, String title) {
        showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showMessageDialog(Component parent, String message, String title, int messageType) {
        Window owner = (parent instanceof Window) ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        try {
            dialog.setBackground(new Color(0, 0, 0, 0));
        } catch (Exception ignored) {}

        JPanel root = createCard(14, BG_CARD, BORDER_GOLD);
        root.setLayout(new BorderLayout());

        JPanel header = createDialogHeader(dialog, title);

        JPanel body = new JPanel(new BorderLayout(14, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(18, 20, 16, 20));

        JComponent iconComp = createDialogIcon(messageType);
        body.add(iconComp, BorderLayout.WEST);

        JTextArea txtMsg = new JTextArea(message);
        txtMsg.setFont(FONT_REGULAR);
        txtMsg.setForeground(TEXT_PRIMARY);
        txtMsg.setOpaque(false);
        txtMsg.setEditable(false);
        txtMsg.setFocusable(false);
        txtMsg.setLineWrap(true);
        txtMsg.setWrapStyleWord(true);
        txtMsg.setColumns(26);

        body.add(txtMsg, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(0, 20, 16, 20));

        JButton btnOk = createButton("Đồng ý", PRIMARY, Color.WHITE, 8);
        btnOk.setPreferredSize(new Dimension(95, 34));
        btnOk.addActionListener(e -> dialog.dispose());
        buttonPanel.add(btnOk);

        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(buttonPanel, BorderLayout.SOUTH);

        setupDialogKeys(dialog, btnOk, null);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(380, 160));
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    public static String showInputDialog(Component parent, String message, String title) {
        return showInputDialog(parent, message, title, JOptionPane.QUESTION_MESSAGE);
    }

    public static String showInputDialog(Component parent, String message, String title, int messageType) {
        Window owner = (parent instanceof Window) ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        try {
            dialog.setBackground(new Color(0, 0, 0, 0));
        } catch (Exception ignored) {}

        JPanel root = createCard(14, BG_CARD, BORDER_GOLD);
        root.setLayout(new BorderLayout());

        JPanel header = createDialogHeader(dialog, title);

        JPanel body = new JPanel(new BorderLayout(14, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 20, 12, 20));

        JComponent iconComp = createDialogIcon(messageType);
        body.add(iconComp, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);

        JLabel lblMsg = new JLabel(message);
        lblMsg.setFont(FONT_BOLD);
        lblMsg.setForeground(TEXT_PRIMARY);

        JTextField txtInput = createTextField("", 8);
        txtInput.setPreferredSize(new Dimension(280, 36));

        centerPanel.add(lblMsg, BorderLayout.NORTH);
        centerPanel.add(txtInput, BorderLayout.CENTER);

        body.add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(0, 20, 16, 20));

        final String[] result = new String[1];

        JButton btnCancel = createButton("Hủy bỏ", BORDER_COLOR, TEXT_MUTED, 8);
        btnCancel.setPreferredSize(new Dimension(90, 34));
        btnCancel.addActionListener(e -> {
            result[0] = null;
            dialog.dispose();
        });

        JButton btnOk = createButton("Xác nhận", PRIMARY, Color.WHITE, 8);
        btnOk.setPreferredSize(new Dimension(95, 34));
        btnOk.addActionListener(e -> {
            result[0] = txtInput.getText();
            dialog.dispose();
        });

        txtInput.addActionListener(e -> btnOk.doClick());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnOk);

        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(buttonPanel, BorderLayout.SOUTH);

        setupDialogKeys(dialog, btnOk, btnCancel);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(420, 175));
        dialog.setLocationRelativeTo(owner);

        SwingUtilities.invokeLater(() -> txtInput.requestFocusInWindow());

        dialog.setVisible(true);
        return result[0];
    }

    public static int showConfirmDialog(Component parent, String message, String title, int optionType) {
        return showConfirmDialog(parent, message, title, optionType, JOptionPane.QUESTION_MESSAGE);
    }

    public static int showConfirmDialog(Component parent, String message, String title, int optionType, int messageType) {
        Window owner = (parent instanceof Window) ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        try {
            dialog.setBackground(new Color(0, 0, 0, 0));
        } catch (Exception ignored) {}

        JPanel root = createCard(14, BG_CARD, BORDER_GOLD);
        root.setLayout(new BorderLayout());

        JPanel header = createDialogHeader(dialog, title);

        JPanel body = new JPanel(new BorderLayout(14, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(18, 20, 16, 20));

        JComponent iconComp = createDialogIcon(messageType);
        body.add(iconComp, BorderLayout.WEST);

        JTextArea txtMsg = new JTextArea(message);
        txtMsg.setFont(FONT_REGULAR);
        txtMsg.setForeground(TEXT_PRIMARY);
        txtMsg.setOpaque(false);
        txtMsg.setEditable(false);
        txtMsg.setFocusable(false);
        txtMsg.setLineWrap(true);
        txtMsg.setWrapStyleWord(true);
        txtMsg.setColumns(26);

        body.add(txtMsg, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(0, 20, 16, 20));

        final int[] result = new int[]{JOptionPane.CLOSED_OPTION};

        JButton btnNo = createButton("Không", BORDER_COLOR, TEXT_MUTED, 8);
        btnNo.setPreferredSize(new Dimension(85, 34));
        btnNo.addActionListener(e -> {
            result[0] = JOptionPane.NO_OPTION;
            dialog.dispose();
        });

        JButton btnYes = createButton("Đồng ý", PRIMARY, Color.WHITE, 8);
        btnYes.setPreferredSize(new Dimension(95, 34));
        btnYes.addActionListener(e -> {
            result[0] = JOptionPane.YES_OPTION;
            dialog.dispose();
        });

        buttonPanel.add(btnNo);
        buttonPanel.add(btnYes);

        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(buttonPanel, BorderLayout.SOUTH);

        setupDialogKeys(dialog, btnYes, btnNo);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(380, 160));
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);

        return result[0];
    }

    private static JPanel createDialogHeader(JDialog dialog, String title) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(42, 54, 73, 160)),
                new EmptyBorder(8, 16, 8, 10)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_BOLD);
        lblTitle.setForeground(GOLD_LIGHT);

        JButton btnClose = new JButton("✕");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClose.setForeground(TEXT_MUTED);
        btnClose.setOpaque(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnClose.setForeground(DANGER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnClose.setForeground(TEXT_MUTED);
            }
        });
        btnClose.addActionListener(e -> dialog.dispose());

        final Point[] dragPoint = new Point[1];
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragPoint[0] = e.getPoint();
            }
        });
        header.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragPoint[0] != null) {
                    Point p = dialog.getLocation();
                    dialog.setLocation(p.x + e.getX() - dragPoint[0].x, p.y + e.getY() - dragPoint[0].y);
                }
            }
        });

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnClose, BorderLayout.EAST);
        return header;
    }

    public static JComponent createDialogIcon(int messageType) {
        return new JComponent() {
            {
                setPreferredSize(new Dimension(44, 44));
                setMinimumSize(new Dimension(44, 44));
                setMaximumSize(new Dimension(44, 44));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color bg, border, fg;
                String symbol;

                if (messageType == JOptionPane.ERROR_MESSAGE) {
                    bg = new Color(127, 29, 29, 140);
                    border = DANGER;
                    fg = DANGER;
                    symbol = "✕";
                } else if (messageType == JOptionPane.WARNING_MESSAGE) {
                    bg = new Color(180, 83, 9, 140);
                    border = GOLD_BRIGHT;
                    fg = GOLD_LIGHT;
                    symbol = "⚠";
                } else if (messageType == JOptionPane.QUESTION_MESSAGE) {
                    bg = new Color(30, 58, 138, 140);
                    border = new Color(96, 165, 250);
                    fg = new Color(147, 197, 253);
                    symbol = "?";
                } else {
                    bg = new Color(6, 78, 59, 140);
                    border = SUCCESS;
                    fg = SUCCESS;
                    symbol = "✓";
                }

                g2.setColor(bg);
                g2.fillOval(2, 2, 40, 40);
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(2, 2, 40, 40);

                g2.setColor(fg);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                int x = (44 - fm.stringWidth(symbol)) / 2;
                int y = ((44 - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(symbol, x, y);

                g2.dispose();
            }
        };
    }

    private static void setupDialogKeys(JDialog dialog, JButton confirmBtn, JButton cancelBtn) {
        dialog.getRootPane().setDefaultButton(confirmBtn);
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cancelBtn != null) {
                    cancelBtn.doClick();
                } else {
                    dialog.dispose();
                }
            }
        };
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        dialog.getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }
}
