package com.parkingmanagement.ui;

import com.parkingmanagement.dao.ParkingSpaceDAO;
import com.parkingmanagement.model.ParkingSpace;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ParkingStatusPanel extends JPanel {
    private ParkingSpaceDAO spaceDAO;
    private JTable spaceTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public ParkingStatusPanel() {
        setLayout(new BorderLayout());
        spaceDAO = new ParkingSpaceDAO();

        initializeUI();
        loadParkingSpaces();
    }

    private void initializeUI() {
        // 创建搜索面板
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        searchField = new JTextField(20);
        JButton searchButton = new JButton("查询");
        JButton refreshButton = new JButton("刷新");
        JButton backButton = new JButton("返回");

        searchButton.addActionListener(e -> searchParkingSpaces());
        refreshButton.addActionListener(e -> loadParkingSpaces());
        backButton.addActionListener(e -> {
            // 返回操作，这里可以根据实际情况实现返回逻辑
            // 例如隐藏当前面板，显示主菜单面板等
        });

        searchPanel.add(new JLabel("车位编号:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);
        searchPanel.add(backButton);

        // 创建表格
        String[] columnNames = {"车位ID", "车位编号", "车位类型", "车位状态", "是否可用"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        spaceTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(spaceTable);

        // 添加到面板
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
}