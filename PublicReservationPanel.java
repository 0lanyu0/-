package com.parkingmanagement.ui;

import com.parkingmanagement.dao.ReservationDAO;
import com.parkingmanagement.model.ParkingSpace;
import com.parkingmanagement.model.Reservation;
import com.parkingmanagement.service.ParkingService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class PublicReservationPanel extends JPanel {
    private JTable reservationTable;
    private DefaultTableModel tableModel;
    private ReservationDAO reservationDAO;
    private ParkingService parkingService;
    private JTextField plateNumberField; // 车牌号输入框

    public PublicReservationPanel() {
        setLayout(new BorderLayout());
        reservationDAO = new ReservationDAO();
        parkingService = new ParkingService();
        initializeSearchPanel();
        initializeTable();
        initializeButtons();
        loadReservations("");
    }

    private void initializeSearchPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        // 搜索功能
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("车牌号:"));
        plateNumberField = new JTextField(15);
        JButton searchButton = new JButton("查询我的预约");
        searchButton.addActionListener(e -> {
            String plateNumber = plateNumberField.getText().trim();
            loadReservations(plateNumber);
        });
        searchPanel.add(plateNumberField);
        searchPanel.add(searchButton);
        topPanel.add(searchPanel, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);
    }

    private void initializeTable() {
        String[] columnNames = {"ID", "车牌号", "车主姓名", "性别", "联系电话", "车位编号", "开始时间", "结束时间", "状态", "创建时间"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reservationTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(reservationTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void initializeButtons() {
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("添加预约");
        JButton editButton = new JButton("编辑预约");
        JButton cancelButton = new JButton("取消预约");
        JButton refreshButton = new JButton("刷新");
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);
        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        cancelButton.addActionListener(e -> cancelReservation());
        refreshButton.addActionListener(e -> loadReservations(plateNumberField.getText().trim()));
    }

    private void loadReservations(String plateNumber) {
        tableModel.setRowCount(0);
        if (plateNumber.isEmpty()) {
            // 显示空状态
            return;
        }

        List<Reservation> reservations = reservationDAO.getReservationsByPlateNumber(plateNumber);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Reservation reservation : reservations) {
            tableModel.addRow(new Object[]{
                    reservation.getReservationId(),
                    reservation.getPlateNumber(),
                    reservation.getName(),
                    reservation.getGender(),
                    reservation.getPhone(),
                    reservation.getSpaceNumber(),
                    reservation.getStartTime().format(formatter),
                    reservation.getEndTime().format(formatter),
                    reservation.getStatus(),
                    reservation.getCreateTime().format(formatter)
            });
        }
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "添加预约", true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        JPanel panel = new JPanel(new GridLayout(8, 2, 10, 10));
        JTextField plateNumberField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField genderField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField colorField = new JTextField();
        List<ParkingSpace> availableSpaces = reservationDAO.getAvailableParkingSpaces();
        JComboBox<ParkingSpace> spaceComboBox = new JComboBox<>();
        spaceComboBox.addItem(null);
        for (ParkingSpace space : availableSpaces) {
            spaceComboBox.addItem(space);
        }
        JSpinner startSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner endSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startSpinner, "yyyy-MM-dd HH:mm");
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endSpinner, "yyyy-MM-dd HH:mm");
        startSpinner.setEditor(startEditor);
        endSpinner.setEditor(endEditor);
        panel.add(new JLabel("车牌号:"));
        panel.add(plateNumberField);
        panel.add(new JLabel("车主姓名:"));
        panel.add(nameField);
        panel.add(new JLabel("性别:"));
        panel.add(genderField);
        panel.add(new JLabel("联系电话:"));
        panel.add(phoneField);
        panel.add(new JLabel("车辆颜色:"));
        panel.add(colorField);
        panel.add(new JLabel("选择车位:"));
        panel.add(spaceComboBox);
        panel.add(new JLabel("开始时间:"));
        panel.add(startSpinner);
        panel.add(new JLabel("结束时间:"));
        panel.add(endSpinner);
        JButton saveButton = new JButton("添加预订");
        saveButton.addActionListener(e -> {
            String plateNumber = plateNumberField.getText().trim();
            String name = nameField.getText().trim();
            String gender = genderField.getText().trim();
            String phone = phoneField.getText().trim();
            String color = colorField.getText().trim();
            ParkingSpace selectedSpace = (ParkingSpace) spaceComboBox.getSelectedItem();
            LocalDateTime startTime = new java.sql.Timestamp(
                    ((java.util.Date) startSpinner.getValue()).getTime()).toLocalDateTime();
            LocalDateTime endTime = new java.sql.Timestamp(
                    ((java.util.Date) endSpinner.getValue()).getTime()).toLocalDateTime();
            if (plateNumber.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入车牌号", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入车主姓名", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (selectedSpace == null) {
                JOptionPane.showMessageDialog(dialog, "请选择可用车位", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (startTime.isAfter(endTime)) {
                JOptionPane.showMessageDialog(dialog, "结束时间不能早于开始时间", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (startTime.isBefore(LocalDateTime.now())) {
                JOptionPane.showMessageDialog(dialog, "开始时间不能早于当前时间", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                boolean vehicleExists = parkingService.checkVehicleExists(plateNumber);
                if (vehicleExists) {
                    // 车辆已存在，直接添加预约
                    Reservation reservation = new Reservation();
                    reservation.setPlateNumber(plateNumber);
                    reservation.setName(name);
                    reservation.setGender(gender);
                    reservation.setPhone(phone);
                    reservation.setColor(color);
                    reservation.setSpaceId(selectedSpace.getSpaceId());
                    reservation.setSpaceNumber(selectedSpace.getSpaceNumber());
                    reservation.setStartTime(startTime);
                    reservation.setEndTime(endTime);
                    reservation.setStatus(Reservation.STATUS_RESERVED);
                    parkingService.addReservation(reservation);
                    JOptionPane.showMessageDialog(dialog, "车位预约成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    loadReservations(plateNumber);
                    dialog.dispose();
                } else {
                    // 车辆不存在，先添加车辆信息
                    parkingService.addVehicle(plateNumber, name, gender, phone, color);
                    JOptionPane.showMessageDialog(dialog, "该车辆已登记，请再次尝试预约", "提示", JOptionPane.INFORMATION_MESSAGE);
                    // 清空字段，方便用户再次输入
                    nameField.setText("");
                    genderField.setText("");
                    phoneField.setText("");
                    colorField.setText("");
                    spaceComboBox.setSelectedIndex(0);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "操作失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showEditDialog() {
        int selectedRow = reservationTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要编辑的预订", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int reservationId = (int) tableModel.getValueAt(selectedRow, 0);
        Reservation reservation = reservationDAO.getReservationById(reservationId);
        if (reservation == null) {
            JOptionPane.showMessageDialog(this, "所选预订不存在", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "编辑预订", true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        JPanel panel = new JPanel(new GridLayout(8, 2, 10, 10));
        JTextField plateNumberField = new JTextField(reservation.getPlateNumber());
        plateNumberField.setEditable(false); // 车牌号不可编辑
        JTextField nameField = new JTextField(reservation.getName());
        JTextField genderField = new JTextField(reservation.getGender());
        JTextField phoneField = new JTextField(reservation.getPhone());
        JTextField colorField = new JTextField(reservation.getColor());
        List<ParkingSpace> availableSpaces = reservationDAO.getAvailableParkingSpaces();
        boolean hasCurrentSpace = false;
        for (ParkingSpace space : availableSpaces) {
            if (space.getSpaceId() == reservation.getSpaceId()) {
                hasCurrentSpace = true;
                break;
            }
        }
        if (!hasCurrentSpace) {
            ParkingSpace currentSpace = new ParkingSpace();
            currentSpace.setSpaceId(reservation.getSpaceId());
            currentSpace.setSpaceNumber(reservation.getSpaceNumber());
            availableSpaces.add(currentSpace);
        }
        JComboBox<ParkingSpace> spaceComboBox = new JComboBox<>();
        for (ParkingSpace space : availableSpaces) {
            spaceComboBox.addItem(space);
            if (space.getSpaceId() == reservation.getSpaceId()) {
                spaceComboBox.setSelectedItem(space);
            }
        }
        JSpinner startSpinner = new JSpinner(new SpinnerDateModel(
                java.sql.Timestamp.valueOf(reservation.getStartTime()), null, null, java.util.Calendar.HOUR_OF_DAY));
        JSpinner endSpinner = new JSpinner(new SpinnerDateModel(
                java.sql.Timestamp.valueOf(reservation.getEndTime()), null, null, java.util.Calendar.HOUR_OF_DAY));
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startSpinner, "yyyy-MM-dd HH:mm");
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endSpinner, "yyyy-MM-dd HH:mm");
        startSpinner.setEditor(startEditor);
        endSpinner.setEditor(endEditor);
        panel.add(new JLabel("车牌号:"));
        panel.add(plateNumberField);
        panel.add(new JLabel("车主姓名:"));
        panel.add(nameField);
        panel.add(new JLabel("性别:"));
        panel.add(genderField);
        panel.add(new JLabel("联系电话:"));
        panel.add(phoneField);
        panel.add(new JLabel("车辆颜色:"));
        panel.add(colorField);
        panel.add(new JLabel("选择车位:"));
        panel.add(spaceComboBox);
        panel.add(new JLabel("开始时间:"));
        panel.add(startSpinner);
        panel.add(new JLabel("结束时间:"));
        panel.add(endSpinner);
        JButton saveButton = new JButton("保存修改");
        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String gender = genderField.getText().trim();
            String phone = phoneField.getText().trim();
            String color = colorField.getText().trim();
            ParkingSpace selectedSpace = (ParkingSpace) spaceComboBox.getSelectedItem();
            LocalDateTime startTime = new java.sql.Timestamp(
                    ((java.util.Date) startSpinner.getValue()).getTime()).toLocalDateTime();
            LocalDateTime endTime = new java.sql.Timestamp(
                    ((java.util.Date) endSpinner.getValue()).getTime()).toLocalDateTime();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入车主姓名", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (selectedSpace == null) {
                JOptionPane.showMessageDialog(dialog, "请选择可用车位", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (startTime.isAfter(endTime)) {
                JOptionPane.showMessageDialog(dialog, "结束时间不能早于开始时间", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                boolean vehicleExists = parkingService.checkVehicleExists(reservation.getPlateNumber());
                if (!vehicleExists) {
                    // 如果车辆不存在，先添加车辆信息
                    parkingService.addVehicle(reservation.getPlateNumber(), name, gender, phone, color);
                }
                reservation.setName(name);
                reservation.setGender(gender);
                reservation.setPhone(phone);
                reservation.setColor(color);
                reservation.setSpaceId(selectedSpace.getSpaceId());
                reservation.setSpaceNumber(selectedSpace.getSpaceNumber());
                reservation.setStartTime(startTime);
                reservation.setEndTime(endTime);
                parkingService.updateReservation(reservation);
                JOptionPane.showMessageDialog(dialog, "预订修改成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadReservations(reservation.getPlateNumber());
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "预订修改失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void cancelReservation() {
        int selectedRow = reservationTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要取消的预订", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int reservationId = (int) tableModel.getValueAt(selectedRow, 0);
        Reservation reservation = reservationDAO.getReservationById(reservationId);
        if (reservation == null) {
            JOptionPane.showMessageDialog(this, "所选预订不存在", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!Reservation.STATUS_RESERVED.equals(reservation.getStatus())) {
            JOptionPane.showMessageDialog(this, "该预订状态不能取消", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmResult = JOptionPane.showConfirmDialog(
                this, "确认要取消该预订吗？", "确认取消", JOptionPane.YES_NO_OPTION);
        if (confirmResult == JOptionPane.YES_OPTION) {
            try {
                parkingService.cancelReservation(reservationId);
                JOptionPane.showMessageDialog(this, "预订已取消", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadReservations(plateNumberField.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "取消失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}