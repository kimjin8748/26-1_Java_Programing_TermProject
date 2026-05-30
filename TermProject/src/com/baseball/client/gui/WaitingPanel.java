package com.baseball.client.gui;

import javax.swing.*;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Font;
import java.awt.Insets;

public class WaitingPanel extends JPanel {

    private JLabel lblWaiting;
    private JLabel lblName;

    public WaitingPanel() {
        initialize();
    }

    private void initialize() {
        this.setLayout(new GridBagLayout());

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        // 이름 레이블
        lblName = new JLabel("[ 대기 중... ]", SwingConstants.CENTER);
        lblName.setFont(new Font("굴림", Font.BOLD, 20));
        gbc.gridy = 0;
        centerPanel.add(lblName, gbc);

        // 대기 메시지
        lblWaiting = new JLabel("상대방을 기다리는 중.", SwingConstants.CENTER);
        lblWaiting.setFont(new Font("굴림", Font.PLAIN, 16));
        gbc.gridy = 1;
        centerPanel.add(lblWaiting, gbc);

        // 안내 메시지
        JLabel lblSub = new JLabel("상대방이 접속하면 게임이 시작됩니다.", SwingConstants.CENTER);
        lblSub.setFont(new Font("굴림", Font.PLAIN, 14));
        gbc.gridy = 2;
        centerPanel.add(lblSub, gbc);

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

        this.add(centerPanel, new GridBagConstraints());
    }

    public void setPlayerInfo(String username) {
        lblName.setText("[ " + username + " ]");
    }
}