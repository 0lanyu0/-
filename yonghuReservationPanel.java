package com.parkingmanagement.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Date;
import java.util.Vector;

public class yonghuReservationPanel extends JPanel {
    // 数据库连接信息
    private static final String DB_URL = "jdbc:mysql://localhost:3306/parking_management?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "2004";

    // 输入控件
    private JTextField txtSpaceId, txtName, txtPhone, txtLicensePlate, txtVehicleColor, txtVehicleId, txtSearch;
    private JComboBox<String> cmbGender;
    private JComboBox<String> cmbStatus;
    private JCheckBox chkIsPermanent;
    private JRadioButton rdoDedicated, rdoPermanent;
    private JTable table;
    private DefaultTableModel tableModel;
    private JSpinner spnStartTime, spnEndTime;
    // 当前显示的预约记录ID
    private int currentReservationId = -1;

    public yonghuReservationPanel() {
        setLayout(new BorderLayout());
        // 创建输入信息面板
        JPanel topPanel = createInputPanel();
        add(topPanel, BorderLayout.NORTH);
        // 创建中间数据显示面板
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        // 创建底部操作按钮面板
        JPanel bottomPanel = createButtonPanel();
        add(bottomPanel, BorderLayout.SOUTH);
        // 初始化数据（初始不加载数据）
        clearTable();
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 左侧输入字段
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("车位ID："), gbc);
        gbc.gridx = 1;
        txtSpaceId = new JTextField(15);
        panel.add(txtSpaceId, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("姓名："), gbc);
        gbc.gridx = 1;
        txtName = new JTextField(15);
        panel.add(txtName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("性别："), gbc);
        gbc.gridx = 1;
        cmbGender = new JComboBox<>(new String[]{"男", "女"});
        panel.add(cmbGender, gbc);

        // 右侧输入字段
        gbc.gridx = 2;
        gbc.gridy = 0;
        panel.add(new JLabel("电话："), gbc);
        gbc.gridx = 3;
        txtPhone = new JTextField(15);
        panel.add(txtPhone, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        panel.add(new JLabel("车牌号："), gbc);
        gbc.gridx = 3;
        txtLicensePlate = new JTextField(15);
        panel.add(txtLicensePlate, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        panel.add(new JLabel("车辆颜色："), gbc);
        gbc.gridx = 3;
        txtVehicleColor = new JTextField(15);
        panel.add(txtVehicleColor, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("车辆ID："), gbc);
        gbc.gridx = 1;
        txtVehicleId = new JTextField(15);
        panel.add(txtVehicleId, gbc);

        gbc.gridx = 2;
        gbc.gridy = 3;
        panel.add(new JLabel("状态："), gbc);
        gbc.gridx = 3;
        cmbStatus = new JComboBox<>(new String[]{"已预约", "已使用", "已取消", "已过期"});
        panel.add(cmbStatus, gbc);

        // 搜索框（新增）
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("搜索："), gbc);
        gbc.gridx = 1;
        txtSearch = new JTextField(15);
        panel.add(txtSearch, gbc);
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        JButton searchButton = new JButton("搜索");
        searchButton.addActionListener(e -> searchRecord());
        panel.add(searchButton, gbc);
        gbc.gridwidth = 1;

        // 是否永久专用车位
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("是否永久专用："), gbc);
        gbc.gridx = 1;
        chkIsPermanent = new JCheckBox();
        panel.add(chkIsPermanent, gbc);

        // 预约时间选择器
        gbc.gridx = 0;
        gbc.gridy = 6;
        panel.add(new JLabel("预约开始时间："), gbc);
        gbc.gridx = 1;
        spnStartTime = new JSpinner(new SpinnerDateModel());
        spnStartTime.setEditor(new JSpinner.DateEditor(spnStartTime, "yyyy-MM-dd HH:mm:ss"));
        panel.add(spnStartTime, gbc);

        gbc.gridx = 2;
        gbc.gridy = 6;
        panel.add(new JLabel("预约结束时间："), gbc);
        gbc.gridx = 3;
        spnEndTime = new JSpinner(new SpinnerDateModel());
        spnEndTime.setEditor(new JSpinner.DateEditor(spnEndTime, "yyyy-MM-dd HH:mm:ss"));
        panel.add(spnEndTime, gbc);

        // 保存按钮
        gbc.gridx = 2;
        gbc.gridy = 5;
        JButton btnSave = new JButton("保存");
        panel.add(btnSave, gbc);
        btnSave.addActionListener(e -> saveRecord());

