package com.parkingmanagement.ui;

import com.parkingmanagement.dao.SettingsDAO;
import com.parkingmanagement.model.Settings;

import javax.swing.*;
import java.awt.*;

public class ParkingLotInfoDisplayPanel extends JPanel {
    private SettingsDAO settingsDAO;
    private JLabel parkingNameLabel;
    private JLabel addressLabel;
    private JLabel contactPhoneLabel;
    private JLabel contactEmailLabel;
    private JLabel baseFeeLabel;
    private JLabel hourlyRateLabel;
    private JLabel dailyMaxFeeLabel;
    private JLabel currencyLabel;

    public ParkingLotInfoDisplayPanel() {
        setLayout(new BorderLayout());
        settingsDAO = new SettingsDAO();

        initializeUI();
        loadSettings();
    }

    private void initializeUI() {
        // 创建表单面板
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(8, 2, 10, 10));

        parkingNameLabel = new JLabel();
        addressLabel = new JLabel();
        contactPhoneLabel = new JLabel();
        contactEmailLabel = new JLabel();
        baseFeeLabel = new JLabel();
        hourlyRateLabel = new JLabel();
        dailyMaxFeeLabel = new JLabel();
        currencyLabel = new JLabel();

        formPanel.add(new JLabel("停车场名称:"));
        formPanel.add(parkingNameLabel);
        formPanel.add(new JLabel("地址:"));
        formPanel.add(addressLabel);
        formPanel.add(new JLabel("联系电话:"));
        formPanel.add(contactPhoneLabel);
        formPanel.add(new JLabel("联系邮箱:"));
        formPanel.add(contactEmailLabel);
        formPanel.add(new JLabel("基础费用(前2小时):"));
        formPanel.add(baseFeeLabel);
        formPanel.add(new JLabel("后续每小时收费:"));
        formPanel.add(hourlyRateLabel);
        formPanel.add(new JLabel("每日最高收费:"));
        formPanel.add(dailyMaxFeeLabel);
        formPanel.add(new JLabel("货币单位:"));
        formPanel.add(currencyLabel);

        // 添加到主面板
        add(formPanel, BorderLayout.CENTER);
    }

    private void loadSettings() {
        Settings settings = settingsDAO.getSettings();

        if (settings != null) {
            parkingNameLabel.setText(settings.getParkingName());
            addressLabel.setText(settings.getAddress());
            contactPhoneLabel.setText(settings.getContactPhone());
            contactEmailLabel.setText(settings.getContactEmail());
            baseFeeLabel.setText(String.valueOf(settings.getBaseFee()));
            hourlyRateLabel.setText(String.valueOf(settings.getHourlyRate()));
            dailyMaxFeeLabel.setText(String.valueOf(settings.getDailyMaxFee()));
            currencyLabel.setText(settings.getCurrency());
        }
    }
}