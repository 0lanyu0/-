package com.parkingmanagement.ui;

import com.parkingmanagement.dao.LoginDAO;
import com.parkingmanagement.dao.YonghuDAO;
import com.parkingmanagement.model.Login;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;
    private BufferedImage backgroundImage;

    // 数据库操作对象
    private LoginDAO loginDAO = new LoginDAO();
    private YonghuDAO yonghuDAO = new YonghuDAO();

    public LoginFrame() {
        setTitle("智慧停车场管理系统 - 登录");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        // 加载背景图片
        try {
            backgroundImage = javax.imageio.ImageIO.read(getClass().getClassLoader().getResourceAsStream("com/beijing.png"));
            if (backgroundImage == null) {
                System.err.println("警告：未找到背景图片 com/beijing.png，使用默认背景");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("背景图片加载失败，使用默认背景");
        }
        initializeUI();
    }

    private void initializeUI() {
        // 使用自定义背景面板
        BackgroundPanel mainPanel = new BackgroundPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        // 顶部标题面板
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(41, 128, 185));
        topPanel.setPreferredSize(new Dimension(1200, 100));
        JLabel titleLabel = new JLabel("智慧停车场管理系统");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        // 中间登录表单面板
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false); // 透明背景显示图片
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        // 登录表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        formPanel.setBackground(new Color(255, 255, 255, 220)); // 半透明白色背景
        formPanel.setPreferredSize(new Dimension(400, 350));
        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.insets = new Insets(15, 20, 15, 20);
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formGbc.anchor = GridBagConstraints.CENTER;
        formGbc.gridwidth = 2;
        // 表单标题
        JLabel formTitle = new JLabel("用户登录");
        formTitle.setFont(new Font("微软雅黑", Font.BOLD, 20));
        formTitle.setHorizontalAlignment(SwingConstants.CENTER);
        formGbc.gridx = 0;
        formGbc.gridy = 0;
        formPanel.add(formTitle, formGbc);
        // 用户名标签和输入框
        formGbc.gridwidth = 1;
        formGbc.gridy = 1;
        formGbc.anchor = GridBagConstraints.EAST;
        JLabel usernameLabel = new JLabel("用户名:");
        usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(usernameLabel, formGbc);
        formGbc.gridx = 1;
        formGbc.anchor = GridBagConstraints.WEST;
        usernameField = new JTextField();
        usernameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(200, 30));
        formPanel.add(usernameField, formGbc);
        // 密码标签和输入框
        formGbc.gridy = 2;
        formGbc.gridx = 0;
        formGbc.anchor = GridBagConstraints.EAST;
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(passwordLabel, formGbc);
        formGbc.gridx = 1;
        formGbc.anchor = GridBagConstraints.WEST;
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(200, 30));
        formPanel.add(passwordField, formGbc);
        // 显示消息标签
        formGbc.gridy = 3;
        formGbc.gridx = 0;
        formGbc.gridwidth = 2;
        formGbc.anchor = GridBagConstraints.CENTER;
        messageLabel = new JLabel("");
        messageLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        messageLabel.setForeground(Color.RED);
        formPanel.add(messageLabel, formGbc);
        // 登录按钮
        formGbc.gridy = 4;
        JButton loginButton = new JButton("登录");
        loginButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(100, 35));
        loginButton.setBackground(new Color(41, 128, 185));
        loginButton.setForeground(Color.WHITE);
        loginButton.addActionListener(e -> login());
        formPanel.add(loginButton, formGbc);
        // 链接面板
        formGbc.gridy = 5;
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        linkPanel.setBackground(new Color(255, 255, 255, 0)); // 完全透明
        linkPanel.setOpaque(false);
        JButton forgotBtn = new JButton("忘记密码");
        forgotBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        forgotBtn.setBorderPainted(false);
        forgotBtn.setContentAreaFilled(false);
        forgotBtn.setForeground(new Color(41, 128, 185));
        forgotBtn.addActionListener(e -> showForgotPasswordDialog());
        JButton registerBtn = new JButton("注册账号");
        registerBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        registerBtn.setBorderPainted(false);
        registerBtn.setContentAreaFilled(false);
        registerBtn.setForeground(new Color(41, 128, 185));
        registerBtn.addActionListener(e -> showRegisterDialog());
        // 车位预约按钮，供未登录时使用
        JButton reservationBtn = new JButton("车位预约");
        reservationBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        reservationBtn.setBorderPainted(false);
        reservationBtn.setContentAreaFilled(false);
        reservationBtn.setForeground(new Color(41, 128, 185));
        reservationBtn.addActionListener(e -> showPublicReservationManagement());
        linkPanel.add(forgotBtn);
        linkPanel.add(registerBtn);
        linkPanel.add(reservationBtn);
        formPanel.add(linkPanel, formGbc);
        // 将表单面板添加到中间面板
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(formPanel, gbc);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        // 底部版权信息
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(41, 128, 185, 180)); // 半透明底部
        bottomPanel.setPreferredSize(new Dimension(1200, 50));
        JLabel copyrightLabel = new JLabel("  by软件三班7，8，9号三位同学");
        copyrightLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        copyrightLabel.setForeground(Color.WHITE);
        bottomPanel.add(copyrightLabel);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("用户名和密码不能为空");
            return;
        }

        try {
            // 先查询yonghu表
            Login yonghuUser = yonghuDAO.getUserByUsername(username);
            if (yonghuUser != null && verifyPassword(password, yonghuUser.getPasswordHash())) {
                messageLabel.setText("");
                JOptionPane.showMessageDialog(this, "登录成功，欢迎专用/永久车位的您使用系统", "成功", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
                yonghuMainFrame yonghuMainFrame = new yonghuMainFrame();
                yonghuMainFrame.setVisible(true);
                return; // yonghu表账号只做专用车位预约系统登录
            }

            // 再查询login表
            Login loginUser = loginDAO.getUserByUsername(username);
            if (loginUser != null && verifyPassword(password, loginUser.getPasswordHash())) {
                messageLabel.setText("");
                JOptionPane.showMessageDialog(this, "登录成功，欢迎管理员使用系统", "登录成功", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
                return;
            }

            // 两个表都无该账号
            messageLabel.setText("用户名或密码错误");
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("登录失败，请稍后重试");
        }
    }

    private void showPublicReservationManagement() {
        JFrame reservationFrame = new JFrame("车位预约");
        reservationFrame.setSize(1000, 600);
        reservationFrame.setLocationRelativeTo(this);
        reservationFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // 创建公共车位预约面板
        PublicReservationPanel reservationPanel = new PublicReservationPanel();
        reservationFrame.add(reservationPanel);
        reservationFrame.setVisible(true);
    }

    private void showForgotPasswordDialog() {
        JDialog dialog = new JDialog(this, "忘记密码", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JTextField usernameField = new JTextField();
        JTextField emailField = new JTextField();
        panel.add(new JLabel("用户名:"));
        panel.add(usernameField);
        panel.add(new JLabel("注册邮箱:"));
        panel.add(emailField);
        panel.add(new JLabel(""));
        panel.add(new JLabel(""));
        JButton resetButton = new JButton("重置密码");
        resetButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            if (username.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入用户名和邮箱", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                // 查询yonghu表
                YonghuDAO yonghuDAO = new YonghuDAO();
                Login user = yonghuDAO.getUserByUsername(username);
                if (user == null) {
                    JOptionPane.showMessageDialog(dialog, "用户名不存在", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!user.getEmail().equals(email)) {
                    JOptionPane.showMessageDialog(dialog, "邮箱地址不匹配", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String newPassword = generateRandomPassword(8);
                String hashedPassword = hashPassword(newPassword);
                user.setPasswordHash(hashedPassword);
                yonghuDAO.updateUser(user);
                JOptionPane.showMessageDialog(dialog,
                        "新密码为：" + newPassword + "\n请使用新密码登录",
                        "密码重置成功",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "密码重置失败，请稍后重试", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(resetButton);
        buttonPanel.add(cancelButton);
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showRegisterDialog() {
        JDialog dialog = new JDialog(this, "注册用户账号", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        panel.add(new JLabel("用户名:"));
        panel.add(usernameField);
        panel.add(new JLabel("密码:"));
        panel.add(passwordField);
        panel.add(new JLabel("确认密码:"));
        panel.add(confirmField);
        panel.add(new JLabel("邮箱:"));
        panel.add(emailField);
        panel.add(new JLabel("电话:"));
        panel.add(phoneField);
        JButton registerButton = new JButton("注册");
        registerButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmField.getPassword());
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "用户名、密码和邮箱不能为空", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(dialog, "两次输入的密码不一致", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                // 注册到yonghu表
                YonghuDAO yonghuDAO = new YonghuDAO();
                Login existingUser = yonghuDAO.getUserByUsername(username);
                if (existingUser != null) {
                    JOptionPane.showMessageDialog(dialog, "用户名已存在", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Login newUser = new Login();
                newUser.setUsername(username);
                String hashedPassword = hashPassword(password);
                newUser.setPasswordHash(hashedPassword);
                newUser.setEmail(email);
                newUser.setPhone(phone);
                yonghuDAO.addUser(newUser);
                JOptionPane.showMessageDialog(dialog, "注册成功，请使用新账号登录", "成功", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "注册失败，请稍后重试", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // 密码哈希方法
    private static String hashPassword(String password) throws Exception {
        int iterations = 10000;
        int keyLength = 256;
        byte[] salt = new byte[16]; // 使用固定值，实际应用中应使用随机值
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    private static boolean verifyPassword(String password, String hashedPassword) throws Exception {
        String newHash = hashPassword(password);
        return newHash.equals(hashedPassword);
    }

    private static String generateRandomPassword(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+=-{}[]|;':\",./<>?";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            sb.append(characters.charAt(randomIndex));
        }
        return sb.toString();
    }

    // 自定义背景面板
    private class BackgroundPanel extends JPanel {
        public BackgroundPanel(LayoutManager layout) {
            super(layout);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                // 缩放背景图片以适应窗口大小
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                g2d.dispose();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}