        return panel;
    }

    private void saveRecord() {
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // 创建切换按钮
        JPanel switchPanel = new JPanel();
        rdoDedicated = new JRadioButton("专用车位预约", true);
        rdoPermanent = new JRadioButton("永久专用车位");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rdoDedicated);
        bg.add(rdoPermanent);
        switchPanel.add(rdoDedicated);
        switchPanel.add(rdoPermanent);
        panel.add(switchPanel, BorderLayout.NORTH);

        // 创建表格
        String[] columnNames = {"编号", "车位ID", "姓名", "性别", "电话", "车牌号", "车辆颜色", "车辆ID", "状态", "预约开始时间", "预约结束时间"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);

        // 设置表格数据居中显示
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 切换按钮事件监听器
        rdoDedicated.addActionListener(e -> {
            if (currentReservationId > 0) {
                loadCurrentReservationData();
            }
        });
        rdoPermanent.addActionListener(e -> {
            if (currentReservationId > 0) {
                loadCurrentReservationData();
            }
        });

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        JButton btnAdd = new JButton("添加");
        JButton btnEdit = new JButton("修改");
        JButton btnDelete = new JButton("删除");
        JButton btnRefresh = new JButton("刷新");
        JButton btnBack = new JButton("返回");

        panel.add(btnAdd);
        panel.add(btnEdit);
        panel.add(btnDelete);
        panel.add(btnRefresh);
        panel.add(btnBack);

        // 添加按钮事件监听器
        btnAdd.addActionListener(e -> addRecord());
        btnEdit.addActionListener(e -> editRecord());
        btnDelete.addActionListener(e -> deleteRecord());
        btnRefresh.addActionListener(e -> refreshRecord());
        btnBack.addActionListener(e -> back());

        return panel;
    }

    // 清空表格
    private void clearTable() {
        tableModel.setRowCount(0);
    }

    // 加载当前预约记录
    private void loadCurrentReservationData() {
        if (currentReservationId <= 0) {
            clearTable();
            return;
        }

        clearTable();
        String tableName = rdoPermanent.isSelected() ? "permanent_private_spaces" : "private_space_reservations";
        String sql = "SELECT * FROM " + tableName + " WHERE reservation_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentReservationId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("reservation_id"));
                row.add(rs.getInt("space_id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("gender"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("license_plate"));
                row.add(rs.getString("vehicle_color"));
                row.add(rs.getInt("vehicle_id"));
                row.add(rs.getString("status"));
                row.add(rs.getTimestamp("start_time"));
                row.add(rs.getTimestamp("end_time"));
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据加载失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 搜索记录（新增）
    private void searchRecord() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入搜索关键字", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        clearTable();
        String tableName = rdoPermanent.isSelected() ? "permanent_private_spaces" : "private_space_reservations";
        String sql = "SELECT * FROM " + tableName + " WHERE " +
                "reservation_id = ? OR name LIKE ? OR license_plate LIKE ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 尝试按ID搜索
            try {
                int searchId = Integer.parseInt(keyword);
                pstmt.setInt(1, searchId);
                pstmt.setString(2, "%" + keyword + "%");
                pstmt.setString(3, "%" + keyword + "%");
            } catch (NumberFormatException e) {
                pstmt.setInt(1, -1);
                pstmt.setString(2, "%" + keyword + "%");
                pstmt.setString(3, "%" + keyword + "%");
            }

            ResultSet rs = pstmt.executeQuery();
            boolean found = false;

            while (rs.next()) {
                found = true;
                currentReservationId = rs.getInt("reservation_id");
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("reservation_id"));
                row.add(rs.getInt("space_id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("gender"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("license_plate"));
                row.add(rs.getString("vehicle_color"));
                row.add(rs.getInt("vehicle_id"));
                row.add(rs.getString("status"));
                row.add(rs.getTimestamp("start_time"));
                row.add(rs.getTimestamp("end_time"));
                tableModel.addRow(row);
            }

            if (!found) {
                JOptionPane.showMessageDialog(this, "未找到匹配的预约记录", "搜索结果", JOptionPane.INFORMATION_MESSAGE);
                currentReservationId = -1;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "搜索失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 添加记录
    private void addRecord() {
        String spaceId = txtSpaceId.getText();
        String name = txtName.getText();
        String gender = (String) cmbGender.getSelectedItem();
        String phone = txtPhone.getText();
        String licensePlate = txtLicensePlate.getText();
        String vehicleColor = txtVehicleColor.getText();
        String vehicleIdStr = txtVehicleId.getText();
        String status = (String) cmbStatus.getSelectedItem();
        boolean isPermanent = chkIsPermanent.isSelected();

        // 验证必填字段
        if (spaceId.isEmpty() || name.isEmpty() || phone.isEmpty() || licensePlate.isEmpty() || vehicleColor.isEmpty() || vehicleIdStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写所有必填字段", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 验证车辆ID为有效整数
        int vehicleId;
        try {
            vehicleId = Integer.parseInt(vehicleIdStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "车辆ID必须是有效整数", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 获取时间选择器的值
        Date startDate = (Date) spnStartTime.getValue();
        Date endDate = (Date) spnEndTime.getValue();
        Timestamp startTime = new Timestamp(startDate.getTime());
        Timestamp endTime = new Timestamp(endDate.getTime());

        // 验证时间逻辑
        if (endTime.before(startTime)) {
            JOptionPane.showMessageDialog(this, "预约结束时间不能早于开始时间", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tableName = isPermanent ? "permanent_private_spaces" : "private_space_reservations";
        String sql = "INSERT INTO " + tableName + " (space_id, name, gender, phone, license_plate, vehicle_color, vehicle_id, status, start_time, end_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, spaceId);
            pstmt.setString(2, name);
            pstmt.setString(3, gender);
            pstmt.setString(4, phone);
            pstmt.setString(5, licensePlate);
            pstmt.setString(6, vehicleColor);
            pstmt.setInt(7, vehicleId);
            pstmt.setString(8, status);
            pstmt.setTimestamp(9, startTime);
            pstmt.setTimestamp(10, endTime);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // 获取生成的ID
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        currentReservationId = generatedKeys.getInt(1);
                        JOptionPane.showMessageDialog(this, "添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                        loadCurrentReservationData();
                        clearFormFields();
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "添加失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 编辑记录
    private void editRecord() {
        if (currentReservationId <= 0) {
            JOptionPane.showMessageDialog(this, "请先添加或搜索预约记录", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String spaceId = txtSpaceId.getText();
        String name = txtName.getText();
        String gender = (String) cmbGender.getSelectedItem();
        String phone = txtPhone.getText();
        String licensePlate = txtLicensePlate.getText();
        String vehicleColor = txtVehicleColor.getText();
        String vehicleIdStr = txtVehicleId.getText();
        String status = (String) cmbStatus.getSelectedItem();
        boolean isPermanent = chkIsPermanent.isSelected();

        // 验证必填字段
        if (spaceId.isEmpty() || name.isEmpty() || phone.isEmpty() || licensePlate.isEmpty() || vehicleColor.isEmpty() || vehicleIdStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写所有必填字段", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 验证车辆ID为有效整数
        int vehicleId;
        try {
            vehicleId = Integer.parseInt(vehicleIdStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "车辆ID必须是有效整数", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 获取时间选择器的值
        Date startDate = (Date) spnStartTime.getValue();
        Date endDate = (Date) spnEndTime.getValue();
        Timestamp startTime = new Timestamp(startDate.getTime());
        Timestamp endTime = new Timestamp(endDate.getTime());

        // 验证时间逻辑
        if (endTime.before(startTime)) {
            JOptionPane.showMessageDialog(this, "预约结束时间不能早于开始时间", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tableName = isPermanent ? "permanent_private_spaces" : "private_space_reservations";
        String sql = "UPDATE " + tableName + " SET space_id=?, name=?, gender=?, phone=?, license_plate=?, vehicle_color=?, vehicle_id=?, status=?, start_time=?, end_time=? WHERE reservation_id=?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, spaceId);
            pstmt.setString(2, name);
            pstmt.setString(3, gender);
            pstmt.setString(4, phone);
            pstmt.setString(5, licensePlate);
            pstmt.setString(6, vehicleColor);
            pstmt.setInt(7, vehicleId);
            pstmt.setString(8, status);
            pstmt.setTimestamp(9, startTime);
            pstmt.setTimestamp(10, endTime);
            pstmt.setInt(11, currentReservationId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "修改成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadCurrentReservationData();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "修改失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 删除记录
    private void deleteRecord() {
        if (currentReservationId <= 0) {
            JOptionPane.showMessageDialog(this, "请先选择预约记录", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "确认要删除当前预约记录吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String tableName = rdoPermanent.isSelected() ? "permanent_private_spaces" : "private_space_reservations";
        String sql = "DELETE FROM " + tableName + " WHERE reservation_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentReservationId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                currentReservationId = -1;
                clearTable();
                clearFormFields();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "删除失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 刷新记录
    private void refreshRecord() {
        if (currentReservationId > 0) {
            loadCurrentReservationData();
        }
    }

    private void back() {
        currentReservationId = -1;
        clearTable();
        clearFormFields();
    }

    private void clearFormFields() {
        txtSpaceId.setText("");
        txtName.setText("");
        cmbGender.setSelectedIndex(0);
        txtPhone.setText("");
        txtLicensePlate.setText("");
        txtVehicleColor.setText("");
        txtVehicleId.setText("");
        txtSearch.setText(""); // 清空搜索框
        cmbStatus.setSelectedIndex(0);
        chkIsPermanent.setSelected(false);
        // 重置时间选择器为当前时间
        spnStartTime.setValue(new Date());
        spnEndTime.setValue(new Date());
    }
}