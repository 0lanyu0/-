package com.parkingmanagement.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

public class FeeRechargePanel extends JPanel {
    private JTextField customAmountField;
    private JLabel balanceLabel;
    private double currentBalance = 372.83; // 初始余额

    public FeeRechargePanel() {
        setLayout(new BorderLayout(10, 10));
        initializeUI();
    }

    private void initializeUI() {
        // 顶部提示
        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        JLabel titleLabel = new JLabel("请选择充值套餐", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        topPanel.add(titleLabel);

        // 自定义金额输入（新增充值按钮）
        JPanel customPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        customPanel.add(new JLabel("自定义金额:"));
        customAmountField = new JTextField(10);
        customPanel.add(customAmountField);
        customPanel.add(new JLabel("元"));

        // 新增：自定义金额充值按钮
        JButton customRechargeBtn = new JButton("充值");
        customPanel.add(customRechargeBtn);
        customRechargeBtn.addActionListener(new RechargeActionListener());

        // 充值按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton btn20 = new JButton("20元");
        JButton btn30 = new JButton("30元(+送2元)");
        JButton btn50 = new JButton("50元(+送5元)");
        JButton btn70 = new JButton("70元(+送7元)");
        JButton btn100 = new JButton("100元(+送10元)");
        JButton btn200 = new JButton("200元(+送35元)");

        // 为所有按钮添加相同的事件监听器
        RechargeActionListener listener = new RechargeActionListener();
        btn20.addActionListener(listener);
        btn30.addActionListener(listener);
        btn50.addActionListener(listener);
        btn70.addActionListener(listener);
        btn100.addActionListener(listener);
        btn200.addActionListener(listener);

        buttonPanel.add(btn20);
        buttonPanel.add(btn30);
        buttonPanel.add(btn50);
        buttonPanel.add(btn70);
        buttonPanel.add(btn100);
        buttonPanel.add(btn200);

        // 余额显示
        balanceLabel = new JLabel("您当前余额为 " + currentBalance + " 元", JLabel.CENTER);
        balanceLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));

        // 调整布局，使用 BoxLayout 垂直排列各组件
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(customPanel);
        centerPanel.add(buttonPanel);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(balanceLabel, BorderLayout.SOUTH);
    }

    // 充值按钮点击事件处理 - 弹出包含图片和文字的对话框
    private class RechargeActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 获取图片资源
            URL imageUrl = getClass().getClassLoader().getResource("com/erweima.jpg");
            ImageIcon icon;
            if (imageUrl != null) {
                icon = new ImageIcon(imageUrl);
                // 调整图片大小（可根据需要修改尺寸）
                Image scaledImage = icon.getImage().getScaledInstance(400, 400, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaledImage);
            } else {
                icon = new ImageIcon();
                System.err.println("无法找到图片资源: com/erweima.jpg");
            }

            // 创建弹窗内容面板
            JPanel dialogPanel = new JPanel(new BorderLayout(0, 20));
            dialogPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // 顶部文字
            JLabel titleLabel = new JLabel("请使用微信扫码支付", JLabel.CENTER);
            titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
            dialogPanel.add(titleLabel, BorderLayout.NORTH);

            // 中间图片
            JLabel imageLabel = new JLabel(icon);
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            dialogPanel.add(imageLabel, BorderLayout.CENTER);

            // 显示弹窗（增大弹窗大小）
            JOptionPane.showMessageDialog(
                    FeeRechargePanel.this,
                    dialogPanel,
                    "充值确认",
                    JOptionPane.INFORMATION_MESSAGE,
                    null // 不使用默认图标
            );
        }
    }

    // 更新余额显示
    public void updateBalance(double newBalance) {
        currentBalance = newBalance;
        balanceLabel.setText("您当前余额为 " + currentBalance + " 元");
    }
}