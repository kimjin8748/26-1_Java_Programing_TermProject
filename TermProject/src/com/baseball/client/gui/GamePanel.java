package com.baseball.client.gui;

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
    private JPanel pitcherContainer;
    private JLabel lblPitcherName;

    // 타자
    private JComboBox<String> comboHitType;
    private JButton btnHit;
    private JPanel batterOverlay;
    private JLabel lblGuide;
    private JPanel batterContainer;
    private JLabel lblBatterName;

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
    private JLabel lblTeamNameA, lblTeamNameB;

    // 대화창
    private JTextArea txtGameLog;

    // 역할
    private boolean isPitcher = true;

    // centerPanel 참조
    private JPanel centerPanel;

    // 홈런 효과 변수
    private List<int[]> particles = new ArrayList<>();
    private List<Color> particleColors = new ArrayList<>();
    private List<double[]> particleVelocities = new ArrayList<>();
    private int effectFrame = 0;
    private boolean showEffect = false;
    private Timer effectTimer;
    private JPanel effectPanel;

    public GamePanel(GameController controller) {
        this.controller = controller;
        initialize();
    }

    private void showHomerunEffect() {
        particles.clear();
        particleColors.clear();
        particleVelocities.clear();
        effectFrame = 0;
        showEffect = true;

        Random rand = new Random();
        Color[] colors = {
            Color.RED, Color.ORANGE, Color.YELLOW,
            Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA,
            Color.WHITE, Color.PINK, new Color(255, 150, 0)
        };

        int cx = 400;
        int cy = 340;

        for (int i = 0; i < 200; i++) {
            particles.add(new int[]{cx, cy});
            particleColors.add(colors[rand.nextInt(colors.length)]);
            double angle = Math.random() * Math.PI * 2;
            double speed = Math.random() * 10 + 3;
            particleVelocities.add(new double[]{
                Math.cos(angle) * speed,
                Math.sin(angle) * speed - 3
            });
        }

        effectPanel.setVisible(true);

        if (effectTimer != null) effectTimer.stop();
        effectTimer = new Timer(16, e -> {
            effectFrame++;
            effectPanel.repaint();
            if (effectFrame >= 100) {
                effectTimer.stop();
                showEffect = false;
                effectPanel.setVisible(false);
            }
        });
        effectTimer.start();
    }

    private void initialize() {
        this.setLayout(new GridBagLayout());

        centerPanel = new JPanel();
        centerPanel.setLayout(null);
        centerPanel.setPreferredSize(new java.awt.Dimension(800, 680));

        // ── 상단 전광판 ──
        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(null);
        boardPanel.setBounds(0, 0, 800, 160);
        boardPanel.setBackground(new Color(20, 20, 20));
        boardPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)),
            "전광판",
            0, 0,
            new Font("굴림", Font.BOLD, 12),
            new Color(255, 220, 50)
        ));
        centerPanel.add(boardPanel);

        String[] headers = {"1회", "2회", "3회", "계"};
        for (int i = 0; i < 4; i++) {
            JLabel lbl = new JLabel(headers[i], SwingConstants.CENTER);
            lbl.setBounds(83 + i * 70, 15, 60, 25);
            lbl.setForeground(new Color(255, 220, 50));
            lbl.setFont(new Font("Monospaced", Font.BOLD, 13));
            boardPanel.add(lbl);
        }

        lblTeamNameA = new JLabel("원정", SwingConstants.CENTER);
        lblTeamNameA.setBounds(21, 42, 48, 25);
        lblTeamNameA.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblTeamNameA.setForeground(new Color(200, 200, 200));
        boardPanel.add(lblTeamNameA);

        lblTeamNameB = new JLabel("홈", SwingConstants.CENTER);
        lblTeamNameB.setBounds(21, 70, 48, 25);
        lblTeamNameB.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblTeamNameB.setForeground(new Color(200, 200, 200));
        boardPanel.add(lblTeamNameB);

        for (int i = 0; i < 3; i++) {
            lblScoreA[i] = new JLabel("0", SwingConstants.CENTER);
            lblScoreA[i].setBounds(83 + i * 70, 42, 60, 25);
            lblScoreA[i].setFont(new Font("Monospaced", Font.BOLD, 14));
            lblScoreA[i].setForeground(new Color(255, 220, 50));
            boardPanel.add(lblScoreA[i]);

            lblScoreB[i] = new JLabel("0", SwingConstants.CENTER);
            lblScoreB[i].setBounds(83 + i * 70, 70, 60, 25);
            lblScoreB[i].setFont(new Font("Monospaced", Font.BOLD, 14));
            lblScoreB[i].setForeground(new Color(255, 220, 50));
            boardPanel.add(lblScoreB[i]);
        }

        lblTotalA = new JLabel("0", SwingConstants.CENTER);
        lblTotalA.setBounds(283, 42, 60, 25);
        lblTotalA.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblTotalA.setForeground(new Color(255, 80, 80));
        boardPanel.add(lblTotalA);

        lblTotalB = new JLabel("0", SwingConstants.CENTER);
        lblTotalB.setBounds(283, 70, 60, 25);
        lblTotalB.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblTotalB.setForeground(new Color(255, 80, 80));
        boardPanel.add(lblTotalB);

        JLabel lblInningLabel = new JLabel("이닝:");
        lblInningLabel.setBounds(420, 20, 40, 25);
        lblInningLabel.setForeground(new Color(200, 200, 200));
        boardPanel.add(lblInningLabel);

        lblInningNum = new JLabel("1");
        lblInningNum.setBounds(462, 20, 15, 25);
        lblInningNum.setForeground(new Color(255, 220, 50));
        lblInningNum.setFont(new Font("Monospaced", Font.BOLD, 14));
        boardPanel.add(lblInningNum);

        lblInningArrow = new JLabel("▲");
        lblInningArrow.setBounds(478, 20, 20, 25);
        lblInningArrow.setForeground(new Color(255, 220, 50));
        boardPanel.add(lblInningArrow);

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

        lbl2Base = new JLabel("◇");
        lbl2Base.setFont(new Font("굴림", Font.PLAIN, 25));
        lbl2Base.setBounds(630, 20, 35, 35);
        lbl2Base.setForeground(new Color(100, 100, 100));
        boardPanel.add(lbl2Base);

        lbl1Base = new JLabel("◇");
        lbl1Base.setFont(new Font("굴림", Font.PLAIN, 25));
        lbl1Base.setBounds(665, 55, 35, 35);
        lbl1Base.setForeground(new Color(100, 100, 100));
        boardPanel.add(lbl1Base);

        lbl3Base = new JLabel("◇");
        lbl3Base.setFont(new Font("굴림", Font.PLAIN, 25));
        lbl3Base.setBounds(595, 55, 35, 35);
        lbl3Base.setForeground(new Color(100, 100, 100));
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
                    g2.setColor(i % 2 == 0 ? new Color(80, 80, 80) : new Color(40, 40, 40));
                    g2.fillRect(0, i * getHeight() / 6, getWidth(), getHeight() / 6);
                }
                g2.setClip(null);
                g2.setColor(new Color(120, 120, 120));
                g2.drawPolygon(xPoints, yPoints, 4);
            }
        };
        homeBase.setBounds(623, 90, 35, 35);
        homeBase.setOpaque(false);
        boardPanel.add(homeBase);

        JPanel scoreBoard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 20, 20));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(40, 40, 40));
                g2.fillRect(0, 0, getWidth(), 25);
                g2.setColor(new Color(30, 30, 30));
                g2.fillRect(0, 25, getWidth(), 25);
                g2.setColor(new Color(25, 25, 25));
                g2.fillRect(0, 50, getWidth(), 25);
                g2.setColor(new Color(60, 20, 20));
                g2.fillRect(271, 26, 68, 48);
                g2.setColor(new Color(80, 80, 80));
                g2.drawLine(0, 0, 340, 0);
                g2.drawLine(0, 25, 340, 25);
                g2.drawLine(0, 50, 340, 50);
                g2.drawLine(0, 75, 340, 75);
                g2.drawLine(0, 0, 0, 75);
                g2.drawLine(60, 0, 60, 75);
                g2.drawLine(130, 0, 130, 75);
                g2.drawLine(200, 0, 200, 75);
                g2.drawLine(270, 0, 270, 75);
                g2.drawLine(340, 0, 340, 75);
            }
        };
        scoreBoard.setBounds(20, 15, 350, 75);
        scoreBoard.setOpaque(true);
        boardPanel.add(scoreBoard);
        boardPanel.setComponentZOrder(scoreBoard, boardPanel.getComponentCount() - 1);

        // ── 이름 레이블 ──
        lblPitcherName = new JLabel("< 투수 >", SwingConstants.CENTER);
        lblPitcherName.setBounds(0, 160, 400, 25);
        lblPitcherName.setFont(new Font("굴림", Font.BOLD, 14));
        lblPitcherName.setForeground(new Color(255, 220, 50));
        lblPitcherName.setBackground(new Color(20, 20, 20));
        lblPitcherName.setOpaque(true);
        centerPanel.add(lblPitcherName);

        lblBatterName = new JLabel("< 타자 >", SwingConstants.CENTER);
        lblBatterName.setBounds(400, 160, 400, 25);
        lblBatterName.setFont(new Font("굴림", Font.BOLD, 14));
        lblBatterName.setForeground(new Color(255, 220, 50));
        lblBatterName.setBackground(new Color(20, 20, 20));
        lblBatterName.setOpaque(true);
        centerPanel.add(lblBatterName);

        // ── 투수 컨테이너 ──
        pitcherContainer = new JPanel();
        pitcherContainer.setLayout(null);
        pitcherContainer.setBounds(0, 185, 400, 360);
        centerPanel.add(pitcherContainer);

        JPanel pitcherPanel = new JPanel();
        pitcherPanel.setLayout(null);
        pitcherPanel.setBounds(0, 0, 400, 360);
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
        btnPitch.setBounds(100, 240, 150, 40);
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
        pitcherOverlay.setBounds(0, 0, 400, 360);
        pitcherOverlay.setOpaque(false);
        pitcherOverlay.setVisible(!isPitcher);
        pitcherContainer.add(pitcherOverlay);
        pitcherContainer.setComponentZOrder(pitcherOverlay, 0);

        // ── 타자 컨테이너 ──
        batterContainer = new JPanel();
        batterContainer.setLayout(null);
        batterContainer.setBounds(400, 185, 400, 360);
        centerPanel.add(batterContainer);

        JPanel batterPanel = new JPanel();
        batterPanel.setLayout(null);
        batterPanel.setBounds(0, 0, 400, 360);
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
        btnHit.setBounds(100, 220, 150, 40);
        btnHit.setFont(new Font("굴림", Font.BOLD, 16));
        batterPanel.add(btnHit);

        lblGuide = new JLabel(
            "<html><center>⚠️ 2번 왕복 안에 멈추지 않으면<br>자동으로 기다리기 처리됩니다.</center></html>",
            SwingConstants.CENTER
        );
        lblGuide.setBounds(20, 270, 340, 50);
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
            if (timingLap >= 4) {
                timingTimer.stop();
                timingActive = false;
                timingDone = true;
                stoppedPos = -1;
                lblTimingResult.setText("⚾ 타이밍을 놓쳤습니다! 자동 기다리기.");
                lblTimingResult.setForeground(Color.GRAY);
                addGameLog("⏰ 2번 왕복 초과! 자동으로 기다리기 처리됩니다.");
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
        batterOverlay.setBounds(0, 0, 400, 360);
        batterOverlay.setOpaque(false);
        batterOverlay.setVisible(isPitcher);
        batterContainer.add(batterOverlay);
        batterContainer.setComponentZOrder(batterOverlay, 0);

        // ── 하단 대화창 ──
        JPanel logPanel = new JPanel();
        logPanel.setLayout(null);
        logPanel.setBounds(0, 545, 800, 120);
        logPanel.setBorder(BorderFactory.createTitledBorder("게임 진행"));
        centerPanel.add(logPanel);

        txtGameLog = new JTextArea();
        txtGameLog.setEditable(false);
        txtGameLog.setFont(new Font("굴림", Font.PLAIN, 13));
        txtGameLog.setBackground(new Color(240, 240, 240));

        JScrollPane scrollPane = new JScrollPane(txtGameLog);
        scrollPane.setBounds(10, 20, 775, 85);
        logPanel.add(scrollPane);

        // ── 홈런 효과 패널 ──
        effectPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (!showEffect) return;
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

                float textAlpha = Math.min(1.0f, effectFrame < 20 ? effectFrame / 20.0f :
                                  effectFrame > 80 ? (100 - effectFrame) / 20.0f : 1.0f);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textAlpha));

                g2.setFont(new Font("굴림", Font.BOLD, 80));
                String text = "홈런!";
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(text)) / 2;
                g2.setColor(new Color(255, 100, 0));
                g2.drawString(text, tx + 3, 213);
                g2.setColor(new Color(255, 220, 0));
                g2.drawString(text, tx, 210);

                g2.setFont(new Font("굴림", Font.BOLD, 30));
                String sub = "⚾ HOMERUN !! ⚾";
                fm = g2.getFontMetrics();
                tx = (getWidth() - fm.stringWidth(sub)) / 2;
                g2.setColor(Color.WHITE);
                g2.drawString(sub, tx, 260);

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

                for (int i = 0; i < particles.size(); i++) {
                    int[] pos = particles.get(i);
                    double[] vel = particleVelocities.get(i);
                    pos[0] += (int) vel[0];
                    pos[1] += (int) vel[1];
                    vel[1] += 0.25;

                    float alpha = Math.max(0, 1.0f - effectFrame / 100.0f);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    g2.setColor(particleColors.get(i));
                    int size = (int)(Math.random() * 5 + 3);
                    g2.fillOval(pos[0], pos[1], size, size);
                    if (i % 3 == 0) {
                        g2.fillRect(pos[0] - 1, pos[1] - 4, 3, 9);
                        g2.fillRect(pos[0] - 4, pos[1] - 1, 9, 3);
                    }
                }
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        };
        effectPanel.setBounds(0, 0, 800, 680);
        effectPanel.setOpaque(false);
        effectPanel.setVisible(false);
        centerPanel.add(effectPanel);
        centerPanel.setComponentZOrder(effectPanel, 0);

        this.add(centerPanel, new GridBagConstraints());

        // 초기 상태 - 타자 버튼 비활성화 (투구 받기 전까지)
        setPitcherEnabled(isPitcher);
        setBatterEnabled(false); // ← 처음엔 무조건 false
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
        setBatterEnabled(false); // ← 공수교대 후 타자 버튼 비활성화! 투구 받아야 활성화
        resetTimingBar();

        // 이름 레이블 교체
        String pitcherText = lblPitcherName.getText();
        String batterText = lblBatterName.getText();
        lblPitcherName.setText(batterText);
        lblBatterName.setText(pitcherText);
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
                setBatterEnabled(false); // 투구 받기 전까지 비활성화
                addGameLog("🏏 당신은 타자입니다! 나중에 접속하셨습니다.");
                break;

            case MATCH_COMPLETE:
                addGameLog("✅ 매칭 완료! 게임을 시작합니다.");
                if (msg.getData() != null) {
                    String[] names = (String[]) msg.getData();
                    String homeName = names[0].contains("_") ? names[0].split("_")[1] : names[0];
                    String awayName = names[1].contains("_") ? names[1].split("_")[1] : names[1];
                    lblTeamNameA.setText(awayName);
                    lblTeamNameB.setText(homeName);
                    lblPitcherName.setText("< " + homeName + " >");
                    lblBatterName.setText("< " + awayName + " >");
                }
                break;

            case ACTION_PITCH:
                if (!isPitcher) {
                    // 투구 받았을 때만 타자 활성화!
                    addGameLog("⚾ 공이 날아옵니다! 타이밍을 맞춰 타격하세요!");
                    setBatterEnabled(true);
                    btnHit.setEnabled(true);
                    btnHit.setText("타격!");
                    resetTimingBar();
                } else {
                    addGameLog("투구 완료! 타자의 반응을 기다립니다...");
                    btnPitch.setEnabled(false);
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
                        if (state.getLastMessage().contains("홈런")) {
                            showHomerunEffect();
                        }
                    }
                }
                break;

            case RESULT_BALL:
                addGameLog("⚪ 볼!");
                btnPitch.setEnabled(isPitcher);
                setBatterEnabled(false); // 다음 투구 전까지 비활성화
                btnHit.setText("타격!");
                resetTimingBar();
                break;

            case RESULT_STRIKE:
                addGameLog("🔴 스트라이크!");
                btnPitch.setEnabled(isPitcher);
                setBatterEnabled(false);
                btnHit.setText("타격!");
                resetTimingBar();
                break;

            case RESULT_HIT:
                addGameLog("💥 안타!");
                btnPitch.setEnabled(isPitcher);
                setBatterEnabled(false);
                btnHit.setText("타격!");
                resetTimingBar();
                break;

            case RESULT_OUT:
                addGameLog("❌ 아웃!");
                btnPitch.setEnabled(isPitcher);
                setBatterEnabled(false);
                btnHit.setText("타격!");
                resetTimingBar();
                break;

            case SWAP_TURN:
                addGameLog("🔄 공수 교대!");
                switchRole();
                break;

            case INNING_OVER:
                addGameLog("🏁 이닝 종료!");
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

	public void setPlayerNames(String myName, String opponentName) {
		// TODO Auto-generated method stub
		
	}
}