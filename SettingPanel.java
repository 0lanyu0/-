package com.parkingmanagement.ui;

import com.parkingmanagement.dao.SettingsDAO;
import com.parkingmanagement.model.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;

public class SettingPanel extends JPanel {
    private SettingsDAO settingsDAO;
    private JTextField parkingNameField;
    private JTextField addressField;
    private JTextField contactPhoneField;
    private JTextField contactEmailField;
    private JTextField baseFeeField;
    private JTextField hourlyRateField;
    private JTextField dailyMaxFeeField;
    private JTextField currencyField;

    public SettingPanel() {
        setLayout(new BorderLayout());
        settingsDAO = new SettingsDAO();

        initializeUI();
        loadSettings();
    }

    private void initializeUI() {
        // 创建表单面板
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(8, 2, 10, 10));

        parkingNameField = new JTextField();
        addressField = new JTextField();
        contactPhoneField = new JTextField();
        contactEmailField = new JTextField();
        baseFeeField = new JTextField();
        hourlyRateField = new JTextField();
        dailyMaxFeeField = new JTextField();
        currencyField = new JTextField();

        formPanel.add(new JLabel("停车场名称:"));
        formPanel.add(parkingNameField);
        formPanel.add(new JLabel("地址:"));
        formPanel.add(addressField);
        formPanel.add(new JLabel("联系电话:"));
        formPanel.add(contactPhoneField);
        formPanel.add(new JLabel("电子邮箱:"));
        formPanel.add(contactEmailField);
        formPanel.add(new JLabel("基础费用(前2小时):"));
        formPanel.add(baseFeeField);
        formPanel.add(new JLabel("超时后每小时费用:"));
        formPanel.add(hourlyRateField);
        formPanel.add(new JLabel("每日最高费用:"));
        formPanel.add(dailyMaxFeeField);
        formPanel.add(new JLabel("货币单位:"));
        formPanel.add(currencyField);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("保存设置");
        JButton resetButton = new JButton("重置");

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSettings();
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadSettings();
            }
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(resetButton);

        // 添加到主面板
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadSettings() {
        Settings settings = settingsDAO.getSettings();

        if (settings != null) {
            parkingNameField.setText(settings.getParkingName());
            addressField.setText(settings.getAddress());
            contactPhoneField.setText(settings.getContactPhone());
            contactEmailField.setText(settings.getContactEmail());
            baseFeeField.setText(String.valueOf(settings.getBaseFee()));
            hourlyRateField.setText(String.valueOf(settings.getHourlyRate()));
            dailyMaxFeeField.setText(String.valueOf(settings.getDailyMaxFee()));
            currencyField.setText(settings.getCurrency());
        }
    }

    private void saveSettings() {
        try {
            String parkingName = parkingNameField.getText().trim();
            String address = addressField.getText().trim();
            String contactPhone = contactPhoneField.getText().trim();
            String contactEmail = contactEmailField.getText().trim();
            double baseFee = Double.parseDouble(baseFeeField.getText().trim());
            double hourlyRate = Double.parseDouble(hourlyRateField.getText().trim());
            double dailyMaxFee = Double.parseDouble(dailyMaxFeeField.getText().trim());
            String currency = currencyField.getText().trim();

            if (parkingName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "停车场名称不能为空!", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Settings settings = settingsDAO.getSettings();
            if (settings == null) {
                settings = new Settings();
            }

            settings.setParkingName(parkingName);
            settings.setAddress(address);
            settings.setContactPhone(contactPhone);
            settings.setContactEmail(contactEmail);
            settings.setBaseFee(BigDecimal.valueOf(baseFee));
            settings.setHourlyRate(BigDecimal.valueOf(hourlyRate));
            settings.setDailyMaxFee(BigDecimal.valueOf(dailyMaxFee));
            settings.setCurrency(currency);

            if (settingsDAO.saveSettings(settings)) {
                JOptionPane.showMessageDialog(this, "设置保存成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "设置保存失败!", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "请输入有效的数字格式!", "错误", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "保存设置时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
