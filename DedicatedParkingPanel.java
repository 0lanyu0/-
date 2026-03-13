package com.parkingmanagement.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class DedicatedParkingPanel extends JPanel {
    // 数据库连接信息
    private static final String DB_URL = "jdbc:mysql://localhost:3306/parking_management?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "2004";

    // 输入控件
    private JTextField txtSpaceId, txtName, txtGender, txtPhone, txtLicensePlate, txtVehicleColor;
    private JCheckBox chkIsPermanent;
    private JRadioButton rdoDedicated, rdoPermanent;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;

    public DedicatedParkingPanel() {
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
        txtGender = new JTextField(15);
        panel.add(txtGender, gbc);

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

        // 是否永久专用车位
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("是否永久专用："), gbc);
        gbc.gridx = 1;
        chkIsPermanent = new JCheckBox();
        panel.add(chkIsPermanent, gbc);

        // 新增保存按钮
        gbc.gridx = 2;
        gbc.gridy = 3;
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
        rdoDedicated = new JRadioButton("专用车位信息", true);
        rdoPermanent = new JRadioButton("永久专用车位信息");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rdoDedicated);
        bg.add(rdoPermanent);
        switchPanel.add(rdoDedicated);
        switchPanel.add(rdoPermanent);
        panel.add(switchPanel, BorderLayout.NORTH);

        // 创建表格
        String[] dedicatedColumns = {"编号", "车位ID", "姓名", "性别", "电话", "车牌号", "车辆颜色", "创建时间", "更新时间"};
        String[] permanentColumns = {"编号", "车位ID", "姓名", "性别", "电话", "车牌号", "车辆颜色", "停车卡", "创建时间", "更新时间"};

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
        tableModel.setColumnIdentifiers(new String[]{"编号", "车位ID", "姓名", "性别", "电话", "车牌号", "车辆颜色", "创建时间", "更新时间"});

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM dedicated_owners")) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("owner_id"));
                row.add(rs.getInt("space_id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("gender"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("license_plate"));
                row.add(rs.getString("vehicle_color"));
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
        tableModel.setColumnIdentifiers(new String[]{"编号", "车位ID", "姓名", "性别", "电话", "车牌号", "车辆颜色", "停车卡", "创建时间", "更新时间"});

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM permanent_owners")) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("owner_id"));
                row.add(rs.getInt("space_id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("gender"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("license_plate"));
                row.add(rs.getString("vehicle_color"));
                row.add(rs.getBoolean("authorized_only") ? "固定专用" : "可共用");
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
        String gender = txtGender.getText();
        String phone = txtPhone.getText();
        String licensePlate = txtLicensePlate.getText();
        String vehicleColor = txtVehicleColor.getText();
        boolean isPermanent = chkIsPermanent.isSelected();

        if (spaceId.isEmpty() || name.isEmpty() || gender.isEmpty() || phone.isEmpty() || licensePlate.isEmpty() || vehicleColor.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写所有必填字段", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = isPermanent ?
                "INSERT INTO permanent_owners (space_id, name, gender, phone, license_plate, vehicle_color, authorized_only) VALUES (?, ?, ?, ?, ?, ?, ?)" :
                "INSERT INTO dedicated_owners (space_id, name, gender, phone, license_plate, vehicle_color) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, spaceId);
            pstmt.setString(2, name);
            pstmt.setString(3, gender);
            pstmt.setString(4, phone);
            pstmt.setString(5, licensePlate);
            pstmt.setString(6, vehicleColor);

            if (isPermanent) {
                pstmt.setBoolean(7, true); // 默认固定专用
            }

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
        int ownerId = (int) tableModel.getValueAt(rowIndex, 0);
        int spaceId = (int) tableModel.getValueAt(rowIndex, 1);
        String name = (String) tableModel.getValueAt(rowIndex, 2);
        String gender = (String) tableModel.getValueAt(rowIndex, 3);
        String phone = (String) tableModel.getValueAt(rowIndex, 4);
        String licensePlate = (String) tableModel.getValueAt(rowIndex, 5);
        String vehicleColor = (String) tableModel.getValueAt(rowIndex, 6);

        // 填充到输入框
        txtSpaceId.setText(String.valueOf(spaceId));
        txtName.setText(name);
        txtGender.setText(gender);
        txtPhone.setText(phone);
        txtLicensePlate.setText(licensePlate);
        txtVehicleColor.setText(vehicleColor);

        // 判断是否为永久专用车位
        if (tableModel.getColumnCount() > 7 && tableModel.getValueAt(rowIndex, 7) != null) {
            String parkingCard = (String) tableModel.getValueAt(rowIndex, 7);
            chkIsPermanent.setSelected(parkingCard.equals("固定专用"));
        } else {
            // 专用车位默认非永久专用
            chkIsPermanent.setSelected(false);
        }
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

        int ownerId = (int) tableModel.getValueAt(selectedRow, 0);
        boolean isPermanent = rdoPermanent.isSelected();

        String sql = isPermanent ?
                "DELETE FROM permanent_owners WHERE owner_id=?" :
                "DELETE FROM dedicated_owners WHERE owner_id=?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ownerId);
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
                "SELECT * FROM permanent_owners WHERE name LIKE ? OR license_plate LIKE ?" :
                "SELECT * FROM dedicated_owners WHERE name LIKE ? OR license_plate LIKE ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("owner_id"));
                    row.add(rs.getInt("space_id"));
                    row.add(rs.getString("name"));
                    row.add(rs.getString("gender"));
                    row.add(rs.getString("phone"));
                    row.add(rs.getString("license_plate"));
                    row.add(rs.getString("vehicle_color"));

                    if (isPermanent) {
                        row.add(rs.getBoolean("authorized_only") ? "固定专用" : "可共用");
                    }

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
        txtGender.setText("");
        txtPhone.setText("");
        txtLicensePlate.setText("");
        txtVehicleColor.setText("");
        chkIsPermanent.setSelected(false);
    }

    private void saveRecord() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要修改的记录", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String spaceId = txtSpaceId.getText();
        String name = txtName.getText();
        String gender = txtGender.getText();
        String phone = txtPhone.getText();
        String licensePlate = txtLicensePlate.getText();
        String vehicleColor = txtVehicleColor.getText();
        boolean isPermanent = chkIsPermanent.isSelected();

        if (spaceId.isEmpty() || name.isEmpty() || gender.isEmpty() || phone.isEmpty() || licensePlate.isEmpty() || vehicleColor.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写所有必填字段", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ownerId = (int) tableModel.getValueAt(selectedRow, 0);
        String sql = isPermanent ?
                "UPDATE permanent_owners SET space_id=?, name=?, gender=?, phone=?, license_plate=?, vehicle_color=?, authorized_only=? WHERE owner_id=?" :
                "UPDATE dedicated_owners SET space_id=?, name=?, gender=?, phone=?, license_plate=?, vehicle_color=? WHERE owner_id=?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, spaceId);
            pstmt.setString(2, name);
            pstmt.setString(3, gender);
            pstmt.setString(4, phone);
            pstmt.setString(5, licensePlate);
            pstmt.setString(6, vehicleColor);

            if (isPermanent) {
                pstmt.setBoolean(7, true);
                pstmt.setInt(8, ownerId);
            } else {
                pstmt.setInt(7, ownerId);
            }

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