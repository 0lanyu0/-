package com.parkingmanagement.ui;

import com.parkingmanagement.dao.ParkingSpaceDAO;
import com.parkingmanagement.model.ParkingSpace;
import com.parkingmanagement.model.SpaceType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ParkingSpacePanel extends JPanel {
    private ParkingSpaceDAO spaceDAO;
    private JTable spaceTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    // 数据库连接信息
    private static final String DB_URL = "jdbc:mysql://localhost:3306/parking_management?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "2004";

    public ParkingSpacePanel() {
        setLayout(new BorderLayout());
        spaceDAO = new ParkingSpaceDAO();

        initializeUI();
        loadParkingSpaces();
    }

    private void initializeUI() {
        // 搜索面板
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        searchField = new JTextField(20);
        JButton searchButton = new JButton("搜索");
        JButton addButton = new JButton("添加车位");
        JButton editButton = new JButton("编辑车位");
        JButton deleteButton = new JButton("删除车位");
        JButton refreshButton = new JButton("刷新");

        searchButton.addActionListener(e -> searchParkingSpaces());
        addButton.addActionListener(e -> showAddSpaceDialog());
        editButton.addActionListener(e -> showEditSpaceDialog());
        deleteButton.addActionListener(e -> deleteSelectedSpace());
        refreshButton.addActionListener(e -> loadParkingSpaces());

        searchPanel.add(new JLabel("车位编号:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(addButton);
        searchPanel.add(editButton);
        searchPanel.add(deleteButton);
        searchPanel.add(refreshButton);

        // 中间表格面板
        String[] columnNames = {"车位ID", "车位编号", "车位类型", "车位状态", "是否可用"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        spaceTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(spaceTable);

        // 添加到主面板
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadParkingSpaces() {
        tableModel.setRowCount(0);
        List<ParkingSpace> spaces = spaceDAO.getAllParkingSpaces();

        for (ParkingSpace space : spaces) {
            tableModel.addRow(new Object[]{
                    space.getSpaceId(),
                    space.getSpaceNumber(),
                    space.getTypeName(),
                    space.isOccupied() ? "已占用" : "空闲",
                    space.isAvailable() ? "可用" : "不可用"
            });
        }
    }

    private void searchParkingSpaces() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadParkingSpaces();
            return;
        }

        tableModel.setRowCount(0);
        List<ParkingSpace> spaces = spaceDAO.searchParkingSpaces(keyword);

        for (ParkingSpace space : spaces) {
            tableModel.addRow(new Object[]{
                    space.getSpaceId(),
                    space.getSpaceNumber(),
                    space.getTypeName(),
                    space.isOccupied() ? "已占用" : "空闲",
                    space.isAvailable() ? "可用" : "不可用"
            });
        }
    }

    private void showAddSpaceDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "添加车位", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(5, 2, 10, 10));

        JTextField spaceNumberField = new JTextField();
        JComboBox<SpaceType> typeComboBox = new JComboBox<>(SpaceType.values());
        JCheckBox availableCheckBox = new JCheckBox();

        formPanel.add(new JLabel("车位编号:"));
        formPanel.add(spaceNumberField);
        formPanel.add(new JLabel("车位类型:"));
        formPanel.add(typeComboBox);
        formPanel.add(new JLabel("是否可用:"));
        formPanel.add(availableCheckBox);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(e -> {
            String spaceNumber = spaceNumberField.getText().trim();
            if (spaceNumber.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "车位编号不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SpaceType type = (SpaceType) typeComboBox.getSelectedItem();
            boolean available = availableCheckBox.isSelected();

            ParkingSpace space = new ParkingSpace();
            space.setSpaceNumber(spaceNumber);
            space.setTypeId(type.getId());
            space.setTypeName(type.getName());
            space.setAvailable(available);
            space.setOccupied(false);

            if (type == SpaceType.NORMAL || type == SpaceType.HANDICAPPED) {
                // 选择专用车位或永久专用车位，弹出新对话框输入信息并默认不可用
                showDedicatedInfoDialog(space, dialog);
            } else {
                if (spaceDAO.addParkingSpace(space)) {
                    JOptionPane.showMessageDialog(dialog, "车位添加成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    loadParkingSpaces();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "车位添加失败！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

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

                loadParkingSpaces();
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

    private void showEditSpaceDialog() {
        int selectedRow = spaceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要编辑的车位！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int spaceId = (int) tableModel.getValueAt(selectedRow, 0);
        ParkingSpace space = spaceDAO.getParkingSpaceById(spaceId);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "编辑车位", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(5, 2, 10, 10));

        JTextField spaceNumberField = new JTextField(space.getSpaceNumber());
        JComboBox<SpaceType> typeComboBox = new JComboBox<>(SpaceType.values());
        JCheckBox availableCheckBox = new JCheckBox();
        JCheckBox occupiedCheckBox = new JCheckBox();

        // 设置当前值
        for (int i = 0; i < typeComboBox.getItemCount(); i++) {
            if (typeComboBox.getItemAt(i).getId() == space.getTypeId()) {
                typeComboBox.setSelectedIndex(i);
                break;
            }
        }

        availableCheckBox.setSelected(space.isAvailable());
        occupiedCheckBox.setSelected(space.isOccupied());

        formPanel.add(new JLabel("车位编号:"));
        formPanel.add(spaceNumberField);
        formPanel.add(new JLabel("车位类型:"));
        formPanel.add(typeComboBox);
        formPanel.add(new JLabel("是否可用:"));
        formPanel.add(availableCheckBox);
        formPanel.add(new JLabel("是否占用:"));
        formPanel.add(occupiedCheckBox);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(e -> {
            String spaceNumber = spaceNumberField.getText().trim();
            if (spaceNumber.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "车位编号不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SpaceType type = (SpaceType) typeComboBox.getSelectedItem();
            boolean available = availableCheckBox.isSelected();
            boolean occupied = occupiedCheckBox.isSelected();

            space.setSpaceNumber(spaceNumber);
            space.setTypeId(type.getId());
            space.setTypeName(type.getName());
            space.setAvailable(available);
            space.setOccupied(occupied);

            if (spaceDAO.updateParkingSpace(space)) {
                JOptionPane.showMessageDialog(dialog, "车位更新成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadParkingSpaces();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "车位更新失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void deleteSelectedSpace() {
        int selectedRow = spaceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的车位！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "确认要删除选择的车位吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            int spaceId = (int) tableModel.getValueAt(selectedRow, 0);
            if (spaceDAO.deleteParkingSpace(spaceId)) {
                JOptionPane.showMessageDialog(this, "车位删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadParkingSpaces();
            } else {
                JOptionPane.showMessageDialog(this, "车位删除失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}