package com.parkingmanagement.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class SpecialReservationManagementPanel extends JPanel {
    // 数据库连接信息
    private static final String DB_URL = "jdbc:mysql://localhost:3306/parking_management?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "2004";

    // 输入控件
    private JTextField txtSpaceId, txtName, txtPhone, txtLicensePlate, txtVehicleColor, txtVehicleId;
    private JComboBox<String> cmbGender;
    private JComboBox<String> cmbStatus;
    private JCheckBox chkIsPermanent;
    private JRadioButton rdoDedicated, rdoPermanent;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JSpinner spnStartTime, spnEndTime;

    public SpecialReservationManagementPanel() {
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

        // 初始化数据
        loadDedicatedData();
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

        // 是否永久专用车位
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("是否永久专用："), gbc);
        gbc.gridx = 1;
        chkIsPermanent = new JCheckBox();
        panel.add(chkIsPermanent, gbc);

        // 新增：预约时间选择器
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("预约开始时间："), gbc);
        gbc.gridx = 1;
        spnStartTime = new JSpinner(new SpinnerDateModel());
        spnStartTime.setEditor(new JSpinner.DateEditor(spnStartTime, "yyyy-MM-dd HH:mm:ss"));
        panel.add(spnStartTime, gbc);

        gbc.gridx = 2;
        gbc.gridy = 5;
        panel.add(new JLabel("预约结束时间："), gbc);
        gbc.gridx = 3;
        spnEndTime = new JSpinner(new SpinnerDateModel());
        spnEndTime.setEditor(new JSpinner.DateEditor(spnEndTime, "yyyy-MM-dd HH:mm:ss"));
        panel.add(spnEndTime, gbc);

        // 新增保存按钮
        gbc.gridx = 2;
        gbc.gridy = 4;
        JButton btnSave = new JButton("保存");
        panel.add(btnSave, gbc);

        // 保存按钮事件监听器
        btnSave.addActionListener(e -> saveRecord());

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 创建切换按钮
        JPanel switchPanel = new JPanel();
        rdoDedicated = new JRadioButton("专用车位预约信息", true);
        rdoPermanent = new JRadioButton("永久专用车位预约信息");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rdoDedicated);
        bg.add(rdoPermanent);
        switchPanel.add(rdoDedicated);
        switchPanel.add(rdoPermanent);
        panel.add(switchPanel, BorderLayout.NORTH);

        // 创建表格
        String[] dedicatedColumns = {"编号", "车位ID", "姓名", "性别", "电话", "车牌号", "车辆颜色", "车辆ID", "状态", "预约开始时间", "预约结束时间", "创建时间", "更新时间"};
        String[] permanentColumns = {"编号", "车位ID", "姓名", "性别", "电话", "车牌号", "车辆颜色", "车辆ID", "状态", "预约开始时间", "预约结束时间", "创建时间", "更新时间"};

        tableModel = new DefaultTableModel(dedicatedColumns, 0);
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
        rdoDedicated.addActionListener(e -> loadDedicatedData());
        rdoPermanent.addActionListener(e -> loadPermanentData());

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();

        JButton btnAdd = new JButton("添加信息");
        JButton btnEdit = new JButton("修改信息");
        JButton btnDelete = new JButton("删除信息");
        txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("查询");
        JButton btnRefresh = new JButton("刷新");
        JButton btnBack = new JButton("返回");

        panel.add(btnAdd);
        panel.add(btnEdit);
        panel.add(btnDelete);
        panel.add(new JLabel("查询关键字："));
        panel.add(txtSearch);
        panel.add(btnSearch);
        panel.add(btnRefresh);
        panel.add(btnBack);

        // 添加按钮事件监听器
        btnAdd.addActionListener(e -> addRecord());
        btnEdit.addActionListener(e -> editRecord());
        btnDelete.addActionListener(e -> deleteRecord());
        btnSearch.addActionListener(e -> searchRecords());
        btnRefresh.addActionListener(e -> refreshRecords());
        btnBack.addActionListener(e -> back());

        return panel;
    }

    private void loadDedicatedData() {
        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(new String[]{"编号", "车位ID", "姓名", "性别", "电话", "车牌号", "车辆颜色", "车辆ID", "状态", "预约开始时间", "预约结束时间", "创建时间", "更新时间"});

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM private_space_reservations")) {

            while (rs.next()) {
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
                row.add(rs.getTimestamp("create_time"));
                row.add(rs.getTimestamp("update_time"));
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据加载失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPermanentData() {
        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(new String[]{"编号", "车位ID", "姓名", "性别", "电话", "车牌号", "车辆颜色", "车辆ID", "状态", "预约开始时间", "预约结束时间", "创建时间", "更新时间"});

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM permanent_private_spaces")) {

            while (rs.next()) {
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
                row.add(rs.getTimestamp("create_time"));
                row.add(rs.getTimestamp("update_time"));
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据加载失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

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

        String sql = isPermanent ?
                "INSERT INTO permanent_private_spaces (space_id, name, gender, phone, license_plate, vehicle_color, vehicle_id, status, start_time, end_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" :
                "INSERT INTO private_space_reservations (space_id, name, gender, phone, license_plate, vehicle_color, vehicle_id, status, start_time, end_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);

            if (isPermanent) {
                loadPermanentData();
            } else {
                loadDedicatedData();
            }

            clearFormFields();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "添加失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editRecord() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要修改的记录", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 获取选中行的数据并填充到输入框
        fillFormFieldsWithSelectedRowData(selectedRow);

        // 显示修改提示信息
        JOptionPane.showMessageDialog(this, "请修改相关信息后，点击'保存'按钮保存", "修改提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void fillFormFieldsWithSelectedRowData(int rowIndex) {
        // 获取选中行的数据
        int reservationId = (int) tableModel.getValueAt(rowIndex, 0);
        int spaceId = (int) tableModel.getValueAt(rowIndex, 1);
        String name = (String) tableModel.getValueAt(rowIndex, 2);
        String gender = (String) tableModel.getValueAt(rowIndex, 3);
        String phone = (String) tableModel.getValueAt(rowIndex, 4);
        String licensePlate = (String) tableModel.getValueAt(rowIndex, 5);
        String vehicleColor = (String) tableModel.getValueAt(rowIndex, 6);
        int vehicleId = (int) tableModel.getValueAt(rowIndex, 7);
        String status = (String) tableModel.getValueAt(rowIndex, 8);
        Timestamp startTime = (Timestamp) tableModel.getValueAt(rowIndex, 9);
        Timestamp endTime = (Timestamp) tableModel.getValueAt(rowIndex, 10);

        // 填充到输入框
        txtSpaceId.setText(String.valueOf(spaceId));
        txtName.setText(name);
        cmbGender.setSelectedItem(gender);
        txtPhone.setText(phone);
        txtLicensePlate.setText(licensePlate);
        txtVehicleColor.setText(vehicleColor);
        txtVehicleId.setText(String.valueOf(vehicleId));
        cmbStatus.setSelectedItem(status);
        if (startTime != null) spnStartTime.setValue(startTime);
        if (endTime != null) spnEndTime.setValue(endTime);

        // 判断是否为永久专用车位
        chkIsPermanent.setSelected(rdoPermanent.isSelected());
    }

    private void deleteRecord() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的记录", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "确认要删除选中的记录吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        int reservationId = (int) tableModel.getValueAt(selectedRow, 0);
        boolean isPermanent = rdoPermanent.isSelected();

        String sql = isPermanent ?
                "DELETE FROM permanent_private_spaces WHERE reservation_id=?" :
                "DELETE FROM private_space_reservations WHERE reservation_id=?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reservationId);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);

            if (isPermanent) {
                loadPermanentData();
            } else {
                loadDedicatedData();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "删除失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchRecords() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            refreshRecords();
            return;
        }

        boolean isPermanent = rdoPermanent.isSelected();
        tableModel.setRowCount(0);

        String sql = isPermanent ?
                "SELECT * FROM permanent_private_spaces WHERE name LIKE ? OR license_plate LIKE ?" :
                "SELECT * FROM private_space_reservations WHERE name LIKE ? OR license_plate LIKE ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
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
                    row.add(rs.getTimestamp("create_time"));
                    row.add(rs.getTimestamp("update_time"));
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "查询失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshRecords() {
        if (rdoPermanent.isSelected()) {
            loadPermanentData();
        } else {
            loadDedicatedData();
        }
        txtSearch.setText("");
    }

    private void back() {
        clearFormFields();
        txtSearch.setText("");
    }

    private void clearFormFields() {
        txtSpaceId.setText("");
        txtName.setText("");
        cmbGender.setSelectedIndex(0);
        txtPhone.setText("");
        txtLicensePlate.setText("");
        txtVehicleColor.setText("");
        txtVehicleId.setText("");
        cmbStatus.setSelectedIndex(0);
        chkIsPermanent.setSelected(false);

        // 重置时间选择器为当前时间
        spnStartTime.setValue(new Date());
        spnEndTime.setValue(new Date());
    }

    private void saveRecord() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要修改的记录", "提示", JOptionPane.WARNING_MESSAGE);
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

        int reservationId = (int) tableModel.getValueAt(selectedRow, 0);
        String sql = isPermanent ?
                "UPDATE permanent_private_spaces SET space_id=?, name=?, gender=?, phone=?, license_plate=?, vehicle_color=?, vehicle_id=?, status=?, start_time=?, end_time=? WHERE reservation_id=?" :
                "UPDATE private_space_reservations SET space_id=?, name=?, gender=?, phone=?, license_plate=?, vehicle_color=?, vehicle_id=?, status=?, start_time=?, end_time=? WHERE reservation_id=?";

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
            pstmt.setInt(11, reservationId);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "保存成功", "成功", JOptionPane.INFORMATION_MESSAGE);

            if (isPermanent) {
                loadPermanentData();
            } else {
                loadDedicatedData();
            }

            clearFormFields();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}