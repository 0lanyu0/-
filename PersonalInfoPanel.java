package com.parkingmanagement.ui;

import com.parkingmanagement.dao.VehicleDAO;
import com.parkingmanagement.model.Vehicle;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PersonalInfoPanel extends JPanel {
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    // 输入控件
    private JTextField txtPlateNumber;
    private JTextField txtColor;
    private JTextField txtOwnerName;
    private JComboBox<String> cboGender;
    private JTextField txtContact;
    private JTable table;
    private VehicleTableModel tableModel;
    // 当前选中的车辆，用于编辑时
    private Vehicle selectedVehicle;
    // 搜索框
    private JTextField txtSearch;

    public PersonalInfoPanel() {
        setLayout(new BorderLayout());
        initializeControls();
    }

    private void initializeControls() {
        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        // 车牌号
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("车牌号:"), gbc);
        gbc.gridx = 1;
        txtPlateNumber = new JTextField(20);
        formPanel.add(txtPlateNumber, gbc);
        // 颜色
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("颜色:"), gbc);
        gbc.gridx = 1;
        txtColor = new JTextField(20);
        formPanel.add(txtColor, gbc);
        // 车主姓名
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("车主姓名:"), gbc);
        gbc.gridx = 1;
        txtOwnerName = new JTextField(20);
        formPanel.add(txtOwnerName, gbc);
        // 性别
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("性别:"), gbc);
        gbc.gridx = 1;
        cboGender = new JComboBox<>(new String[]{"男", "女", "未知"});
        formPanel.add(cboGender, gbc);
        // 联系方式
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("联系方式:"), gbc);
        gbc.gridx = 1;
        txtContact = new JTextField(20);
        formPanel.add(txtContact, gbc);
        // 按钮面板
        JPanel btnPanel = new JPanel();
        JButton btnAdd = new JButton("添加");
        JButton btnUpdate = new JButton("修改");
        JButton btnDelete = new JButton("删除");
        JButton btnSearch = new JButton("搜索");
        JButton btnReset = new JButton("重置");
        txtSearch = new JTextField(20);
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(new JLabel("关键字:"));
        btnPanel.add(txtSearch);
        btnPanel.add(btnSearch);
        btnPanel.add(btnReset);
        // 添加事件监听器
        btnAdd.addActionListener(e -> saveVehicle());
        btnUpdate.addActionListener(e -> updateVehicle());
        btnDelete.addActionListener(e -> deleteVehicle());
        btnSearch.addActionListener(e -> searchVehicles(txtSearch.getText()));
        txtSearch.addActionListener(e -> searchVehicles(txtSearch.getText()));
        btnReset.addActionListener(e -> clearForm());
        // 表格模型
        tableModel = new VehicleTableModel();
        table = new JTable((TableModel) tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectVehicle();
            }
        });
        // 布局添加
        add(formPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // 保存/添加车辆
    private void saveVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setPlateNumber(txtPlateNumber.getText());
        vehicle.setColor(txtColor.getText());
        vehicle.setOwnerName(txtOwnerName.getText());
        vehicle.setGender((String) cboGender.getSelectedItem());
        vehicle.setContact(txtContact.getText());
        vehicle.setCreateTime(LocalDateTime.now());
        vehicle.setUpdateTime(LocalDateTime.now());
        if (vehicleDAO.addVehicle(vehicle)) {
            JOptionPane.showMessageDialog(this, "车辆信息添加成功!");
            clearForm();
            searchVehicles(vehicle.getPlateNumber());
        } else {
            JOptionPane.showMessageDialog(this, "添加失败，请检查输入!", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 修改车辆
    private void updateVehicle() {
        if (selectedVehicle == null) {
            JOptionPane.showMessageDialog(this, "请选择要修改的记录!");
            return;
        }
        selectedVehicle.setPlateNumber(txtPlateNumber.getText());
        selectedVehicle.setColor(txtColor.getText());
        selectedVehicle.setOwnerName(txtOwnerName.getText());
        selectedVehicle.setGender((String) cboGender.getSelectedItem());
        selectedVehicle.setContact(txtContact.getText());
        selectedVehicle.setUpdateTime(LocalDateTime.now());
        if (vehicleDAO.updateVehicle(selectedVehicle)) {
            JOptionPane.showMessageDialog(this, "车辆信息修改成功!");
            clearForm();
            searchVehicles(selectedVehicle.getPlateNumber());
        } else {
            JOptionPane.showMessageDialog(this, "修改失败，请检查输入!", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 删除车辆
    private void deleteVehicle() {
        if (selectedVehicle == null) {
            JOptionPane.showMessageDialog(this, "请选择要删除的记录!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确认删除所选车辆?", "确认删除", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (vehicleDAO.deleteVehicle(selectedVehicle.getVehicleId())) {
                JOptionPane.showMessageDialog(this, "车辆信息删除成功!");
                clearForm();
                tableModel.setVehicles(new ArrayList<>());
            } else {
                JOptionPane.showMessageDialog(this, "删除失败，请稍后重试!", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 搜索车辆
    private void searchVehicles(String keyword) {
        List<Vehicle> vehicles = vehicleDAO.searchVehicles(keyword);
        tableModel.setVehicles(vehicles);
    }

    // 选择车辆时填充表单
    private void selectVehicle() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            selectedVehicle = tableModel.getVehicleAt(selectedRow);
            txtPlateNumber.setText(selectedVehicle.getPlateNumber());
            txtColor.setText(selectedVehicle.getColor());
            txtOwnerName.setText(selectedVehicle.getOwnerName());
            cboGender.setSelectedItem(selectedVehicle.getGender());
            txtContact.setText(selectedVehicle.getContact());
        } else {
            selectedVehicle = null;
        }
    }

    // 清空表单
    private void clearForm() {
        txtPlateNumber.setText("");
        txtColor.setText("");
        txtOwnerName.setText("");
        cboGender.setSelectedIndex(0);
        txtContact.setText("");
        selectedVehicle = null;
        table.clearSelection();
        tableModel.setVehicles(new ArrayList<>());
    }

    // 表格模型（内部类）
    private static class VehicleTableModel extends AbstractTableModel {
        private List<Vehicle> vehicles = new ArrayList<>();
        private String[] columns = {"ID", "车牌号", "颜色", "车主姓名", "性别", "联系方式", "创建时间", "更新时间"};

        public void setVehicles(List<Vehicle> vehicles) {
            this.vehicles = vehicles;
            fireTableDataChanged();
        }

        public Vehicle getVehicleAt(int row) {
            return vehicles.get(row);
        }

        @Override
        public int getRowCount() {
            return vehicles.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            Vehicle vehicle = vehicles.get(row);
            switch (column) {
                case 0:
                    return vehicle.getVehicleId();
                case 1:
                    return vehicle.getPlateNumber();
                case 2:
                    return vehicle.getColor();
                case 3:
                    return vehicle.getOwnerName();
                case 4:
                    return vehicle.getGender();
                case 5:
                    return vehicle.getContact();
                case 6:
                    return vehicle.getCreateTime();
                case 7:
                    return vehicle.getUpdateTime();
                default:
                    return null;
            }
        }
    }
}