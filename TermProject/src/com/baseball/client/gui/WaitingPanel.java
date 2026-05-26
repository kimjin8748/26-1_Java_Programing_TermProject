package com.baseball.client.gui;

import java.awt.EventQueue;
import javax.swing.*;

public class WaitingPanel {

    private JFrame frame;
    private JLabel lblWaiting;
    private String studentId;
    private String name;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                WaitingPanel window = new WaitingPanel("", "");
                window.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public WaitingPanel(String studentId, String name) {
        this.studentId = studentId;
        this.name = name;
        initialize();
    }

    public void show() {
        frame.setVisible(true);
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("야구 게임 - 대기중");
        frame.setBounds(100, 100, 400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        // 이름 표시
        JLabel lblName = new JLabel("[ " + name + " / " + studentId + " ]", SwingConstants.CENTER);
        lblName.setBounds(0, 60, 400, 30);
        frame.getContentPane().add(lblName);

        // 대기 메시지
        lblWaiting = new JLabel("상대방을 기다리는 중.", SwingConstants.CENTER);
        lblWaiting.setBounds(0, 110, 400, 30);
        frame.getContentPane().add(lblWaiting);

        JLabel lblSub = new JLabel("상대방이 접속하면 게임이 시작됩니다.", SwingConstants.CENTER);
        lblSub.setBounds(0, 150, 400, 25);
        frame.getContentPane().add(lblSub);

        // 점 애니메이션
        Timer timer = new Timer(500, null);
        timer.addActionListener(e -> {
            String text = lblWaiting.getText();
            if (text.endsWith("...")) {
                lblWaiting.setText("상대방을 기다리는 중.");
            } else {
                lblWaiting.setText(text + ".");
            }
        });
        timer.start();
    }
}
