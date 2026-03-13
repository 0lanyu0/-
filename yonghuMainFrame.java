package com.parkingmanagement.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class yonghuMainFrame extends JFrame {
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private yonghuReservationPanel reservationPanel;
    private ParkingStatusPanel parkingStatusPanel;
    private PersonalInfoPanel personalInfoPanel;
    private ParkingLotInfoDisplayPanel parkingLotInfoPanel;
    private JPanel moreInfoPanel;
    // 新增两个面板引用
    private ParkingFeeQueryPanel parkingFeeQueryPanel;
    private FeeRechargePanel feeRechargePanel;

    public yonghuMainFrame() {
        setTitle("专用/永久专用车位系统");
        setSize(1200, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initializeUI();
    }

    private void initializeUI() {
        // 初始化卡片布局管理器
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        // 初始化各个面板
        reservationPanel = new yonghuReservationPanel();
        parkingStatusPanel = new ParkingStatusPanel();
        personalInfoPanel = new PersonalInfoPanel();
        parkingLotInfoPanel = new ParkingLotInfoDisplayPanel();
        moreInfoPanel = createPlaceholderPanel();
        // 初始化新增面板
        parkingFeeQueryPanel = new ParkingFeeQueryPanel();
        feeRechargePanel = new FeeRechargePanel();

        // 将各个面板添加到主面板（新面板放在个人信息面板之前）
        mainPanel.add(reservationPanel, "reservation");
        mainPanel.add(parkingStatusPanel, "parkingStatus");
        mainPanel.add(parkingFeeQueryPanel, "parkingFeeQuery");
        mainPanel.add(feeRechargePanel, "feeRecharge");
        mainPanel.add(personalInfoPanel, "personalInfo");
        mainPanel.add(parkingLotInfoPanel, "parkingLotInfo");
        mainPanel.add(moreInfoPanel, "moreInfo");

        // 创建欢迎信息面板
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(41, 128, 185));
        topPanel.setPreferredSize(new Dimension(800, 50));
        JLabel welcomeLabel = new JLabel("欢迎使用专用/永久专用车位系统");
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        welcomeLabel.setForeground(Color.WHITE);
        topPanel.add(welcomeLabel);

        // 创建功能按钮面板
        JPanel buttonPanel = createButtonPanel();

        // 组装界面
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(buttonPanel, BorderLayout.WEST);
        contentPanel.add(mainPanel, BorderLayout.CENTER);
        add(contentPanel);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(7, 1, 10, 10)); // 调整为7行以容纳新增按钮
        panel.setPreferredSize(new Dimension(200, getHeight()));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton reservationButton = new JButton("预约/取消专用车位预约");
        reservationButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        reservationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "reservation");
            }
        });

        JButton parkingStatusButton = new JButton("车位状态");
        parkingStatusButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        parkingStatusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "parkingStatus");
            }
        });

        // 新增停车费用查询按钮
        JButton parkingFeeQueryButton = new JButton("停车费用查询");
        parkingFeeQueryButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        parkingFeeQueryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "parkingFeeQuery");
            }
        });

        // 新增费用充值按钮
        JButton feeRechargeButton = new JButton("费用充值");
        feeRechargeButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        feeRechargeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "feeRecharge");
            }
        });

        JButton personalInfoButton = new JButton("个人信息");
        personalInfoButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        personalInfoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "personalInfo");
            }
        });

        JButton parkingLotInfoButton = new JButton("停车场信息");
        parkingLotInfoButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        parkingLotInfoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "parkingLotInfo");
            }
        });

        JButton moreInfoButton = new JButton("更多信息");
        moreInfoButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        moreInfoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "moreInfo");
            }
        });

        // 按顺序添加按钮（新按钮位于个人信息按钮上方）
        panel.add(reservationButton);
        panel.add(parkingStatusButton);
        panel.add(parkingFeeQueryButton);
        panel.add(feeRechargeButton);
        panel.add(personalInfoButton);
        panel.add(parkingLotInfoButton);
        panel.add(moreInfoButton);

        return panel;
    }

    private JPanel createPlaceholderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        String moreInfoText = " 系统更多信息\n" +
                "\n" +
                "\n" +
                "    首次停车需登记您的车辆以及您的个人信息，您的信息将被严密保存，不会对外公布，您的信息仅便于后续您的便捷使用，还请谅解！\n" +
                "    用户账号的密码采用哈希加密，能够严格保障您的账户安全。\n" +
                "    若存在账号异常、锁定等问题请联系停车场管理员解决，通过重置密码保障您的隐私安全！\n " +
                "    车位共有四类：永久专用车位，专用车位，临时车位，普通车位。使用永久专用车位或专用车位需登记您的个人信息，等级结束后车位将为您保留，可随时使用您的车位。\n" +
                "    本智慧停车系统由兰州信息科技学院22级软件工程学生开发，该停车场支持便捷预约，您可随时通过系统快速选定心仪车位，灵活安排停车时段。价格实惠，前2小时仅需10元，后续每小时5元，每日最高收费100元封顶，性价比高。场内布局合理，车位标识清晰，出入通道顺畅，配套设施完善，为您提供优质停车环境，让您停车无忧，轻松享受高效、经济的停车服务。\n" +
                "    若您登记的车位信息存在严重错误，请及时联系停车场工作人员，或拨打12345678以及通过邮箱12345678@parking.com与我们联系。感谢您的使用！\n" +
                "    若您发现系统存在漏洞问题或有优化的地方欢迎投稿至1478356846@qq.com\n" +
                "    感谢您给予的包容！\n" +
                "  致谢！" +
                "                                                                                                                                                                                                                                                                                                                                                                                                                                                 设计来自软件三班的三位同学\n" +
                "                                                                                                                                         by 2025年6月\n" +
                "";
        JTextArea textArea = new JTextArea(moreInfoText);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        textArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        textArea.setAlignmentY(Component.CENTER_ALIGNMENT);
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            yonghuMainFrame frame = new yonghuMainFrame();
            frame.setVisible(true);
        });
    }
}