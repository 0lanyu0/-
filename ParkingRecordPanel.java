package com.parkingmanagement.ui;

import com.parkingmanagement.dao.ParkingRecordDAO;
import com.parkingmanagement.dao.ParkingSpaceDAO;
import com.parkingmanagement.dao.VehicleDAO;
import com.parkingmanagement.model.ParkingRecord;
import com.parkingmanagement.model.ParkingSpace;
import com.parkingmanagement.model.Vehicle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ParkingRecordPanel extends JPanel {
    private ParkingRecordDAO recordDAO;
    private ParkingSpaceDAO spaceDAO;
    private VehicleDAO vehicleDAO;
    private JTable recordTable;
    private DefaultTableModel tableModel;
    private JTextField licensePlateField;
    private JTextField dateFromField;
    private JTextField dateToField;

    public ParkingRecordPanel() {
        setLayout(new BorderLayout());
        recordDAO = new ParkingRecordDAO();
        spaceDAO = new ParkingSpaceDAO();
        vehicleDAO = new VehicleDAO();
        initializeUI();
        loadParkingRecords();
    }

    private void initializeUI() {
        // 顶部搜索面板
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        licensePlateField = new JTextField(10);
        dateFromField = new JTextField(10);
        dateToField = new JTextField(10);
        JButton searchButton = new JButton("搜索");
        JButton addButton = new JButton("入场登记");
        JButton exitButton = new JButton("出场登记");
        JButton editButton = new JButton("修改记录");
        JButton deleteButton = new JButton("删除记录");
        JButton refreshButton = new JButton("刷新");

        // 设置默认日期范围为今天
        LocalDateTime now = LocalDateTime.now();
        dateFromField.setText(now.toLocalDate().toString());
        dateToField.setText(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        searchButton.addActionListener(e -> searchParkingRecords());
        addButton.addActionListener(e -> showEntryDialog());
        exitButton.addActionListener(e -> showExitDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteRecord());
        refreshButton.addActionListener(e -> loadParkingRecords());

        searchPanel.add(new JLabel("车牌号:"));
        searchPanel.add(licensePlateField);
        searchPanel.add(new JLabel("开始日期:"));
        searchPanel.add(dateFromField);
        searchPanel.add(new JLabel("结束日期:"));
        searchPanel.add(dateToField);
        searchPanel.add(searchButton);
        searchPanel.add(addButton);
        searchPanel.add(exitButton);
        searchPanel.add(editButton);
        searchPanel.add(deleteButton);
        searchPanel.add(refreshButton);

        // 中间表格面板（添加支付状态列）
        String[] columnNames = {"记录ID", "车牌号", "车位编号", "入场时间", "出场时间", "停车时长", "费用", "状态", "支付状态"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recordTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(recordTable);

        // 添加到主面板
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadParkingRecords() {
        tableModel.setRowCount(0);
        List<ParkingRecord> records = recordDAO.getAllParkingRecords();
        for (ParkingRecord record : records) {
            String entryTime = record.getEntryTime() != null ?
                    record.getEntryTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
            String exitTime = record.getExitTime() != null ?
                    record.getExitTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
            String duration = record.getDuration() != null ?
                    formatDuration(record.getDuration()) : "";
            String fee = record.getFee() != null ?
                    String.format("%.2f", record.getFee()) : "未结算";
            String status = record.getExitTime() != null ? "已出场" : "在场";

            tableModel.addRow(new Object[]{
                    record.getRecordId(),
                    record.getLicensePlate(),
                    record.getSpaceNumber(),
                    entryTime,
                    exitTime,
                    duration,
                    fee,
                    status,
                    record.getPaymentStatus() // 显示支付状态
            });
        }
    }

    private void searchParkingRecords() {
        String licensePlate = licensePlateField.getText().trim();
        String dateFrom = dateFromField.getText().trim();
        String dateTo = dateToField.getText().trim();
        int vehicleId = -1;

        if (!licensePlate.isEmpty()) {
            Vehicle vehicle = vehicleDAO.getVehicleByPlateNumber(licensePlate);
            if (vehicle != null) {
                vehicleId = vehicle.getVehicleId();
            } else {
                JOptionPane.showMessageDialog(this, "未找到该车牌号的车辆记录", "提示", JOptionPane.INFORMATION_MESSAGE);
                loadParkingRecords();
                return;
            }
        }

        tableModel.setRowCount(0);
        List<ParkingRecord> records = recordDAO.searchParkingRecords(
                vehicleId != -1 ? vehicleId : null,
                dateFrom,
                dateTo
        );

        for (ParkingRecord record : records) {
            String entryTime = record.getEntryTime() != null ?
                    record.getEntryTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
            String exitTime = record.getExitTime() != null ?
                    record.getExitTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
            String duration = record.getDuration() != null ?
                    formatDuration(record.getDuration()) : "";
            String fee = record.getFee() != null ?
                    String.format("%.2f", record.getFee()) : "未结算";
            String status = record.getExitTime() != null ? "已出场" : "在场";

            tableModel.addRow(new Object[]{
                    record.getRecordId(),
                    record.getLicensePlate(),
                    record.getSpaceNumber(),
                    entryTime,
                    exitTime,
                    duration,
                    fee,
                    status,
                    record.getPaymentStatus() // 显示支付状态
            });
        }
    }

    private void showEntryDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "入场登记", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(4, 2, 10, 10));
        JTextField licensePlateField = new JTextField();
        JComboBox<ParkingSpace> spaceComboBox = new JComboBox<>();
        JTextField entryTimeField = new JTextField();

        // 设置当前时间
        entryTimeField.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 加载可用车位
        List<ParkingSpace> availableSpaces = spaceDAO.getAvailableParkingSpaces();
        for (ParkingSpace space : availableSpaces) {
            spaceComboBox.addItem(space);
        }

        if (spaceComboBox.getItemCount() == 0) {
            JOptionPane.showMessageDialog(dialog, "没有可用车位!", "提示", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
            return;
        }

        formPanel.add(new JLabel("车牌号:"));
        formPanel.add(licensePlateField);
        formPanel.add(new JLabel("车位选择:"));
        formPanel.add(spaceComboBox);
        formPanel.add(new JLabel("入场时间:"));
        formPanel.add(entryTimeField);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("登记");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(e -> {
            String licensePlate = licensePlateField.getText().trim();
            if (licensePlate.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "车牌号不能为空!", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ParkingSpace selectedSpace = (ParkingSpace) spaceComboBox.getSelectedItem();
            LocalDateTime entryTime;

            try {
                entryTime = LocalDateTime.parse(entryTimeField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "请输入正确的日期时间格式 (YYYY-MM-DD HH:MM:SS)!", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 检查车辆是否存在，不存在则创建
            Vehicle vehicle = vehicleDAO.getVehicleByPlateNumber(licensePlate);
            if (vehicle == null) {
                // 创建新车辆记录
                vehicle = new Vehicle();
                vehicle.setPlateNumber(licensePlate);
                vehicle.setCreateTime(LocalDateTime.now());
                vehicle.setUpdateTime(LocalDateTime.now());
                if (!vehicleDAO.addVehicle(vehicle)) {
                    JOptionPane.showMessageDialog(dialog, "车辆信息创建失败!", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // 重新获取带ID的车辆信息
                vehicle = vehicleDAO.getVehicleByPlateNumber(licensePlate);
                if (vehicle == null) {
                    JOptionPane.showMessageDialog(dialog, "无法获取车辆ID!", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // 创建停车记录
            ParkingRecord record = new ParkingRecord();
            record.setVehicleId(vehicle.getVehicleId());
            record.setSpaceId(selectedSpace.getSpaceId());
            record.setEntryTime(entryTime);
            record.setLicensePlate(licensePlate);
            record.setSpaceNumber(selectedSpace.getSpaceNumber());

            // 保存记录
            if (recordDAO.addParkingRecord(record)) {
                // 更新车位状态
                selectedSpace.setOccupied(true);
                spaceDAO.updateParkingSpace(selectedSpace);
                JOptionPane.showMessageDialog(dialog, "入场登记成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadParkingRecords();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "入场登记失败!", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showExitDialog() {
        int selectedRow = recordTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要处理的停车记录!", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int recordId = (int) tableModel.getValueAt(selectedRow, 0);
        ParkingRecord record = recordDAO.getParkingRecordById(recordId);

        if (record.getExitTime() != null) {
            JOptionPane.showMessageDialog(this, "该车辆已出场，无需重复处理!", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "出场登记", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(6, 2, 10, 10));
        JTextField licensePlateField = new JTextField(record.getLicensePlate());
        JTextField spaceNumberField = new JTextField(record.getSpaceNumber());
        JTextField entryTimeField = new JTextField(record.getEntryTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        JTextField exitTimeField = new JTextField();
        JTextField durationField = new JTextField();
        JTextField feeField = new JTextField();

        // 设置当前时间
        LocalDateTime exitTime = LocalDateTime.now();
        exitTimeField.setText(exitTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 计算停车时长和费用
        long durationSeconds = java.time.Duration.between(record.getEntryTime(), exitTime).getSeconds();
        double fee = calculateParkingFee(durationSeconds);
        durationField.setText(formatDuration(durationSeconds));
        feeField.setText(String.format("%.2f", fee));

        licensePlateField.setEditable(false);
        spaceNumberField.setEditable(false);
        entryTimeField.setEditable(false);
        durationField.setEditable(false);
        feeField.setEditable(false);

        formPanel.add(new JLabel("车牌号:"));
        formPanel.add(licensePlateField);
        formPanel.add(new JLabel("车位编号:"));
        formPanel.add(spaceNumberField);
        formPanel.add(new JLabel("入场时间:"));
        formPanel.add(entryTimeField);
        formPanel.add(new JLabel("出场时间:"));
        formPanel.add(exitTimeField);
        formPanel.add(new JLabel("停车时长:"));
        formPanel.add(durationField);
        formPanel.add(new JLabel("停车费用:"));
        formPanel.add(feeField);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("确认出场");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(e -> {
            try {
                LocalDateTime exitTimeInput = LocalDateTime.parse(exitTimeField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                // 更新停车记录
                record.setExitTime(exitTimeInput);
                record.setFee(fee);

                if (recordDAO.updateParkingRecord(record)) {
                    // 更新车位状态
                    ParkingSpace space = spaceDAO.getParkingSpaceById(record.getSpaceId());
                    if (space != null) {
                        space.setOccupied(false);
                        spaceDAO.updateParkingSpace(space);
                    }

                    JOptionPane.showMessageDialog(dialog, "出场登记成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                    loadParkingRecords();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "出场登记失败!", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "请输入正确的日期时间格式 (YYYY-MM-DD HH:MM:SS)!", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showEditDialog() {
        int selectedRow = recordTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要修改的记录!", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int recordId = (int) tableModel.getValueAt(selectedRow, 0);
        ParkingRecord record = recordDAO.getParkingRecordById(recordId);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "修改停车记录", true);
        dialog.setSize(400, 400); // 调整高度以容纳支付状态选项
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(7, 2, 10, 10)); // 增加为7行，包含支付状态行

        JTextField licensePlateField = new JTextField(record.getLicensePlate());
        JComboBox<ParkingSpace> spaceComboBox = new JComboBox<>();
        JTextField entryTimeField = new JTextField(record.getEntryTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        JTextField exitTimeField = new JTextField();
        JTextField durationField = new JTextField();
        JTextField feeField = new JTextField();

        // 支付状态选择框
        JComboBox<String> paymentStatusCombo = new JComboBox<>(new String[]{"未支付", "已支付"});
        paymentStatusCombo.setSelectedItem(record.getPaymentStatus());

        // 获取车位信息
        ParkingSpace currentSpace = spaceDAO.getParkingSpaceById(record.getSpaceId());
        // 加载所有车位
        List<ParkingSpace> allSpaces = spaceDAO.getAllParkingSpaces();
        for (ParkingSpace space : allSpaces) {
            spaceComboBox.addItem(space);
            if (space.getSpaceId() == record.getSpaceId()) {
                spaceComboBox.setSelectedItem(space);
            }
        }

        // 设置出场时间（如果已存在）
        if (record.getExitTime() != null) {
            exitTimeField.setText(record.getExitTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            // 计算停车时长和费用
            long durationSeconds = java.time.Duration.between(record.getEntryTime(), record.getExitTime()).getSeconds();
            double fee = calculateParkingFee(durationSeconds);
            durationField.setText(formatDuration(durationSeconds));
            feeField.setText(String.format("%.2f", fee));
        }

        formPanel.add(new JLabel("车牌号:"));
        formPanel.add(licensePlateField);
        formPanel.add(new JLabel("车位选择:"));
        formPanel.add(spaceComboBox);
        formPanel.add(new JLabel("入场时间:"));
        formPanel.add(entryTimeField);
        formPanel.add(new JLabel("出场时间:"));
        formPanel.add(exitTimeField);
        formPanel.add(new JLabel("停车时长:"));
        formPanel.add(durationField);
        formPanel.add(new JLabel("停车费用:"));
        formPanel.add(feeField);
        formPanel.add(new JLabel("支付状态:"));
        formPanel.add(paymentStatusCombo); // 添加支付状态选择项

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("保存修改");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(e -> {
            try {
                String licensePlate = licensePlateField.getText().trim();
                if (licensePlate.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "车牌号不能为空!", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                ParkingSpace selectedSpace = (ParkingSpace) spaceComboBox.getSelectedItem();
                LocalDateTime entryTime = LocalDateTime.parse(entryTimeField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                LocalDateTime exitTime = null;

                if (!exitTimeField.getText().trim().isEmpty()) {
                    exitTime = LocalDateTime.parse(exitTimeField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }

                // 更新停车记录
                record.setSpaceId(selectedSpace.getSpaceId());
                record.setEntryTime(entryTime);
                record.setExitTime(exitTime);

                if (exitTime != null) {
                    long durationSeconds = java.time.Duration.between(entryTime, exitTime).getSeconds();
                    double fee = calculateParkingFee(durationSeconds);
                    record.setFee(fee);
                } else {
                    record.setFee(null);
                }

                // 设置支付状态
                record.setPaymentStatus((String) paymentStatusCombo.getSelectedItem());

                // 检查车辆是否存在，不存在则创建
                Vehicle vehicle = vehicleDAO.getVehicleByPlateNumber(licensePlate);
                if (vehicle == null) {
                    // 创建新车辆记录
                    vehicle = new Vehicle();
                    vehicle.setPlateNumber(licensePlate);
                    vehicle.setCreateTime(LocalDateTime.now());
                    vehicle.setUpdateTime(LocalDateTime.now());
                    if (!vehicleDAO.addVehicle(vehicle)) {
                        JOptionPane.showMessageDialog(dialog, "车辆信息创建失败!", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    // 重新获取带ID的车辆信息
                    vehicle = vehicleDAO.getVehicleByPlateNumber(licensePlate);
                    if (vehicle == null) {
                        JOptionPane.showMessageDialog(dialog, "无法获取车辆ID!", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                // 更新车辆ID
                record.setVehicleId(vehicle.getVehicleId());
                record.setLicensePlate(licensePlate);
                record.setSpaceNumber(selectedSpace.getSpaceNumber());

                if (recordDAO.updateParkingRecord(record)) {
                    // 更新车位状态
                    if (currentSpace != null && currentSpace.getSpaceId() != selectedSpace.getSpaceId()) {
                        // 如果修改了车位，更新原车位和新车位状态
                        currentSpace.setOccupied(false);
                        spaceDAO.updateParkingSpace(currentSpace);
                        selectedSpace.setOccupied(exitTime == null);
                        spaceDAO.updateParkingSpace(selectedSpace);
                    } else if (currentSpace != null) {
                        // 只更新当前车位状态
                        currentSpace.setOccupied(exitTime == null);
                        spaceDAO.updateParkingSpace(currentSpace);
                    }

                    JOptionPane.showMessageDialog(dialog, "记录修改成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                    loadParkingRecords();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "记录修改失败!", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "修改失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteRecord() {
        int selectedRow = recordTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的记录!", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int recordId = (int) tableModel.getValueAt(selectedRow, 0);
        ParkingRecord record = recordDAO.getParkingRecordById(recordId);

        if (record.getExitTime() == null) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "该车辆尚未出场，确定要删除此记录吗？\n删除后车位将变为可用状态。",
                    "确认删除",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        } else {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "确定要删除此停车记录吗？",
                    "确认删除",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            // 如果车辆未出场，释放车位
            if (record.getExitTime() == null) {
                ParkingSpace space = spaceDAO.getParkingSpaceById(record.getSpaceId());
                if (space != null) {
                    space.setOccupied(false);
                    spaceDAO.updateParkingSpace(space);
                }
            }

            if (recordDAO.deleteParkingRecord(recordId)) {
                JOptionPane.showMessageDialog(this, "记录删除成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadParkingRecords();
            } else {
                JOptionPane.showMessageDialog(this, "记录删除失败!", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "删除失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double calculateParkingFee(long seconds) {
        // 计费规则：前2小时每小时5元，之后每小时3元
        double hours = seconds / 3600.0;
        if (hours <= 2) {
            return hours * 5;
        } else {
            return 10 + (hours - 2) * 3;
        }
    }

    // 格式化时长显示（时:分:秒）
    private String formatDuration(Long seconds) {
        if (seconds == null) return "";
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}