package com.parkingmanagement.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;

    public MainFrame() {
        setTitle("???????????");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // ???????

        initializeUI();
    }

    private void initializeUI() {
        // ?????????
        createMenuBar();

        // ??????????
        tabbedPane = new JTabbedPane();

        // ????????????飨???????飩
        tabbedPane.addTab("????????", new VehicleManagementPanel());
        tabbedPane.addTab("??λ????", new ParkingSpacePanel());
        // ??ó?λ???????
        tabbedPane.addTab("??ó?λ????", new DedicatedParkingPanel());
        // ???????/?????????????
        tabbedPane.addTab("???/??????????", new SpecialReservationManagementPanel());
        tabbedPane.addTab("??λ???????", new ReservationManagementPanel());
        tabbedPane.addTab("??????", new ParkingRecordPanel());
        tabbedPane.addTab("??????", new StatisticsPanel());
        tabbedPane.addTab("???????", new UserManagementPanel());
        tabbedPane.addTab("??????", new SettingPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // ??????
        JMenu fileMenu = new JMenu("???");
        JMenuItem exitMenuItem = new JMenuItem("???");
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        MainFrame.this,
                        "????????????",
                        "??????",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
        fileMenu.add(exitMenuItem);

        // ??????????????????????
        JMenu functionMenu = new JMenu("????");
        JMenuItem vehicleManagementMenuItem = new JMenuItem("????????");
        vehicleManagementMenuItem.addActionListener(e -> tabbedPane.setSelectedIndex(0)); // ????0??λ??

        JMenuItem parkingSpaceMenuItem = new JMenuItem("??λ????");
        parkingSpaceMenuItem.addActionListener(e -> tabbedPane.setSelectedIndex(1));

        JMenuItem dedicatedParkingMenuItem = new JMenuItem("??ó?λ????");
        dedicatedParkingMenuItem.addActionListener(e -> tabbedPane.setSelectedIndex(2));

        // ???????/???????????????
        JMenuItem specialReservationMenuItem = new JMenuItem("???/??????????");
        specialReservationMenuItem.addActionListener(e -> tabbedPane.setSelectedIndex(3));

        JMenuItem reservationManagementMenuItem = new JMenuItem("??λ???????");
        reservationManagementMenuItem.addActionListener(e -> tabbedPane.setSelectedIndex(4));

        JMenuItem parkingRecordMenuItem = new JMenuItem("??????");
        parkingRecordMenuItem.addActionListener(e -> tabbedPane.setSelectedIndex(5));

        JMenuItem statisticsMenuItem = new JMenuItem("??????");
        statisticsMenuItem.addActionListener(e -> tabbedPane.setSelectedIndex(6));

        functionMenu.add(vehicleManagementMenuItem); // ??????????
        functionMenu.add(parkingSpaceMenuItem);
        functionMenu.add(dedicatedParkingMenuItem);
        functionMenu.add(specialReservationMenuItem);
        functionMenu.add(reservationManagementMenuItem);
        functionMenu.add(parkingRecordMenuItem);
        functionMenu.add(statisticsMenuItem);

        // ?????
        JMenu systemMenu = new JMenu("??");
        JMenuItem userManagementMenuItem = new JMenuItem("???????");
        userManagementMenuItem.addActionListener(e -> tabbedPane.setSelectedIndex(7));

        JMenuItem settingMenuItem = new JMenuItem("??????");
        settingMenuItem.addActionListener(e -> tabbedPane.setSelectedIndex(8));

        JMenuItem aboutMenuItem = new JMenuItem("????");
        aboutMenuItem.addActionListener(e -> showAboutDialog());

        systemMenu.add(userManagementMenuItem);
        systemMenu.add(settingMenuItem);
        systemMenu.addSeparator();
        systemMenu.add(aboutMenuItem);

        // ????????????
        menuBar.add(fileMenu);
        menuBar.add(functionMenu);
        menuBar.add(systemMenu);

        setJMenuBar(menuBar);
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(
                this,
                "?????????????? \n\n" +
                        "?????Ч???????????????????????????λ??????λ?????????????????????????\n\n" +
                        "",
                "????",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}