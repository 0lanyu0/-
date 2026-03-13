package com.parkingmanagement.ui;

import com.parkingmanagement.dao.FeeRuleDAO;
import com.parkingmanagement.model.FeeRule;
import com.parkingmanagement.model.SpaceType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class FeeRuleManagementPanel extends JPanel {
    private JTable ruleTable;
    private DefaultTableModel tableModel;
    private FeeRuleDAO feeRuleDAO;
    private JComboBox<SpaceType> typeFilter;

    public FeeRuleManagementPanel() {
        setLayout(new BorderLayout());
        feeRuleDAO = new FeeRuleDAO();

        initializeFilterPanel();
        initializeTable();
        initializeButtons();
        loadFeeRules(SpaceType.values()[0]);
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

        typeFilter.addActionListener(e -> loadFeeRules((SpaceType) typeFilter.getSelectedItem()));
        refreshButton.addActionListener(e -> loadFeeRules((SpaceType) typeFilter.getSelectedItem()));
    }

    private void initializeTable() {
        String[] columnNames = {"ID", "小时费率", "日最高收费", "生效日期", "状态", "创建时间", "更新时间"};
        tableModel = new DefaultTableModel(columnNames, 0);
        ruleTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(ruleTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void initializeButtons() {
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("添加规则");
        JButton editButton = new JButton("修改规则");
        JButton activateButton = new JButton("启用规则");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(activateButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        activateButton.addActionListener(e -> activateRule());
    }

    private void loadFeeRules(SpaceType type) {
        tableModel.setRowCount(0);
        List<FeeRule> rules = feeRuleDAO.getAllFeeRules();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (FeeRule rule : rules) {
            if (rule.getTypeId() == type.getId()) {
                String status = rule.isActive() ? "已启用" : "未启用";
                tableModel.addRow(new Object[]{
                        rule.getRuleId(),
                        "?" + rule.getHourlyRate(),
                        rule.getDailyMax() > 0 ? "?" + rule.getDailyMax() : "无",
                        rule.getEffectiveDate().format(formatter),
                        status,
                        rule.getCreateTime().format(formatter),
                        rule.getUpdateTime().format(formatter)
                });
            }
        }
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "添加收费规则", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        JComboBox<SpaceType> typeComboBox = new JComboBox<>(SpaceType.values());
        JTextField hourlyRateField = new JTextField();
        JTextField dailyMaxField = new JTextField();
        JCheckBox activeCheckBox = new JCheckBox("立即启用", true);
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());

        // 设置日期选择器格式
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd HH:mm");
        dateSpinner.setEditor(dateEditor);

        panel.add(new JLabel("车位类型:"));
        panel.add(typeComboBox);
        panel.add(new JLabel("小时费率(元):"));
        panel.add(hourlyRateField);
        panel.add(new JLabel("日最高收费(元):"));
        panel.add(dailyMaxField);
        panel.add(new JLabel("生效日期:"));
        panel.add(dateSpinner);
        panel.add(new JLabel("状态:"));
        panel.add(activeCheckBox);

        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(e -> {
            SpaceType type = (SpaceType) typeComboBox.getSelectedItem();
            String hourlyRateText = hourlyRateField.getText().trim();
            String dailyMaxText = dailyMaxField.getText().trim();
            boolean isActive = activeCheckBox.isSelected();
            LocalDateTime effectiveDate = LocalDateTime.ofInstant(
                    ((Date) dateSpinner.getValue()).toInstant(),
                    ZoneId.systemDefault()
            );

            if (hourlyRateText.isEmpty() || dailyMaxText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "费率和日最高收费不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double hourlyRate = Double.parseDouble(hourlyRateText);
                double dailyMax = Double.parseDouble(dailyMaxText);

                if (hourlyRate <= 0 || dailyMax <= 0) {
                    JOptionPane.showMessageDialog(dialog, "费率和日最高收费必须大于0！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                FeeRule rule = new FeeRule();
                rule.setTypeId(type.getId());
                rule.setTypeName(type.getName());
                rule.setHourlyRate(hourlyRate);
                rule.setDailyMax(dailyMax);
                rule.setActive(isActive);
                rule.setEffectiveDate(effectiveDate);
                rule.setCreateTime(LocalDateTime.now());
                rule.setUpdateTime(LocalDateTime.now());

                feeRuleDAO.addFeeRule(rule);

                // 如果启用了该规则，禁用该类型的其他规则
                if (isActive) {
                    List<FeeRule> allRules = feeRuleDAO.getAllFeeRules();
                    for (FeeRule r : allRules) {
                        if (r.getTypeId() == type.getId() && r.getRuleId() != rule.getRuleId() && r.isActive()) {
                            r.setActive(false);
                            feeRuleDAO.updateFeeRule(r);
                        }
                    }
                }

                loadFeeRules((SpaceType) typeFilter.getSelectedItem());
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "请输入有效的数字！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showEditDialog() {
        int selectedRow = ruleTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要修改的规则！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ruleId = (int) tableModel.getValueAt(selectedRow, 0);
        FeeRule rule = feeRuleDAO.getFeeRuleById(ruleId);
        if (rule == null) {
            JOptionPane.showMessageDialog(this, "未找到该规则！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "修改收费规则", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        JComboBox<SpaceType> typeComboBox = new JComboBox<>(SpaceType.values());
        JTextField hourlyRateField = new JTextField(String.valueOf(rule.getHourlyRate()));
        JTextField dailyMaxField = new JTextField(rule.getDailyMax() > 0 ? String.valueOf(rule.getDailyMax()) : "");
        JCheckBox activeCheckBox = new JCheckBox("启用", rule.isActive());

        // 修改此处：将LocalDateTime转换为Date
        Date effectiveDateAsDate = Date.from(rule.getEffectiveDate().atZone(ZoneId.systemDefault()).toInstant());
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel(effectiveDateAsDate, null, null, java.util.Calendar.HOUR_OF_DAY));

        // 设置类型选中项
        for (SpaceType type : SpaceType.values()) {
            if (type.getId() == rule.getTypeId()) {
                typeComboBox.setSelectedItem(type);
                break;
            }
        }

        // 设置日期选择器格式
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd HH:mm");
        dateSpinner.setEditor(dateEditor);

        panel.add(new JLabel("车位类型:"));
        panel.add(typeComboBox);
        panel.add(new JLabel("小时费率(元):"));
        panel.add(hourlyRateField);
        panel.add(new JLabel("日最高收费(元):"));
        panel.add(dailyMaxField);
        panel.add(new JLabel("生效日期:"));
        panel.add(dateSpinner);
        panel.add(new JLabel("状态:"));
        panel.add(activeCheckBox);

        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(e -> {
            SpaceType type = (SpaceType) typeComboBox.getSelectedItem();
            String hourlyRateText = hourlyRateField.getText().trim();
            String dailyMaxText = dailyMaxField.getText().trim();
            boolean isActive = activeCheckBox.isSelected();
            LocalDateTime effectiveDate = LocalDateTime.ofInstant(
                    ((Date) dateSpinner.getValue()).toInstant(),
                    ZoneId.systemDefault()
            );

            if (hourlyRateText.isEmpty() || dailyMaxText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "费率和日最高收费不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double hourlyRate = Double.parseDouble(hourlyRateText);
                double dailyMax = Double.parseDouble(dailyMaxText);

                if (hourlyRate <= 0 || dailyMax <= 0) {
                    JOptionPane.showMessageDialog(dialog, "费率和日最高收费必须大于0！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                rule.setTypeId(type.getId());
                rule.setTypeName(type.getName());
                rule.setHourlyRate(hourlyRate);
                rule.setDailyMax(dailyMax);
                rule.setActive(isActive);
                rule.setEffectiveDate(effectiveDate);
                rule.setUpdateTime(LocalDateTime.now());

                feeRuleDAO.updateFeeRule(rule);

                // 如果启用了该规则，禁用该类型的其他规则
                if (isActive) {
                    List<FeeRule> allRules = feeRuleDAO.getAllFeeRules();
                    for (FeeRule r : allRules) {
                        if (r.getTypeId() == type.getId() && r.getRuleId() != rule.getRuleId() && r.isActive()) {
                            r.setActive(false);
                            feeRuleDAO.updateFeeRule(r);
                        }
                    }
                }

                loadFeeRules((SpaceType) typeFilter.getSelectedItem());
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "请输入有效的数字！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void activateRule() {
        int selectedRow = ruleTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要启用的规则！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ruleId = (int) tableModel.getValueAt(selectedRow, 0);
        FeeRule rule = feeRuleDAO.getFeeRuleById(ruleId);

        if (rule.isActive()) {
            JOptionPane.showMessageDialog(this, "该规则已启用！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要启用 " + rule.getTypeName() + " 的收费规则吗？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // 先禁用该类型的所有其他规则
                List<FeeRule> allRules = feeRuleDAO.getAllFeeRules();
                for (FeeRule r : allRules) {
                    if (r.getTypeId() == rule.getTypeId() && r.isActive()) {
                        r.setActive(false);
                        feeRuleDAO.updateFeeRule(r);
                    }
                }

                // 启用选中的规则
                rule.setActive(true);
                rule.setUpdateTime(LocalDateTime.now());
                feeRuleDAO.updateFeeRule(rule);

                JOptionPane.showMessageDialog(this, "规则启用成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadFeeRules((SpaceType) typeFilter.getSelectedItem());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "规则启用失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}