package com.baseball.client.gui;

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import com.baseball.client.controller.GameController;
import com.baseball.common.model.GameState;
import com.baseball.common.protocol.GameMessage;
import com.baseball.common.protocol.MessageType;

public class GamePanel extends JPanel {

    private GameController controller;

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
    private JLabel lblGuide;

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

    public GamePanel(GameController controller) {
        this.controller = controller;
        initialize();
    }

    private void initialize() {
        this.setLayout(null);
        this.setSize(800, 700);

        // ── 상단 전광판 ──
        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(null);
        boardPanel.setBounds(0, 0, 800, 200);
        boardPanel.setBackground(new Color(20, 20, 20));
        boardPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)),
            "전광판",
            0, 0,
            new Font("굴림", Font.BOLD, 12),
            new Color(255, 220, 50)
        ));
        this.add(boardPanel);

        // 헤더 레이블 (노란색)
        String[] headers = {"1회", "2회", "3회", "계"};
        for (int i = 0; i < 4; i++) {
            JLabel lbl = new JLabel(headers[i], SwingConstants.CENTER);
            lbl.setBounds(83 + i * 70, 20, 60, 30);
            lbl.setForeground(new Color(255, 220, 50));
            lbl.setFont(new Font("Monospaced", Font.BOLD, 13));
            boardPanel.add(lbl);
        }

        // 팀명
        JLabel lblTeamA = new JLabel("원정", SwingConstants.CENTER);
        lblTeamA.setBounds(21, 50, 48, 30);
        lblTeamA.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblTeamA.setForeground(new Color(200, 200, 200));
        boardPanel.add(lblTeamA);

        JLabel lblTeamB = new JLabel("홈", SwingConstants.CENTER);
        lblTeamB.setBounds(21, 80, 48, 30);
        lblTeamB.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblTeamB.setForeground(new Color(200, 200, 200));
        boardPanel.add(lblTeamB);

        // 이닝 점수
        for (int i = 0; i < 3; i++) {
            lblScoreA[i] = new JLabel("0", SwingConstants.CENTER);
            lblScoreA[i].setBounds(83 + i * 70, 50, 60, 30);
            lblScoreA[i].setFont(new Font("Monospaced", Font.BOLD, 14));
            lblScoreA[i].setForeground(new Color(255, 220, 50));
            boardPanel.add(lblScoreA[i]);

            lblScoreB[i] = new JLabel("0", SwingConstants.CENTER);
            lblScoreB[i].setBounds(83 + i * 70, 80, 60, 30);
            lblScoreB[i].setFont(new Font("Monospaced", Font.BOLD, 14));
            lblScoreB[i].setForeground(new Color(255, 220, 50));
            boardPanel.add(lblScoreB[i]);
        }

        // 합계
        lblTotalA = new JLabel("0", SwingConstants.CENTER);
        lblTotalA.setBounds(283, 50, 60, 30);
        lblTotalA.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblTotalA.setForeground(new Color(255, 80, 80));
        boardPanel.add(lblTotalA);

        lblTotalB = new JLabel("0", SwingConstants.CENTER);
        lblTotalB.setBounds(283, 80, 60, 30);
        lblTotalB.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblTotalB.setForeground(new Color(255, 80, 80));
        boardPanel.add(lblTotalB);

        // 이닝 + 화살표
        JLabel lblInningLabel = new JLabel("이닝:");
        lblInningLabel.setBounds(420, 25, 40, 25);
        lblInningLabel.setForeground(new Color(200, 200, 200));
        boardPanel.add(lblInningLabel);

        lblInningNum = new JLabel("1");
        lblInningNum.setBounds(462, 25, 15, 25);
        lblInningNum.setForeground(new Color(255, 220, 50));
        lblInningNum.setFont(new Font("Monospaced", Font.BOLD, 14));
        boardPanel.add(lblInningNum);

        lblInningArrow = new JLabel("▲");
        lblInningArrow.setBounds(478, 25, 20, 25);
        lblInningArrow.setForeground(new Color(255, 220, 50));
        boardPanel.add(lblInningArrow);

        // SBO
        lblStrike = new JLabel("S: 0");
        lblStrike.setBounds(420, 55, 50, 25);
        lblStrike.setForeground(new Color(255, 100, 100));
        lblStrike.setFont(new Font("Monospaced", Font.BOLD, 13));
        boardPanel.add(lblStrike);

        lblBall = new JLabel("B: 0");
        lblBall.setBounds(480, 55, 50, 25);
        lblBall.setForeground(new Color(100, 200, 100));
        lblBall.setFont(new Font("Monospaced", Font.BOLD, 13));
        boardPanel.add(lblBall);

        lblOut = new JLabel("O: 0");
        lblOut.setBounds(540, 55, 50, 25);
        lblOut.setForeground(new Color(255, 180, 50));
        lblOut.setFont(new Font("Monospaced", Font.BOLD, 13));
        boardPanel.add(lblOut);

        // 베이스
        lbl2Base = new JLabel("◇");
        lbl2Base.setFont(new Font("굴림", Font.PLAIN, 25));
        lbl2Base.setBounds(630, 30, 35, 35);
        lbl2Base.setForeground(new Color(100, 100, 100));
        boardPanel.add(lbl2Base);

        lbl1Base = new JLabel("◇");
        lbl1Base.setFont(new Font("굴림", Font.PLAIN, 25));
        lbl1Base.setBounds(665, 65, 35, 35);
        lbl1Base.setForeground(new Color(100, 100, 100));
        boardPanel.add(lbl1Base);

        lbl3Base = new JLabel("◇");
        lbl3Base.setFont(new Font("굴림", Font.PLAIN, 25));
        lbl3Base.setBounds(595, 65, 35, 35);
        lbl3Base.setForeground(new Color(100, 100, 100));
        boardPanel.add(lbl3Base);

        // 홈베이스
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
                    g2.setColor(i % 2 == 0 ? new Color(80, 80, 80) : new Color(40, 40, 40));
                    g2.fillRect(0, i * getHeight() / 6, getWidth(), getHeight() / 6);
                }
                g2.setClip(null);
                g2.setColor(new Color(120, 120, 120));
                g2.drawPolygon(xPoints, yPoints, 4);
            }
        };
        homeBase.setBounds(623, 100, 35, 35);
        homeBase.setOpaque(false);
        boardPanel.add(homeBase);

        // scoreBoard 배경 맨 마지막에 추가
        JPanel scoreBoard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(20, 20, 20));
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(new Color(40, 40, 40));
                g2.fillRect(0, 0, getWidth(), 30);

                g2.setColor(new Color(30, 30, 30));
                g2.fillRect(0, 30, getWidth(), 30);

                g2.setColor(new Color(25, 25, 25));
                g2.fillRect(0, 60, getWidth(), 30);

                g2.setColor(new Color(60, 20, 20));
                g2.fillRect(271, 31, 68, 58);

                g2.setColor(new Color(80, 80, 80));
                g2.drawLine(0, 0, 340, 0);
                g2.drawLine(0, 30, 340, 30);
                g2.drawLine(0, 60, 340, 60);

                g2.drawLine(0, 0, 0, 90);
                g2.drawLine(60, 0, 60, 90);
                g2.drawLine(130, 0, 130, 90);
                g2.drawLine(200, 0, 200, 90);
                g2.drawLine(270, 0, 270, 90);
                g2.drawLine(340, 0, 340, 90);
            }
        };
        scoreBoard.setBounds(20, 20, 350, 110);
        scoreBoard.setOpaque(true);
        boardPanel.add(scoreBoard);
        boardPanel.setComponentZOrder(scoreBoard, boardPanel.getComponentCount() - 1);

        // ── 투수 컨테이너 ──
        JPanel pitcherContainer = new JPanel();
        pitcherContainer.setLayout(null);
        pitcherContainer.setBounds(0, 200, 400, 350);
        this.add(pitcherContainer);

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
            int speed = sliderSpeed.getValue();
            int typeIdx = comboPitch.getSelectedIndex();
            String typeChar = (typeIdx == 0) ? "f" : (typeIdx == 1) ? "c" : "s";
            controller.sendPitch(speed + typeChar);
            addGameLog("투구 완료! 타자의 반응을 기다립니다...");
            btnPitch.setEnabled(false);
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
        this.add(batterContainer);

        JPanel batterPanel = new JPanel();
        batterPanel.setLayout(null);
        batterPanel.setBounds(0, 0, 400, 350);
        batterPanel.setBorder(new TitledBorder("타자"));
        batterContainer.add(batterPanel);

        JLabel lblHitType = new JLabel("치는 방법:");
        lblHitType.setBounds(30, 30, 80, 25);
        batterPanel.add(lblHitType);

        comboHitType = new JComboBox<>(new String[]{"당겨치기", "밀어치기", "직선타", "기다리기"});
        comboHitType.setBounds(120, 30, 150, 25);
        batterPanel.add(comboHitType);

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

                g2.setColor(Color.DARK_GRAY);
                g2.setPaint(Color.DARK_GRAY);
                g2.drawRect(0, barY, w - 1, barH);

                g2.setColor(Color.WHITE);
                g2.setStroke(new java.awt.BasicStroke(2));
                g2.drawLine(w / 2, barY - 5, w / 2, barY + barH + 5);

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

        lblTimingResult = new JLabel("", SwingConstants.CENTER);
        lblTimingResult.setBounds(20, 175, 340, 25);
        lblTimingResult.setFont(new Font("굴림", Font.BOLD, 13));
        batterPanel.add(lblTimingResult);

        btnHit = new JButton("타격!");
        btnHit.setBounds(100, 210, 150, 40);
        btnHit.setFont(new Font("굴림", Font.BOLD, 16));
        batterPanel.add(btnHit);

        lblGuide = new JLabel(
            "<html><center>⚠️ 4번 왕복 안에 멈추지 않으면<br>자동으로 기다리기 처리됩니다.</center></html>",
            SwingConstants.CENTER
        );
        lblGuide.setBounds(20, 260, 340, 50);
        lblGuide.setForeground(new Color(180, 0, 0));
        lblGuide.setFont(new Font("굴림", Font.BOLD, 12));
        lblGuide.setBorder(BorderFactory.createLineBorder(new Color(180, 0, 0)));
        lblGuide.setOpaque(true);
        lblGuide.setBackground(new Color(255, 240, 240));
        batterPanel.add(lblGuide);

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
            if (timingLap >= 8) {
                timingTimer.stop();
                timingActive = false;
                timingDone = true;
                stoppedPos = -1;
                lblTimingResult.setText("⚾ 타이밍을 놓쳤습니다! 자동 기다리기.");
                lblTimingResult.setForeground(Color.GRAY);
                addGameLog("⏰ 4번 왕복 초과! 자동으로 기다리기 처리됩니다.");
                controller.sendTake();
                btnHit.setText("타격!");
            }
            timingBarPanel.repaint();
        });

        btnHit.addActionListener(e -> {
            if (comboHitType.getSelectedItem().equals("기다리기")) {
                addGameLog("⚾ 기다리기! 공을 지켜봅니다.");
                controller.sendTake();
                btnHit.setEnabled(false);
                return;
            }

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

            if (timingActive) {
                timingTimer.stop();
                timingActive = false;
                timingDone = true;
                stoppedPos = timingPos;

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
                addGameLog("타격: " + (String) comboHitType.getSelectedItem() + " / " + timing);

                int typeIdx = comboHitType.getSelectedIndex();
                String timingChar = (Math.abs(diff) <= 10) ? "f" : (diff < -10) ? "e" : "l";
                controller.sendSwing((typeIdx + 1) + timingChar);
                btnHit.setEnabled(false);
                timingBarPanel.repaint();
            }
        });

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
        this.add(logPanel);

        txtGameLog = new JTextArea();
        txtGameLog.setEditable(false);
        txtGameLog.setFont(new Font("굴림", Font.PLAIN, 13));
        txtGameLog.setBackground(new Color(240, 240, 240));

        JScrollPane scrollPane = new JScrollPane(txtGameLog);
        scrollPane.setBounds(10, 20, 775, 80);
        logPanel.add(scrollPane);

        setPitcherEnabled(isPitcher);
        setBatterEnabled(!isPitcher);
    }

    private void setPitcherEnabled(boolean enabled) {
        sliderSpeed.setEnabled(enabled);
        comboPitch.setEnabled(enabled);
        btnPitch.setEnabled(enabled);
    }

    private void setBatterEnabled(boolean enabled) {
        comboHitType.setEnabled(enabled);
        btnHit.setEnabled(enabled);
        if (!enabled && timingTimer != null) {
            timingTimer.stop();
            timingActive = false;
        }
    }

    public void switchRole() {
        isPitcher = !isPitcher;
        pitcherOverlay.setVisible(!isPitcher);
        batterOverlay.setVisible(isPitcher);
        setPitcherEnabled(isPitcher);
        setBatterEnabled(!isPitcher);
        resetTimingBar();
    }

    private void resetTimingBar() {
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

    public void updateScreen(GameMessage msg) {
        MessageType type = msg.getType();
        switch (type) {

            case ROLE_PITCHER:
                isPitcher = true;
                pitcherOverlay.setVisible(false);
                batterOverlay.setVisible(true);
                setPitcherEnabled(true);
                setBatterEnabled(false);
                addGameLog("⚾ 당신은 투수입니다! 먼저 접속하셨습니다.");
                break;

            case ROLE_BATTER:
                isPitcher = false;
                pitcherOverlay.setVisible(true);
                batterOverlay.setVisible(false);
                setPitcherEnabled(false);
                setBatterEnabled(true);
                addGameLog("🏏 당신은 타자입니다! 나중에 접속하셨습니다.");
                break;

            case MATCH_COMPLETE:
                addGameLog("✅ 매칭 완료! 게임을 시작합니다.");
                break;

            case ACTION_PITCH:
                if (!isPitcher) {
                    addGameLog("⚾ 공이 날아옵니다! 타이밍을 맞춰 타격하세요!");
                    setBatterEnabled(true);
                    btnHit.setEnabled(true);
                    btnHit.setText("타격!");
                    resetTimingBar();
                } else {
                    addGameLog("투구 완료! 타자의 반응을 기다립니다...");
                }
                break;

            case STATE_UPDATE:
                GameState state = (GameState) msg.getData();
                if (state != null) {
                    updateInning(state.getInning(), state.isTop());
                    updateSBO(state.getStrikeCount(), state.getBallCount(), state.getOutCount());
                    boolean[] bases = state.getBases();
                    updateBase(bases[0], bases[1], bases[2]);
                    int[] awayByInning = state.getAwayScoreByInning();
                    int[] homeByInning = state.getHomeScoreByInning();
                    for (int i = 0; i < 3; i++) {
                        updateScore(i, awayByInning[i], homeByInning[i]);
                    }
                    if (state.getLastMessage() != null) {
                        addGameLog(state.getLastMessage());
                    }
                }
                break;

            case RESULT_BALL:
                addGameLog("⚪ 볼!");
                btnPitch.setEnabled(true);
                btnHit.setEnabled(true);
                btnHit.setText("타격!");
                resetTimingBar();
                break;

            case RESULT_STRIKE:
                addGameLog("🔴 스트라이크!");
                btnPitch.setEnabled(true);
                btnHit.setEnabled(true);
                btnHit.setText("타격!");
                resetTimingBar();
                break;

            case RESULT_HIT:
                addGameLog("💥 안타!");
                btnPitch.setEnabled(true);
                btnHit.setEnabled(true);
                btnHit.setText("타격!");
                resetTimingBar();
                break;

            case RESULT_OUT:
                addGameLog("❌ 아웃!");
                btnPitch.setEnabled(true);
                btnHit.setEnabled(true);
                btnHit.setText("타격!");
                resetTimingBar();
                break;

            case SWAP_TURN:
                addGameLog("🔄 공수 교대!");
                switchRole();
                break;

            case INNING_OVER:
                addGameLog("🏁 이닝 종료!");
                switchRole();
                break;

            case GAME_OVER:
                String result = (String) msg.getData();
                addGameLog("🏆 게임 종료! " + (result != null ? result : ""));
                btnPitch.setEnabled(false);
                btnHit.setEnabled(false);
                break;

            default:
                addGameLog("[알 수 없는 메시지]: " + type);
                break;
        }
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
        lbl1Base.setForeground(first ? new Color(255, 200, 0) : new Color(100, 100, 100));
        lbl2Base.setForeground(second ? new Color(255, 200, 0) : new Color(100, 100, 100));
        lbl3Base.setForeground(third ? new Color(255, 200, 0) : new Color(100, 100, 100));
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