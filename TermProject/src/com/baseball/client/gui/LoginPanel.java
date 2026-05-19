package com.baseball.client.gui;

import javax.swing.*;

import java.awt.EventQueue;

public class LoginPanel {

    private JFrame frame;
    private JTextField textFieldIP;
    private JTextField textFieldNickname;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                LoginPanel window = new LoginPanel();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public LoginPanel() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("야구 게임 - 접속");
        frame.setBounds(100, 100, 450, 397);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        // 서버 IP 레이블
        JLabel lblIP = new JLabel("서버 IP:");
        lblIP.setBounds(12, 10, 134, 72);
        frame.getContentPane().add(lblIP);

        // 서버 IP 입력칸
        textFieldIP = new JTextField();
        textFieldIP.setBounds(226, 36, 112, 21);
        textFieldIP.setColumns(10);
        frame.getContentPane().add(textFieldIP);

        // 닉네임 레이블
        JLabel lblNickname = new JLabel("닉네임:");
        lblNickname.setBounds(12, 96, 134, 77);
        frame.getContentPane().add(lblNickname);

        // 닉네임 입력칸
        textFieldNickname = new JTextField();
        textFieldNickname.setBounds(226, 124, 117, 21);
        textFieldNickname.setColumns(10);
        frame.getContentPane().add(textFieldNickname);

        // 투수 라디오버튼
        JRadioButton rdbtnPitcher = new JRadioButton("투수");
        rdbtnPitcher.setBounds(12, 151, 49, 22);
        frame.getContentPane().add(rdbtnPitcher);

        // 타자 라디오버튼
        JRadioButton rdbtnBatter = new JRadioButton("타자");
        rdbtnBatter.setBounds(225, 151, 49, 22);
        frame.getContentPane().add(rdbtnBatter);

        // ButtonGroup으로 묶기 (하나만 선택되게)
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(rdbtnPitcher);
        buttonGroup.add(rdbtnBatter);

        // 접속 버튼
        JButton btnConnect = new JButton("접속");
        btnConnect.setBounds(12, 183, 304, 21);
        frame.getContentPane().add(btnConnect);

        // 접속 버튼 이벤트
        btnConnect.addActionListener(e -> {
            String ip = textFieldIP.getText();
            String nickname = textFieldNickname.getText();

            // 입력값 검증
            if (ip.isEmpty() || nickname.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "IP와 닉네임을 입력해주세요!");
                return;
            }
            if (!rdbtnPitcher.isSelected() && !rdbtnBatter.isSelected()) {
                JOptionPane.showMessageDialog(frame, "투수 또는 타자를 선택해주세요!");
                return;
            }

            boolean isPitcher = rdbtnPitcher.isSelected();
            System.out.println("접속 시도: " + ip + " / " + nickname + " / " + (isPitcher ? "투수" : "타자"));

            // TODO: 나중에 여기서 ServerConnection 연결 코드 추가
        });
    }
}