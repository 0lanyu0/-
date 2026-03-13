package com.parkingmanagement.ui;

import com.parkingmanagement.dao.ParkingSpaceDAO;
import com.parkingmanagement.dao.VehicleDAO;
import com.parkingmanagement.model.ParkingSpace;
import com.parkingmanagement.model.Vehicle;
import com.parkingmanagement.model.SpaceType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ParkingSpaceManagementPanel extends JPanel {
    private JTable spaceTable;
    private DefaultTableModel tableModel;
    private ParkingSpaceDAO spaceDAO;
    private VehicleDAO vehicleDAO;
    private JComboBox<SpaceType> typeFilter;

    // 数据库连接信息
    private static final String DB_URL = "jdbc:mysql://localhost:3306/parking_management?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "2004";

    public ParkingSpaceManagementPanel() {
        setLayout(new BorderLayout());
        spaceDAO = new ParkingSpaceDAO();
        vehicleDAO = new VehicleDAO();

        initializeFilterPanel();
        initializeTable();
        initializeButtons();
        loadParkingSpaces(SpaceType.values()[0]);
    }

    private void initializeFilterPanel() {
        JPanel filterPanel = new JPanel();
        typeFilter = new JComboBox<>(SpaceType.values());
        typeFilter.setSelectedIndex(0);
        JButton refreshButton = new JButton("刷新");

        filterPanel.add(new JLabel("车位类型:"));
        filterPanel.add(typeFilter);
        filterPanel.add(refreshButton);
        add(filterPanel, BorderLayout.NORTH);

        typeFilter.addActionListener(e -> loadParkingSpaces((SpaceType) typeFilter.getSelectedItem()));
        refreshButton.addActionListener(e -> loadParkingSpaces((SpaceType) typeFilter.getSelectedItem()));
    }

    private void initializeTable() {
        String[] columnNames = {"ID", "车位编号", "类型", "状态", "占用车辆", "创建时间", "更新时间"};
        tableModel = new DefaultTableModel(columnNames, 0);
        spaceTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(spaceTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void initializeButtons() {
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("添加车位");
        JButton editButton = new JButton("修改车位");
        JButton deleteButton = new JButton("删除车位");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteParkingSpace());
    }

    private void loadParkingSpaces(SpaceType type) {
        tableModel.setRowCount(0);
        List<ParkingSpace> spaces = spaceDAO.getAllParkingSpaces();

        for (ParkingSpace space : spaces) {
            if (type == SpaceType.values()[0] || SpaceType.getById(space.getTypeId()) == type) {
                Integer vehicleId = space.getVehicleId();
                String vehicleInfo = vehicleId != null ?
                        vehicleDAO.getVehicleById(vehicleId).getPlateNumber() : "无";
                String status = space.isOccupied() ? "已占用" : (space.isAvailable() ? "可用" : "不可用");

                tableModel.addRow(new Object[]{
                        space.getSpaceId(),
                        space.getSpaceNumber(),
                        space.getTypeName(),
                        status,
                        vehicleInfo,
                        space.getCreateTime(),
                        space.getUpdateTime()
                });
            }
        }
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "添加车位", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        JTextField spaceNumberField = new JTextField();
        JComboBox<SpaceType> typeComboBox = new JComboBox<>(SpaceType.values());
        JComboBox<Vehicle> vehicleComboBox = new JComboBox<>();
        JCheckBox occupiedCheckBox = new JCheckBox("已占用");
        JCheckBox availableCheckBox = new JCheckBox("可用", true);

        // 加载所有车辆信息
        List<Vehicle> vehicles = vehicleDAO.getAllVehicles();
        vehicleComboBox.addItem(null); // 无选择
        for (Vehicle vehicle : vehicles) {
            vehicleComboBox.addItem(vehicle);
        }

        panel.add(new JLabel("车位编号:"));
        panel.add(spaceNumberField);
        panel.add(new JLabel("车位类型:"));
        panel.add(typeComboBox);
        panel.add(new JLabel("关联车辆:"));
        panel.add(vehicleComboBox);
        panel.add(new JLabel("状态:"));
        panel.add(occupiedCheckBox);
        panel.add(new JLabel("可用性:"));
        panel.add(availableCheckBox);

        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(e -> {
            String spaceNumber = spaceNumberField.getText().trim();
            SpaceType type = (SpaceType) typeComboBox.getSelectedItem();
            Vehicle selectedVehicle = (Vehicle) vehicleComboBox.getSelectedItem();
            boolean isOccupied = occupiedCheckBox.isSelected();
            boolean isAvailable = availableCheckBox.isSelected();

            if (spaceNumber.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "车位编号不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ParkingSpace space = new ParkingSpace();
            space.setSpaceNumber(spaceNumber);
            space.setTypeId(type.getId());
            space.setTypeName(type.getName());
            space.setOccupied(isOccupied);

            if (type == SpaceType.NORMAL || type == SpaceType.HANDICAPPED) {
                // 选择专用车位或永久专用车位，弹出新对话框输入信息并默认不可用
                showDedicatedInfoDialog(space, dialog);
            } else {
                space.setAvailable(isAvailable);
                space.setCreateTime(LocalDateTime.now());
                space.setUpdateTime(LocalDateTime.now());

                if (selectedVehicle != null) {
                    space.setVehicleId(selectedVehicle.getVehicleId());
                }

                spaceDAO.addParkingSpace(space);
                loadParkingSpaces((SpaceType) typeFilter.getSelectedItem());
                dialog.dispose();
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showDedicatedInfoDialog(ParkingSpace space, JDialog parentDialog) {
        JDialog dedicatedDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parentDialog), "专用车位信息录入", true);
        dedicatedDialog.setSize(400, 350);
        dedicatedDialog.setLocationRelativeTo(parentDialog);
        dedicatedDialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        JTextField nameField = new JTextField();
        JTextField genderField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField licensePlateField = new JTextField();
        JTextField vehicleColorField = new JTextField();
        JCheckBox isPermanentCheckBox = new JCheckBox("是否永久专用");

        panel.add(new JLabel("姓名:"));
        panel.add(nameField);
        panel.add(new JLabel("性别:"));
        panel.add(genderField);
        panel.add(new JLabel("电话:"));
        panel.add(phoneField);
        panel.add(new JLabel("车牌号:"));
        panel.add(licensePlateField);
        panel.add(new JLabel("车辆颜色:"));
        panel.add(vehicleColorField);
        panel.add(new JLabel("是否永久专用:"));
        panel.add(isPermanentCheckBox);

        JButton saveDedicatedButton = new JButton("保存");
        saveDedicatedButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String gender = genderField.getText().trim();
            String phone = phoneField.getText().trim();
            String licensePlate = licensePlateField.getText().trim();
            String vehicleColor = vehicleColorField.getText().trim();
            boolean isPermanent = isPermanentCheckBox.isSelected();

            if (name.isEmpty() || gender.isEmpty() || phone.isEmpty() || licensePlate.isEmpty() || vehicleColor.isEmpty()) {
                JOptionPane.showMessageDialog(dedicatedDialog, "请填写所有必填字段！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            space.setAvailable(false); // 默认不可用
            space.setCreateTime(LocalDateTime.now());
            space.setUpdateTime(LocalDateTime.now());

            spaceDAO.addParkingSpace(space);

            // 将专用车位信息存入对应数据表
            String sql = isPermanent ?
                    "INSERT INTO permanent_owners (space_id, name, gender, phone, license_plate, vehicle_color, authorized_only) VALUES (?, ?, ?, ?, ?, ?, ?)" :
                    "INSERT INTO dedicated_owners (space_id, name, gender, phone, license_plate, vehicle_color) VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, space.getSpaceId());
                pstmt.setString(2, name);
                pstmt.setString(3, gender);
                pstmt.setString(4, phone);
                pstmt.setString(5, licensePlate);
                pstmt.setString(6, vehicleColor);

                if (isPermanent) {
                    pstmt.setBoolean(7, true); // 默认永久专用
                }

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(dedicatedDialog, "信息保存成功！", "成功", JOptionPane.INFORMATION_MESSAGE);

                loadParkingSpaces((SpaceType) typeFilter.getSelectedItem());
                dedicatedDialog.dispose();
                parentDialog.dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dedicatedDialog, "信息保存失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        dedicatedDialog.add(panel, BorderLayout.CENTER);
        dedicatedDialog.add(saveDedicatedButton, BorderLayout.SOUTH);
        dedicatedDialog.setVisible(true);
    }

    private void showEditDialog() {
        int selectedRow = spaceTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要修改的车位！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int spaceId = (int) tableModel.getValueAt(selectedRow, 0);
        ParkingSpace space = spaceDAO.getParkingSpaceById(spaceId);
        if (space == null) {
            JOptionPane.showMessageDialog(this, "未找到该车位！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "修改车位", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        JTextField spaceNumberField = new JTextField(space.getSpaceNumber());
        JComboBox<SpaceType> typeComboBox = new JComboBox<>(SpaceType.values());
        JComboBox<Vehicle> vehicleComboBox = new JComboBox<>();
        JCheckBox occupiedCheckBox = new JCheckBox("已占用", space.isOccupied());
        JCheckBox availableCheckBox = new JCheckBox("可用", space.isAvailable());

        // 设置当前选择项
        for (SpaceType type : SpaceType.values()) {
            if (type.getId() == space.getTypeId()) {
                typeComboBox.setSelectedItem(type);
                break;
            }
        }

        // 加载所有车辆信息
        List<Vehicle> vehicles = vehicleDAO.getAllVehicles();
        vehicleComboBox.addItem(null); // 无选择
        for (Vehicle vehicle : vehicles) {
            vehicleComboBox.addItem(vehicle);
            Integer vehicleId = space.getVehicleId();
            if (vehicleId != null && vehicleId.equals(vehicle.getVehicleId())) {
                vehicleComboBox.setSelectedItem(vehicle);
            }
        }

        panel.add(new JLabel("车位编号:"));
        panel.add(spaceNumberField);
        panel.add(new JLabel("车位类型:"));
        panel.add(typeComboBox);
        panel.add(new JLabel("关联车辆:"));
        panel.add(vehicleComboBox);
        panel.add(new JLabel("状态:"));
        panel.add(occupiedCheckBox);
        panel.add(new JLabel("可用性:"));
        panel.add(availableCheckBox);

        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(e -> {
            String spaceNumber = spaceNumberField.getText().trim();
            SpaceType type = (SpaceType) typeComboBox.getSelectedItem();
            Vehicle selectedVehicle = (Vehicle) vehicleComboBox.getSelectedItem();
            boolean isOccupied = occupiedCheckBox.isSelected();
            boolean isAvailable = availableCheckBox.isSelected();

            if (spaceNumber.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "车位编号不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            space.setSpaceNumber(spaceNumber);
            space.setTypeId(type.getId());
            space.setTypeName(type.getName());
            space.setOccupied(isOccupied);
            space.setAvailable(isAvailable);
            space.setUpdateTime(LocalDateTime.now());

            if (selectedVehicle != null) {
                space.setVehicleId(selectedVehicle.getVehicleId());
            } else {
                space.setVehicleId(null);
            }

            spaceDAO.updateParkingSpace(space);
            loadParkingSpaces((SpaceType) typeFilter.getSelectedItem());
            dialog.dispose();
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteParkingSpace() {
        int selectedRow = spaceTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要删除的车位！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int spaceId = (int) tableModel.getValueAt(selectedRow, 0);
        ParkingSpace space = spaceDAO.getParkingSpaceById(spaceId);

        if (space.isOccupied()) {
            JOptionPane.showMessageDialog(this, "该车位当前已占用，无法删除！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "确认要删除车位 " + space.getSpaceNumber() + " 吗？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            spaceDAO.deleteParkingSpace(spaceId);
            loadParkingSpaces((SpaceType) typeFilter.getSelectedItem());
        }
    }
}