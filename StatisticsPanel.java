package com.parkingmanagement.ui;

import com.parkingmanagement.dao.ParkingRecordDAO;
import com.parkingmanagement.dao.ParkingSpaceDAO;
import com.parkingmanagement.model.ParkingRecord;
import com.parkingmanagement.model.ParkingSpace;
import com.parkingmanagement.service.ParkingService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsPanel extends JPanel {
    private ParkingSpaceDAO spaceDAO;
    private ParkingRecordDAO recordDAO;
    private ParkingService parkingService;
    private JTabbedPane statsTabbedPane;
    private JTextField dateFromField;
    private JTextField dateToField;
    // 图表相关组件
    private ChartPanel usageChartPanel;
    private DefaultPieDataset usageDataset;

    public StatisticsPanel() {
        setLayout(new BorderLayout());
        spaceDAO = new ParkingSpaceDAO();
        recordDAO = new ParkingRecordDAO();
        parkingService = new ParkingService();
        initializeFilterPanel();
        initializeStatsTabs();
        updateStatistics();
    }

    private void initializeFilterPanel() {
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        dateFromField = new JTextField(10);
        dateToField = new JTextField(10);
        LocalDate today = LocalDate.now();
        dateFromField.setText(today.toString());
        dateToField.setText(today.toString());
        JButton refreshButton = new JButton("刷新统计");
        filterPanel.add(new JLabel("开始日期:"));
        filterPanel.add(dateFromField);
        filterPanel.add(new JLabel("结束日期:"));
        filterPanel.add(dateToField);
        filterPanel.add(refreshButton);
        add(filterPanel, BorderLayout.NORTH);
        refreshButton.addActionListener(e -> updateStatistics());
    }

    private void initializeStatsTabs() {
        statsTabbedPane = new JTabbedPane();
        JPanel overviewPanel = createOverviewPanel();
        statsTabbedPane.addTab("停车场概览", overviewPanel);
        JPanel spaceUsagePanel = createSpaceUsagePanel();
        statsTabbedPane.addTab("车位使用统计", spaceUsagePanel);
        JPanel revenuePanel = createRevenuePanel();
        statsTabbedPane.addTab("收入统计", revenuePanel);
        add(statsTabbedPane, BorderLayout.CENTER);
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel infoCard = new JPanel(new GridLayout(3, 2, 10, 10));
        infoCard.setBorder(BorderFactory.createTitledBorder("停车场基本信息"));
        JLabel totalSpacesLabel = new JLabel("总车位数:");
        JLabel totalSpacesValue = new JLabel("0");
        JLabel availableSpacesLabel = new JLabel("可用车位数:");
        JLabel availableSpacesValue = new JLabel("0");
        JLabel occupiedSpacesLabel = new JLabel("已占用车位数:");
        JLabel occupiedSpacesValue = new JLabel("0");
        infoCard.add(totalSpacesLabel);
        infoCard.add(totalSpacesValue);
        infoCard.add(availableSpacesLabel);
        infoCard.add(availableSpacesValue);
        infoCard.add(occupiedSpacesLabel);
        infoCard.add(occupiedSpacesValue);
        panel.add(infoCard);
        JPanel usageChartPanel = new JPanel();
        usageChartPanel.setBorder(BorderFactory.createTitledBorder("停车场使用率"));
        usageChartPanel.setPreferredSize(new Dimension(800, 300));
        usageDataset = new DefaultPieDataset();
        JFreeChart chart = createUsageChart(usageDataset);
        this.usageChartPanel = new ChartPanel(chart);
        this.usageChartPanel.setPreferredSize(new Dimension(700, 250));
        this.usageChartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.usageChartPanel.setMouseWheelEnabled(true);
        usageChartPanel.add(this.usageChartPanel);
        panel.add(usageChartPanel);
        JPanel dailyStatsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        dailyStatsPanel.setBorder(BorderFactory.createTitledBorder("今日统计"));
        JLabel totalEntriesLabel = new JLabel("总入场车辆:");
        JLabel totalEntriesValue = new JLabel("0");
        JLabel totalExitsLabel = new JLabel("总出场车辆:");
        JLabel totalExitsValue = new JLabel("0");
        JLabel totalRevenueLabel = new JLabel("总收入:");
        JLabel totalRevenueValue = new JLabel("0.00");
        dailyStatsPanel.add(totalEntriesLabel);
        dailyStatsPanel.add(totalEntriesValue);
        dailyStatsPanel.add(totalExitsLabel);
        dailyStatsPanel.add(totalExitsValue);
        dailyStatsPanel.add(totalRevenueLabel);
        dailyStatsPanel.add(totalRevenueValue);
        panel.add(dailyStatsPanel);
        panel.putClientProperty("totalSpacesValue", totalSpacesValue);
        panel.putClientProperty("availableSpacesValue", availableSpacesValue);
        panel.putClientProperty("occupiedSpacesValue", occupiedSpacesValue);
        panel.putClientProperty("totalEntriesValue", totalEntriesValue);
        panel.putClientProperty("totalExitsValue", totalExitsValue);
        panel.putClientProperty("totalRevenueValue", totalRevenueValue);
        return panel;
    }

    private JFreeChart createUsageChart(DefaultPieDataset dataset) {
        JFreeChart chart = ChartFactory.createPieChart(
                "停车场使用率",
                dataset,
                true,
                true,
                false
        );
        PiePlot plot = (PiePlot) chart.getPlot();
        Font chineseFont = new Font("SimSun", Font.PLAIN, 12);
        plot.setLabelFont(chineseFont);
        // 设置图例字体，通过获取图例并设置字体
        chart.getLegend().setItemFont(chineseFont);
        // 设置标题字体
        TextTitle title = chart.getTitle();
        title.setFont(chineseFont);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0} = {1} ({2})",
                new DecimalFormat("0"),
                new DecimalFormat("0.00%")
        ));
        plot.setNoDataMessage("没有可用数据");
        plot.setCircular(true);
        plot.setLabelGap(0.02);
        return chart;
    }

    private JPanel createSpaceUsagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columnNames = {"车位编号", "车位类型", "使用次数", "最近使用时间", "使用率"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable spaceUsageTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(spaceUsageTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.putClientProperty("spaceUsageTableModel", tableModel);
        return panel;
    }

    private JPanel createRevenuePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columnNames = {"日期", "收入金额", "交易笔数", "平均停车时长(小时)"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable revenueTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(revenueTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.putClientProperty("revenueTableModel", tableModel);
        return panel;
    }

    private void updateStatistics() {
        try {
            LocalDate fromDate = LocalDate.parse(dateFromField.getText());
            LocalDate toDate = LocalDate.parse(dateToField.getText());
            LocalDateTime fromDateTime = fromDate.atStartOfDay();
            LocalDateTime toDateTime = toDate.atTime(23, 59, 59);
            updateOverviewStats(fromDateTime, toDateTime);
            updateSpaceUsageStats(fromDateTime, toDateTime);
            updateRevenueStats(fromDateTime, toDateTime);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "日期格式错误，请使用YYYY-MM-DD格式！", "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateOverviewStats(LocalDateTime fromDateTime, LocalDateTime toDateTime) {
        JPanel overviewPanel = (JPanel) statsTabbedPane.getComponentAt(0);
        JLabel totalSpacesValue = (JLabel) overviewPanel.getClientProperty("totalSpacesValue");
        JLabel availableSpacesValue = (JLabel) overviewPanel.getClientProperty("availableSpacesValue");
        JLabel occupiedSpacesValue = (JLabel) overviewPanel.getClientProperty("occupiedSpacesValue");
        List<ParkingSpace> allSpaces = spaceDAO.getAllParkingSpaces();
        int totalSpaces = allSpaces.size();
        long availableSpaces = allSpaces.stream()
                .filter(s -> s.isAvailable() && !s.isOccupied())
                .count();
        long occupiedSpaces = allSpaces.stream()
                .filter(ParkingSpace::isOccupied)
                .count();
        totalSpacesValue.setText(String.valueOf(totalSpaces));
        availableSpacesValue.setText(String.valueOf(availableSpaces));
        occupiedSpacesValue.setText(String.valueOf(occupiedSpaces));
        updateUsageChart(occupiedSpaces, totalSpaces);
        JLabel totalEntriesValue = (JLabel) overviewPanel.getClientProperty("totalEntriesValue");
        JLabel totalExitsValue = (JLabel) overviewPanel.getClientProperty("totalExitsValue");
        JLabel totalRevenueValue = (JLabel) overviewPanel.getClientProperty("totalRevenueValue");
        List<ParkingRecord> records = recordDAO.getAllParkingRecords().stream()
                .filter(r -> r.getEntryTime() != null &&
                        !r.getEntryTime().isBefore(fromDateTime) &&
                        !r.getEntryTime().isAfter(toDateTime))
                .collect(Collectors.toList()); // 修改此处：toList() 改为 collect(Collectors.toList())
        int totalEntries = records.size();
        int totalExits = (int) records.stream()
                .filter(r -> r.getExitTime() != null)
                .count();
        double totalRevenue = records.stream()
                .filter(r -> r.getFee() != null)
                .mapToDouble(ParkingRecord::getFee)
                .sum();
        totalEntriesValue.setText(String.valueOf(totalEntries));
        totalExitsValue.setText(String.valueOf(totalExits));
        totalRevenueValue.setText(String.format("%.2f", totalRevenue));
    }

    private void updateUsageChart(long occupied, long total) {
        if (usageDataset == null) return;
        double occupiedRate = total > 0 ? (double) occupied / total * 100 : 0;
        double availableRate = 100 - occupiedRate;
        usageDataset.clear();
        usageDataset.setValue("已占用 (" + (int) occupiedRate + "%)", occupied);
        usageDataset.setValue("可用 (" + (int) availableRate + "%)", total - occupied);
        if (usageChartPanel != null) {
            usageChartPanel.getChart().fireChartChanged();
        }
    }

    private void updateSpaceUsageStats(LocalDateTime fromDateTime, LocalDateTime toDateTime) {
        JPanel spaceUsagePanel = (JPanel) statsTabbedPane.getComponentAt(1);
        DefaultTableModel tableModel = (DefaultTableModel) spaceUsagePanel.getClientProperty("spaceUsageTableModel");
        tableModel.setRowCount(0);
        List<ParkingSpace> allSpaces = spaceDAO.getAllParkingSpaces();
        List<ParkingRecord> records = recordDAO.getAllParkingRecords().stream()
                .filter(r -> r.getExitTime() != null &&
                        !r.getExitTime().isBefore(fromDateTime) &&
                        !r.getExitTime().isAfter(toDateTime))
                .collect(Collectors.toList());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (ParkingSpace space : allSpaces) {
            long usageCount = records.stream()
                    .filter(r -> r.getSpaceId() == space.getSpaceId())
                    .count();
            LocalDateTime lastUsedTime = records.stream()
                    .filter(r -> r.getSpaceId() == space.getSpaceId())
                    .map(ParkingRecord::getExitTime)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            double usageRate = records.size() > 0 ? (double) usageCount / records.size() * 100 : 0;
            tableModel.addRow(new Object[]{
                    space.getSpaceNumber(),
                    space.getTypeName(),
                    usageCount,
                    lastUsedTime != null ? lastUsedTime.format(formatter) : "从未使用",
                    String.format("%.2f%%", usageRate)
            });
        }
    }

    private void updateRevenueStats(LocalDateTime fromDateTime, LocalDateTime toDateTime) {
        JPanel revenuePanel = (JPanel) statsTabbedPane.getComponentAt(2);
        DefaultTableModel tableModel = (DefaultTableModel) revenuePanel.getClientProperty("revenueTableModel");
        tableModel.setRowCount(0);
        List<ParkingRecord> records = recordDAO.getAllParkingRecords().stream()
                .filter(r -> r.getExitTime() != null &&
                        !r.getExitTime().isBefore(fromDateTime) &&
                        !r.getExitTime().isAfter(toDateTime))
                .collect(Collectors.toList());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<LocalDate, List<ParkingRecord>> dailyRecords = records.stream()
                .collect(Collectors.groupingBy(r -> r.getExitTime().toLocalDate()));
        for (LocalDate date : dailyRecords.keySet()) {
            List<ParkingRecord> dailyRecs = dailyRecords.get(date);
            double dailyRevenue = dailyRecs.stream()
                    .filter(r -> r.getFee() != null)
                    .mapToDouble(ParkingRecord::getFee)
                    .sum();
            int transactionCount = dailyRecs.size();
            double avgDuration = dailyRecs.stream()
                    .filter(r -> r.getDuration() != null)
                    .mapToDouble(r -> r.getDuration() / 3600.0)
                    .average()
                    .orElse(0.0);
            tableModel.addRow(new Object[]{
                    date.format(formatter),
                    String.format("%.2f", dailyRevenue),
                    transactionCount,
                    String.format("%.2f", avgDuration)
            });
        }
    }
}