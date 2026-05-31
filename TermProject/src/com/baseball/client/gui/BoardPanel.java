package com.baseball.client.gui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.*;

public class BoardPanel {
    private JFrame frame;

    private JLabel[] lblScoreA = new JLabel[3];
    private JLabel[] lblScoreB = new JLabel[3];
    private JLabel lblTotalA;
    private JLabel lblTotalB;

    private JLabel lbl1Base, lbl2Base, lbl3Base;
    private JLabel lblStrike, lblBall, lblOut;
    private JLabel lblInningLabel;
    private JLabel lblInningNum;
    private JLabel lblInningArrow;
    private boolean isTop = true;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                BoardPanel window = new BoardPanel();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public BoardPanel() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("전광판");
        frame.setBounds(100, 100, 500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        // 이닝 구분선 패널 - 빈칸 제거
        JPanel scoreBoard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawLine(0, 0, 340, 0);
                g.drawLine(0, 25, 340, 25);
                g.drawLine(0, 50, 340, 50);
                g.drawLine(0, 75, 340, 75);
                g.drawLine(0, 100, 340, 100);  

                g.drawLine(0, 0, 0, 100);
                g.drawLine(60, 0, 60, 75);
                g.drawLine(130, 0, 130, 75);
                g.drawLine(200, 0, 200, 75);
                g.drawLine(270, 0, 270, 75);
                g.drawLine(340, 0, 340, 75);
            }
        };
        scoreBoard.setBounds(10, 5, 350, 100);
        scoreBoard.setOpaque(false);
        frame.getContentPane().add(scoreBoard);

        // 헤더 레이블 중앙 정렬
        String[] headers = {"1이닝", "2이닝", "3이닝", "합계"};
        for (int i = 0; i < 4; i++) {
            JLabel lbl = new JLabel(headers[i], SwingConstants.CENTER);
            lbl.setBounds(63 + i * 70, 10, 60, 20);
            frame.getContentPane().add(lbl);
        }

        // 팀A, 팀B 중앙 정렬
        JLabel lblTeamA = new JLabel("팀A", SwingConstants.CENTER);
        lblTeamA.setBounds(11, 35, 48, 20);
        frame.getContentPane().add(lblTeamA);

        JLabel lblTeamB = new JLabel("팀B", SwingConstants.CENTER);
        lblTeamB.setBounds(11, 60, 48, 20);
        frame.getContentPane().add(lblTeamB);

        // 이닝 점수 중앙 정렬
        for (int i = 0; i < 3; i++) {
            lblScoreA[i] = new JLabel("0", SwingConstants.CENTER);
            lblScoreA[i].setBounds(63 + i * 70, 35, 60, 20);
            frame.getContentPane().add(lblScoreA[i]);

            lblScoreB[i] = new JLabel("0", SwingConstants.CENTER);
            lblScoreB[i].setBounds(63 + i * 70, 60, 60, 20);
            frame.getContentPane().add(lblScoreB[i]);
        }

        // 합계
        lblTotalA = new JLabel("0", SwingConstants.CENTER);
        lblTotalA.setBounds(273, 35, 60, 20);
        frame.getContentPane().add(lblTotalA);

        lblTotalB = new JLabel("0", SwingConstants.CENTER);
        lblTotalB.setBounds(273, 60, 60, 20);
        frame.getContentPane().add(lblTotalB);

        // 이닝 + 화살표
        lblInningLabel = new JLabel("이닝:");
        lblInningLabel.setBounds(20, 100, 40, 25);
        frame.getContentPane().add(lblInningLabel);

        lblInningNum = new JLabel("1");
        lblInningNum.setBounds(62, 100, 15, 25);
        frame.getContentPane().add(lblInningNum);

        lblInningArrow = new JLabel("▲");
        lblInningArrow.setBounds(78, 100, 20, 25);
        frame.getContentPane().add(lblInningArrow);

        // SBO
        lblStrike = new JLabel("S: 0");
        lblStrike.setBounds(20, 130, 60, 25);
        frame.getContentPane().add(lblStrike);

        lblBall = new JLabel("B: 0");
        lblBall.setBounds(90, 130, 60, 25);
        frame.getContentPane().add(lblBall);

        lblOut = new JLabel("O: 0");
        lblOut.setBounds(160, 130, 60, 25);
        frame.getContentPane().add(lblOut);

        // 2루
        lbl2Base = new JLabel("◇");
        lbl2Base.setFont(new Font("굴림", Font.PLAIN, 30));
        lbl2Base.setBounds(230, 175, 40, 40);
        frame.getContentPane().add(lbl2Base);

        // 1루
        lbl1Base = new JLabel("◇");
        lbl1Base.setFont(new Font("굴림", Font.PLAIN, 30));
        lbl1Base.setBounds(280, 215, 40, 40);
        frame.getContentPane().add(lbl1Base);

        // 3루
        lbl3Base = new JLabel("◇");
        lbl3Base.setFont(new Font("굴림", Font.PLAIN, 30));
        lbl3Base.setBounds(180, 215, 40, 40);
        frame.getContentPane().add(lbl3Base);

        // 홈베이스 
        JPanel homeBase = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                int size = 14;

                int[] xPoints = {cx, cx + size, cx, cx - size};
                int[] yPoints = {cy - size, cy, cy + size, cy};

                g2.setClip(new java.awt.Polygon(xPoints, yPoints, 4));
                for (int i = 0; i < 6; i++) {
                    g2.setColor(i % 2 == 0 ? Color.WHITE : Color.LIGHT_GRAY);
                    g2.fillRect(0, i * getHeight() / 6, getWidth(), getHeight() / 6);
                }

                g2.setClip(null);
                g2.setColor(Color.BLACK);
                g2.drawPolygon(xPoints, yPoints, 4);
            }
        };
        homeBase.setBounds(223, 255, 40, 40);
        homeBase.setOpaque(false);
        frame.getContentPane().add(homeBase);
    }

    public void updateScore(int inning, int scoreA, int scoreB) {
        lblScoreA[inning].setText(String.valueOf(scoreA));
        lblScoreB[inning].setText(String.valueOf(scoreB));

        // 합계 자동 계산
        int totalA = 0, totalB = 0;
        for (int i = 0; i < 3; i++) {
            totalA += Integer.parseInt(lblScoreA[i].getText());
            totalB += Integer.parseInt(lblScoreB[i].getText());
        }
        lblTotalA.setText(String.valueOf(totalA));
        lblTotalB.setText(String.valueOf(totalB));
    }

    public void updateBase(boolean first, boolean second, boolean third) {
        lbl1Base.setText(first ? "◆" : "◇");
        lbl2Base.setText(second ? "◆" : "◇");
        lbl3Base.setText(third ? "◆" : "◇");
    }

    public void updateSBO(int strike, int ball, int out) {
        lblStrike.setText("S: " + strike);
        lblBall.setText("B: " + ball);
        lblOut.setText("O: " + out);
    }

    public void updateInningArrow(int inning, boolean top) {
        isTop = top;
        lblInningNum.setText(String.valueOf(inning));
        lblInningArrow.setText(top ? "▲" : "▼");
    }
}