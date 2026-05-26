package com.baseball.client.gui;

import javax.swing.*;

public class WaitingPanel extends JPanel {

    private JLabel lblWaiting;
    private JLabel lblName;


    public WaitingPanel() {
        initialize();
    }

    private void initialize() {
    	this.setLayout(null);
        this.setSize(400, 300); // 크기 지정

        // 이름 표시 라벨 (처음에는 빈칸으로 둡니다)
        lblName = new JLabel("[ 대기 중... ]", SwingConstants.CENTER);
        lblName.setBounds(0, 60, 400, 30);
        this.add(lblName); // frame.getContentPane().add() -> this.add()로 변경

        // 대기 메시지
        lblWaiting = new JLabel("상대방을 기다리는 중.", SwingConstants.CENTER);
        lblWaiting.setBounds(0, 110, 400, 30);
        this.add(lblWaiting);

        JLabel lblSub = new JLabel("상대방이 접속하면 게임이 시작됩니다.", SwingConstants.CENTER);
        lblSub.setBounds(0, 150, 400, 25);
        this.add(lblSub);

        // 점 애니메이션
        Timer timer = new Timer(500, e -> {
            String text = lblWaiting.getText();
            if (text.endsWith("...")) {
                lblWaiting.setText("상대방을 기다리는 중.");
            } else {
                lblWaiting.setText(text + ".");
            }
        });
        timer.start();
    }
    public void setPlayerInfo(String username) {
        lblName.setText("[ " + username + " ]");
    }
}
