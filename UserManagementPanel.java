package com.parkingmanagement.ui;
import com.parkingmanagement.dao.LoginDAO;
import com.parkingmanagement.model.Login;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class UserManagementPanel extends JPanel {
    private LoginDAO loginDAO;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public UserManagementPanel() {
        setLayout(new BorderLayout());
        loginDAO = new LoginDAO();
        initializeUI();
        loadUsers();
    }

    private void initializeUI() {
        // 搜索面板
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("搜索");
        JButton addButton = new JButton("添加用户");
        JButton editButton = new JButton("编辑用户");
        JButton deleteButton = new JButton("删除用户");
        JButton refreshButton = new JButton("刷新");
        JButton resetPasswordButton = new JButton("重置密码"); // 新增重置密码按钮

        searchButton.addActionListener(e -> searchUsers());
        addButton.addActionListener(e -> showAddUserDialog());
        editButton.addActionListener(e -> showEditUserDialog());
        deleteButton.addActionListener(e -> deleteSelectedUser());
        refreshButton.addActionListener(e -> loadUsers());
        resetPasswordButton.addActionListener(e -> showResetPasswordDialog()); // 为重置密码按钮添加事件监听器

        searchPanel.add(new JLabel("用户名:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(addButton);
        searchPanel.add(editButton);
        searchPanel.add(deleteButton);
        searchPanel.add(refreshButton);
        searchPanel.add(resetPasswordButton); // 将重置密码按钮添加到搜索面板

        // 中间用户表格（匹配login表结构）
        String[] columnNames = {"用户ID", "用户名", "电子邮箱", "联系电话", "创建时间", "更新时间"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(userTable);

        // 添加到主面板
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        List<Login> users = loginDAO.getAllUsers();
        for (Login user : users) {
            tableModel.addRow(new Object[]{
                    user.getUserId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getCreateTime().format(formatter),
                    user.getUpdateTime().format(formatter)
            });
        }
    }

    private void searchUsers() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadUsers();
            return;
        }
        tableModel.setRowCount(0);
        List<Login> users = loginDAO.getAllUsers(); // 实际逻辑应实现搜索功能
        for (Login user : users) {
            if (user.getUsername().contains(keyword) || user.getEmail().contains(keyword) || user.getPhone().contains(keyword)) {
                tableModel.addRow(new Object[]{
                        user.getUserId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getCreateTime().format(formatter),
                        user.getUpdateTime().format(formatter)
                });
            }
        }
    }

    private void showAddUserDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "添加用户", true);
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
                LoginDAO loginDAO = new LoginDAO();
                Login existingUser = loginDAO.getUserByUsername(username);
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
                newUser.setCreateTime(LocalDateTime.now());
                newUser.setUpdateTime(LocalDateTime.now());
                loginDAO.addUser(newUser);
                JOptionPane.showMessageDialog(dialog, "注册成功，请使用该账号登录", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
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

    private void showEditUserDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要编辑的用户!", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        Login user = loginDAO.getUserByUsername((String) tableModel.getValueAt(selectedRow, 1));

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "编辑用户", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(5, 2, 10, 10));
        JTextField usernameField = new JTextField(user.getUsername());
        JTextField emailField = new JTextField(user.getEmail() != null ? user.getEmail() : "");
        JTextField phoneField = new JTextField(user.getPhone() != null ? user.getPhone() : "");

        usernameField.setEditable(false); // 用户名不可编辑
        formPanel.add(new JLabel("用户名:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("电子邮箱:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("联系电话:"));
        formPanel.add(phoneField);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");
        saveButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            user.setEmail(email);
            user.setPhone(phone);
            user.setUpdateTime(LocalDateTime.now());

            if (loginDAO.updateUser(user)) {
                JOptionPane.showMessageDialog(dialog, "用户信息更新成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "用户信息更新失败!", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showResetPasswordDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要重置密码的用户!", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        Login user = loginDAO.getUserByUsername(username);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "确认要重置该用户的密码吗?",
                "确认重置",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String newPassword = generateRandomPassword(8);
                String hashedPassword = hashPassword(newPassword);
                user.setPasswordHash(hashedPassword);
                loginDAO.updateUser(user);
                JOptionPane.showMessageDialog(this,
                        "密码重置成功，新密码为：" + newPassword + "\n请使用新密码登录",
                        "密码重置成功",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "密码重置失败，请稍后重试", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的用户!", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int userId = (int) tableModel.getValueAt(selectedRow, 0);

        // 系统管理员账号ID=1，不能删除
        if (userId == 1) {
            JOptionPane.showMessageDialog(this, "不能删除系统管理员!", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "确认要删除选中的用户吗?",
                "确认删除",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            if (loginDAO.deleteUser(userId)) {
                JOptionPane.showMessageDialog(this, "用户删除成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "用户删除失败!", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 哈希密码方法
    private static String hashPassword(String password) throws Exception {
        int iterations = 10000;
        int keyLength = 256;
        byte[] salt = new byte[16]; // 使用固定值，实际应用应使用随机值
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    // 生成随机密码方法
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
}