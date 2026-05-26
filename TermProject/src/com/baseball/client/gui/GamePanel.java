package com.baseball.client.gui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class GamePanel {

    private JFrame frame;

    // 투수
    private JSlider sliderSpeed;
    private JComboBox<String> comboPitch;
    private JButton btnPitch;
    private JLabel lblSpeedValue;
    private JPanel pitcherOverlay;

    // 타자
    private JComboBox<String> comboHitType;
    private JButton btnHit;
    private JPanel batterOverlay;

    // 타이밍 바
    private Timer timingTimer;
    private int timingPos = 0;
    private int timingDir = 1;
    private int timingLap = 0;
    private boolean timingActive = false;
    private boolean timingDone = false;
    private int stoppedPos = -1;
    private JPanel timingBarPanel;
    private JLabel lblTimingResult;

    // 전광판
    private JLabel[] lblScoreA = new JLabel[3];
    private JLabel[] lblScoreB = new JLabel[3];
    private JLabel lblTotalA, lblTotalB;
    private JLabel lbl1Base, lbl2Base, lbl3Base;
    private JLabel lblStrike, lblBall, lblOut;
    private JLabel lblInningNum, lblInningArrow;

    // 대화창
    private JTextArea txtGameLog;

    // 역할
    private boolean isPitcher = true;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                GamePanel window = new GamePanel();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public GamePanel() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("야구 게임");
        frame.setBounds(100, 100, 800, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        // ── 상단 전광판 ──
        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(null);
        boardPanel.setBounds(0, 0, 800, 200);
        boardPanel.setBorder(BorderFactory.createTitledBorder("전광판"));
        frame.getContentPane().add(boardPanel);

        JPanel scoreBoard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawLine(0, 0, 340, 0);
                g.drawLine(0, 25, 340, 25);
                g.drawLine(0, 50, 340, 50);
                g.drawLine(0, 75, 340, 75);
                g.drawLine(0, 0, 0, 75);
                g.drawLine(60, 0, 60, 75);
                g.drawLine(130, 0, 130, 75);
                g.drawLine(200, 0, 200, 75);
                g.drawLine(270, 0, 270, 75);
                g.drawLine(340, 0, 340, 75);
            }
        };
        scoreBoard.setBounds(20, 20, 350, 75);
        scoreBoard.setOpaque(false);
        boardPanel.add(scoreBoard);

        String[] headers = {"1이닝", "2이닝", "3이닝", "합계"};
        for (int i = 0; i < 4; i++) {
            JLabel lbl = new JLabel(headers[i], SwingConstants.CENTER);
            lbl.setBounds(83 + i * 70, 25, 60, 20);
            boardPanel.add(lbl);
        }

        JLabel lblTeamA = new JLabel("팀A", SwingConstants.CENTER);
        lblTeamA.setBounds(21, 55, 48, 20);
        boardPanel.add(lblTeamA);

        JLabel lblTeamB = new JLabel("팀B", SwingConstants.CENTER);
        lblTeamB.setBounds(21, 80, 48, 20);
        boardPanel.add(lblTeamB);

        for (int i = 0; i < 3; i++) {
            lblScoreA[i] = new JLabel("0", SwingConstants.CENTER);
            lblScoreA[i].setBounds(83 + i * 70, 55, 60, 20);
            boardPanel.add(lblScoreA[i]);

            lblScoreB[i] = new JLabel("0", SwingConstants.CENTER);
            lblScoreB[i].setBounds(83 + i * 70, 80, 60, 20);
            boardPanel.add(lblScoreB[i]);
        }

        lblTotalA = new JLabel("0", SwingConstants.CENTER);
        lblTotalA.setBounds(283, 55, 60, 20);
        boardPanel.add(lblTotalA);

        lblTotalB = new JLabel("0", SwingConstants.CENTER);
        lblTotalB.setBounds(283, 80, 60, 20);
        boardPanel.add(lblTotalB);

        JLabel lblInningLabel = new JLabel("이닝:");
        lblInningLabel.setBounds(420, 25, 40, 25);
        boardPanel.add(lblInningLabel);

        lblInningNum = new JLabel("1");
        lblInningNum.setBounds(462, 25, 15, 25);
        boardPanel.add(lblInningNum);

        lblInningArrow = new JLabel("▲");
        lblInningArrow.setBounds(478, 25, 20, 25);
        boardPanel.add(lblInningArrow);

        lblStrike = new JLabel("S: 0");
        lblStrike.setBounds(420, 55, 50, 25);
        boardPanel.add(lblStrike);

        lblBall = new JLabel("B: 0");
        lblBall.setBounds(480, 55, 50, 25);
        boardPanel.add(lblBall);

        lblOut = new JLabel("O: 0");
        lblOut.setBounds(540, 55, 50, 25);
        boardPanel.add(lblOut);

        lbl2Base = new JLabel("◇");
        lbl2Base.setFont(new Font("굴림", Font.PLAIN, 25));
        lbl2Base.setBounds(630, 30, 35, 35);
        boardPanel.add(lbl2Base);

        lbl1Base = new JLabel("◇");
        lbl1Base.setFont(new Font("굴림", Font.PLAIN, 25));
        lbl1Base.setBounds(665, 65, 35, 35);
        boardPanel.add(lbl1Base);

        lbl3Base = new JLabel("◇");
        lbl3Base.setFont(new Font("굴림", Font.PLAIN, 25));
        lbl3Base.setBounds(595, 65, 35, 35);
        boardPanel.add(lbl3Base);

        JPanel homeBase = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                int size = 12;
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
        homeBase.setBounds(623, 100, 35, 35);
        homeBase.setOpaque(false);
        boardPanel.add(homeBase);

        // ── 투수 컨테이너 ──
        JPanel pitcherContainer = new JPanel();
        pitcherContainer.setLayout(null);
        pitcherContainer.setBounds(0, 200, 400, 350);
        frame.getContentPane().add(pitcherContainer);

        JPanel pitcherPanel = new JPanel();
        pitcherPanel.setLayout(null);
        pitcherPanel.setBounds(0, 0, 400, 350);
        pitcherPanel.setBorder(new TitledBorder("투수"));
        pitcherContainer.add(pitcherPanel);

        JLabel lblSpeed = new JLabel("구속:");
        lblSpeed.setBounds(30, 40, 50, 25);
        pitcherPanel.add(lblSpeed);

        lblSpeedValue = new JLabel("80 km/h");
        lblSpeedValue.setForeground(new Color(0, 200, 0));
        lblSpeedValue.setFont(new Font("굴림", Font.BOLD, 14));
        lblSpeedValue.setBounds(150, 40, 80, 25);
        pitcherPanel.add(lblSpeedValue);

        sliderSpeed = new JSlider(JSlider.HORIZONTAL, 0, 160, 80);
        sliderSpeed.setBounds(20, 70, 340, 50);
        sliderSpeed.setMajorTickSpacing(40);
        sliderSpeed.setMinorTickSpacing(10);
        sliderSpeed.setPaintTicks(true);
        sliderSpeed.setSnapToTicks(true);

        java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<>();
        labelTable.put(0,   new JLabel("0"));
        labelTable.put(40,  new JLabel("40"));
        labelTable.put(80,  new JLabel("80"));
        labelTable.put(120, new JLabel("120"));
        labelTable.put(160, new JLabel("160"));
        sliderSpeed.setLabelTable(labelTable);
        sliderSpeed.setPaintLabels(true);
        pitcherPanel.add(sliderSpeed);

        sliderSpeed.addChangeListener(e -> {
            int val = sliderSpeed.getValue();
            lblSpeedValue.setText(val + " km/h");
            int r, g, b;
            if (val <= 80) {
                float ratio = val / 80.0f;
                r = 0;
                g = (int)(ratio * 200);
                b = (int)((1 - ratio) * 255);
            } else {
                float ratio = (val - 80) / 80.0f;
                r = (int)(ratio * 255);
                g = (int)((1 - ratio) * 200);
                b = 0;
            }
            lblSpeedValue.setForeground(new Color(r, g, b));
        });

        JLabel lblPitch = new JLabel("구질:");
        lblPitch.setBounds(30, 140, 50, 25);
        pitcherPanel.add(lblPitch);

        comboPitch = new JComboBox<>(new String[]{"직구", "커브", "슬라이더"});
        comboPitch.setBounds(90, 140, 150, 25);
        pitcherPanel.add(comboPitch);

        btnPitch = new JButton("투구!");
        btnPitch.setBounds(100, 220, 150, 40);
        btnPitch.setFont(new Font("굴림", Font.BOLD, 16));
        pitcherPanel.add(btnPitch);

        btnPitch.addActionListener(e -> {
            String pitch = (String) comboPitch.getSelectedItem();
            addGameLog("투구: " + lblSpeedValue.getText() + " / " + pitch);
        });

        // 투수 오버레이
        pitcherOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        pitcherOverlay.setBounds(0, 0, 400, 350);
        pitcherOverlay.setOpaque(false);
        pitcherOverlay.setVisible(!isPitcher);
        pitcherContainer.add(pitcherOverlay);
        pitcherContainer.setComponentZOrder(pitcherOverlay, 0);

        // ── 타자 컨테이너 ──
        JPanel batterContainer = new JPanel();
        batterContainer.setLayout(null);
        batterContainer.setBounds(400, 200, 400, 350);
        frame.getContentPane().add(batterContainer);

        JPanel batterPanel = new JPanel();
        batterPanel.setLayout(null);
        batterPanel.setBounds(0, 0, 400, 350);
        batterPanel.setBorder(new TitledBorder("타자"));
        batterContainer.add(batterPanel);

        JLabel lblHitType = new JLabel("치는 방법:");
        lblHitType.setBounds(30, 30, 80, 25);
        batterPanel.add(lblHitType);

        comboHitType = new JComboBox<>(new String[]{"당겨치기", "밀어치기", "직선타"});
        comboHitType.setBounds(120, 30, 150, 25);
        batterPanel.add(comboHitType);

        // 타이밍 바
        JLabel lblTiming = new JLabel("타이밍:");
        lblTiming.setBounds(30, 80, 60, 25);
        batterPanel.add(lblTiming);

        timingBarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int barH = 20;
                int barY = h / 2 - barH / 2;

                // 무지개 그라데이션
                Color[] rainbow = {
                    Color.RED, Color.ORANGE, Color.YELLOW,
                    Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA
                };
                int segW = w / (rainbow.length - 1);
                for (int i = 0; i < rainbow.length - 1; i++) {
                    GradientPaint gp = new GradientPaint(
                        i * segW, 0, rainbow[i],
                        (i + 1) * segW, 0, rainbow[i + 1]
                    );
                    g2.setPaint(gp);
                    g2.fillRect(i * segW, barY, segW + 1, barH);
                }

                // 테두리
                g2.setColor(Color.DARK_GRAY);
                g2.setPaint(Color.DARK_GRAY);
                g2.drawRect(0, barY, w - 1, barH);

                // 정중간 기준선
                g2.setColor(Color.WHITE);
                g2.setStroke(new java.awt.BasicStroke(2));
                g2.drawLine(w / 2, barY - 5, w / 2, barY + barH + 5);

                // 움직이는 선 또는 멈춘 선
                if (timingDone && stoppedPos >= 0) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new java.awt.BasicStroke(3));
                    int px = (int)((stoppedPos / 200.0) * w);
                    g2.drawLine(px, barY - 8, px, barY + barH + 8);
                } else if (timingActive) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new java.awt.BasicStroke(3));
                    int px = (int)((timingPos / 200.0) * w);
                    g2.drawLine(px, barY - 8, px, barY + barH + 8);
                }

                // 레이블
                g2.setColor(Color.DARK_GRAY);
                g2.setFont(new Font("굴림", Font.PLAIN, 11));
                g2.drawString("빠르게", 2, barY - 6);
                g2.drawString("늦게", w - 35, barY - 6);
                g2.drawString("정확", w / 2 - 12, barY + barH + 15);
            }
        };
        timingBarPanel.setBounds(20, 110, 340, 60);
        timingBarPanel.setOpaque(false);
        batterPanel.add(timingBarPanel);

        // 타이밍 결과
        lblTimingResult = new JLabel("", SwingConstants.CENTER);
        lblTimingResult.setBounds(20, 175, 340, 25);
        lblTimingResult.setFont(new Font("굴림", Font.BOLD, 13));
        batterPanel.add(lblTimingResult);

        // 타격 버튼
        btnHit = new JButton("타격!");
        btnHit.setBounds(100, 220, 150, 40);
        btnHit.setFont(new Font("굴림", Font.BOLD, 16));
        batterPanel.add(btnHit);

        // 타이밍 타이머
        timingTimer = new Timer(8, e -> {
            timingPos += timingDir;
            if (timingPos >= 200) {
                timingPos = 200;
                timingDir = -1;
                timingLap++;
            } else if (timingPos <= 0) {
                timingPos = 0;
                timingDir = 1;
                timingLap++;
            }
            // 3번 왕복 (6번 방향 전환) 후 자동 종료
            if (timingLap >= 6) {
                timingTimer.stop();
                timingActive = false;
                timingDone = true;
                stoppedPos = -1;
                lblTimingResult.setText("⚾ 그냥 바라봤습니다...");
                lblTimingResult.setForeground(Color.GRAY);
                addGameLog("타자가 공을 그냥 바라봤습니다.");
            }
            timingBarPanel.repaint();
        });

        btnHit.addActionListener(e -> {
            // 타이밍 바 시작 (아직 시작 안했으면)
            if (!timingActive && !timingDone) {
                timingPos = 0;
                timingDir = 1;
                timingLap = 0;
                timingActive = true;
                timingDone = false;
                stoppedPos = -1;
                lblTimingResult.setText("");
                timingTimer.start();
                btnHit.setText("멈춰!");
                return;
            }

            // 타이밍 멈추기
            if (timingActive) {
                timingTimer.stop();
                timingActive = false;
                timingDone = true;
                stoppedPos = timingPos;

                String hitType = (String) comboHitType.getSelectedItem();
                int diff = stoppedPos - 100;
                String timing;
                Color resultColor;

                if (Math.abs(diff) <= 10) {
                    timing = "완벽한 타이밍!";
                    resultColor = Color.GREEN;
                } else if (diff < -10) {
                    timing = "빠르게 쳤습니다!";
                    resultColor = Color.BLUE;
                } else {
                    timing = "늦게 쳤습니다!";
                    resultColor = Color.RED;
                }

                lblTimingResult.setText(timing);
                lblTimingResult.setForeground(resultColor);
                addGameLog("타격: " + hitType + " / " + timing);
                btnHit.setText("타격!");

                timingBarPanel.repaint();
            }
        });

        // 타자 오버레이
        batterOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        batterOverlay.setBounds(0, 0, 400, 350);
        batterOverlay.setOpaque(false);
        batterOverlay.setVisible(isPitcher);
        batterContainer.add(batterOverlay);
        batterContainer.setComponentZOrder(batterOverlay, 0);

        // ── 하단 대화창 ──
        JPanel logPanel = new JPanel();
        logPanel.setLayout(null);
        logPanel.setBounds(0, 550, 800, 110);
        logPanel.setBorder(BorderFactory.createTitledBorder("게임 진행"));
        frame.getContentPane().add(logPanel);

        txtGameLog = new JTextArea();
        txtGameLog.setEditable(false);
        txtGameLog.setFont(new Font("굴림", Font.PLAIN, 13));
        txtGameLog.setBackground(new Color(240, 240, 240));

        JScrollPane scrollPane = new JScrollPane(txtGameLog);
        scrollPane.setBounds(10, 20, 775, 80);
        logPanel.add(scrollPane);

        // 초기 상태 설정
        setPitcherEnabled(isPitcher);
        setBatterEnabled(!isPitcher);
     // 테스트용 공수전환 버튼 (나중에 서버 연동하면 삭제)
        JButton btnSwitch = new JButton("공수전환 테스트");
        btnSwitch.setBounds(300, 260, 160, 30);
        frame.getContentPane().add(btnSwitch);
        btnSwitch.addActionListener(e -> switchRole());
    }

    // 투수 컴포넌트 활성화/비활성화
    private void setPitcherEnabled(boolean enabled) {
        sliderSpeed.setEnabled(enabled);
        comboPitch.setEnabled(enabled);
        btnPitch.setEnabled(enabled);
    }

    // 타자 컴포넌트 활성화/비활성화
    private void setBatterEnabled(boolean enabled) {
        comboHitType.setEnabled(enabled);
        btnHit.setEnabled(enabled);
        if (!enabled && timingTimer != null) {
            timingTimer.stop();
            timingActive = false;
        }
    }

    // 역할 전환
    public void switchRole() {
        isPitcher = !isPitcher;
        pitcherOverlay.setVisible(!isPitcher);
        batterOverlay.setVisible(isPitcher);
        setPitcherEnabled(isPitcher);
        setBatterEnabled(!isPitcher);

        // 타이밍 바 초기화
        timingPos = 0;
        timingDir = 1;
        timingLap = 0;
        timingActive = false;
        timingDone = false;
        stoppedPos = -1;
        lblTimingResult.setText("");
        btnHit.setText("타격!");
        timingBarPanel.repaint();
    }

    public void addGameLog(String message) {
        txtGameLog.append(message + "\n");
        txtGameLog.setCaretPosition(txtGameLog.getDocument().getLength());
    }

    public void updateScore(int inning, int scoreA, int scoreB) {
        lblScoreA[inning].setText(String.valueOf(scoreA));
        lblScoreB[inning].setText(String.valueOf(scoreB));
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

    public void updateInning(int inning, boolean top) {
        lblInningNum.setText(String.valueOf(inning));
        lblInningArrow.setText(top ? "▲" : "▼");
    }
}