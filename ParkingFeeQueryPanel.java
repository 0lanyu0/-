package com.parkingmanagement.ui;

import com.parkingmanagement.dao.ParkingRecordDAO;
import com.parkingmanagement.dao.VehicleDAO;
import com.parkingmanagement.model.ParkingRecord;
import com.parkingmanagement.model.Vehicle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParkingFeeQueryPanel extends JPanel {
    private ParkingRecordDAO recordDAO;
    private VehicleDAO vehicleDAO;
    private JTextField licensePlateField;
    private JTextField dateFromField;
    private JTextField dateToField;
    private JLabel resultLabel;
    private JPanel resultPanel;
    private JTable recordTable;
    private DefaultTableModel tableModel;
    private JRadioButton allRecordsRadio;
    private JRadioButton activeRecordsRadio;
    private JTextField searchField;

    public ParkingFeeQueryPanel() {
        setLayout(new BorderLayout());
        recordDAO = new ParkingRecordDAO();
        vehicleDAO = new VehicleDAO();
        initializeUI();
        initializeFilterAndSearchPanel();
        initializeTable();
        loadRecords(true, "");
    }

    private void initializeUI() {
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        licensePlateField = new JTextField(15);
        dateFromField = new JTextField(10);
        dateToField = new JTextField(10);
        JButton searchButton = new JButton("查询");

        LocalDateTime now = LocalDateTime.now();
        dateFromField.setText(now.toLocalDate().toString());
        dateToField.setText(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        searchButton.addActionListener(e -> searchParkingRecord());

        searchPanel.add(new JLabel("请输入车牌号查询停车费用:"));
        searchPanel.add(licensePlateField);
        searchPanel.add(new JLabel("开始日期:"));
        searchPanel.add(dateFromField);
        searchPanel.add(new JLabel("结束日期:"));
        searchPanel.add(dateToField);
        searchPanel.add(searchButton);

        resultPanel = new JPanel(new BorderLayout());
        resultLabel = new JLabel("请输入车牌号查询停车费用信息");
        resultLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resultPanel.add(resultLabel, BorderLayout.CENTER);

        // 新增：直接在中间靠下方显示图片和文字
        JPanel qrCodePanel = new JPanel(new BorderLayout());
        JLabel qrCodeTextLabel = new JLabel("请使用微信扫码支付", SwingConstants.CENTER);
        qrCodeTextLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        qrCodePanel.add(qrCodeTextLabel, BorderLayout.NORTH);

        try {
            ImageIcon icon = new ImageIcon("com/erweima.jpg");
            Image scaledImage = icon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            qrCodePanel.add(imageLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("无法加载二维码图片");
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            qrCodePanel.add(errorLabel, BorderLayout.CENTER);
            e.printStackTrace();
        }

        resultPanel.add(qrCodePanel, BorderLayout.SOUTH);

        add(searchPanel, BorderLayout.NORTH);
        add(resultPanel, BorderLayout.CENTER);
    }

    private void initializeFilterAndSearchPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));

        JPanel filterPanel = new JPanel();
        allRecordsRadio = new JRadioButton("所有记录");
        activeRecordsRadio = new JRadioButton("在停记录");
        allRecordsRadio.setSelected(true);
        ButtonGroup group = new ButtonGroup();
        group.add(allRecordsRadio);
        group.add(activeRecordsRadio);
        filterPanel.add(allRecordsRadio);
        filterPanel.add(activeRecordsRadio);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.add(new JLabel("搜索:"));
        searchField = new JTextField(15);
        JButton searchButton = new JButton("搜索");
        searchButton.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            loadRecords(allRecordsRadio.isSelected(), keyword);
        });
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        topPanel.add(filterPanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        allRecordsRadio.addActionListener(e -> loadRecords(true, searchField.getText().trim()));
        activeRecordsRadio.addActionListener(e -> loadRecords(false, searchField.getText().trim()));
    }

    private void initializeTable() {
        String[] columnNames = {"ID", "车牌号", "车位编号", "入场时间", "出场时间", "费用", "支付状态"};
        tableModel = new DefaultTableModel(columnNames, 0);
        recordTable = new JTable(tableModel);
        recordTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollPane = new JScrollPane(recordTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadRecords(boolean showAll, String keyword) {
        tableModel.setRowCount(0);
        List<ParkingRecord> records;

        if (showAll) {
            records = recordDAO.getAllParkingRecords();
        } else {
            records = recordDAO.getActiveParkingRecords();
        }

        if (records.isEmpty()) {
            resultLabel.setText("未找到符合条件的停车记录");
            return;
        }

        if (!keyword.isEmpty()) {
            records = records.stream()
                    .filter(r -> r.getLicensePlate() != null && r.getLicensePlate().contains(keyword) ||
                            r.getSpaceNumber() != null && r.getSpaceNumber().contains(keyword))
                    .collect(Collectors.toList());
        }

        for (ParkingRecord record : records) {
            tableModel.addRow(new Object[]{
                    record.getRecordId(),
                    record.getLicensePlate(),
                    record.getSpaceNumber(),
                    formatDateTime(record.getEntryTime()),
                    formatDateTime(record.getExitTime()),
                    record.getFee() != null ? record.getFee() : "未计算",
                    record.getPaymentStatus()
            });
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
    }

    private void searchParkingRecord() {
        String licensePlate = licensePlateField.getText().trim();
        String dateFrom = dateFromField.getText().trim();
        String dateTo = dateToField.getText().trim();

        if (licensePlate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入车牌号!", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ParkingRecord record = null;
        try {
            Optional<Vehicle> vehicleOpt = Optional.ofNullable(
                    vehicleDAO.getVehicleByPlateNumber(licensePlate)
            );

            if (vehicleOpt.isPresent()) {
                Integer vehicleId = vehicleOpt.get().getVehicleId();
                List<ParkingRecord> records = recordDAO.searchParkingRecords(vehicleId, dateFrom, dateTo);
                record = records.isEmpty() ? null : records.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "查询失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }

        if (record == null) {
            resultLabel.setText("未找到该车牌号的停车记录");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String resultText = "车牌号: " + record.getLicensePlate() + "\n" +
                "车位编号: " + record.getSpaceNumber() + "\n" +
                "入场时间: " + record.getEntryTime().format(formatter) + "\n";

        if (record.getExitTime() != null) {
            resultText += "出场时间: " + record.getExitTime().format(formatter) + "\n";
            resultText += "停车时长: " + formatDuration(record.getDuration()) + "\n";
            resultText += "应缴费用: " + record.getFee() + " 元\n";
        } else {
            resultText += "当前状态: 停车中\n";
            long currentDuration = Duration.between(record.getEntryTime(), LocalDateTime.now()).getSeconds();
            resultText += "当前停车时长: " + formatDuration(currentDuration) + "\n";
            double currentFee = calculateParkingFee(currentDuration);
            resultText += "当前应缴费用: " + currentFee + " 元\n";
            record.setFee(currentFee);
        }

        resultLabel.setText(resultText);
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private double calculateParkingFee(long seconds) {
        double hours = seconds / 3600.0;
        if (hours <= 2) {
            return hours * 5;
        } else {
            return 10 + (hours - 2) * 3;
        }
    }

    private String formatDuration(Long seconds) {
        if (seconds == null) return "";
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d时%02d分%02d秒", hours, minutes, secs);
    }
}