package com.baseball.client.gui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class GamePanel {

    private JFrame frame;

    // 투수
    private JSlider sliderSpeed;
    private JComboBox<String> comboPitch;
    private JButton btnPitch;
    private JLabel lblSpeedValue;
    private JPanel pitcherOverlay; // 투수 오버레이

    // 타자
    private JComboBox<String> comboHitType;
    private JComboBox<String> comboTiming;
    private JButton btnHit;
    private JPanel batterOverlay; // 타자 오버레이

    // 전광판
    private JLabel[] lblScoreA = new JLabel[3];
    private JLabel[] lblScoreB = new JLabel[3];
    private JLabel lblTotalA, lblTotalB;
    private JLabel lbl1Base, lbl2Base, lbl3Base;
    private JLabel lblStrike, lblBall, lblOut;
    private JLabel lblInningNum, lblInningArrow;

    // 대화창
    private JTextArea txtGameLog;

    // 역할 (true = 투수, false = 타자)
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

        // ── 투수 컨테이너 (왼쪽) ──
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
            // TODO: 서버로 투구 정보 전송
        });

        // 투수 오버레이 - 타자일 때 투수 화면 어둡게
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
        pitcherOverlay.setVisible(!isPitcher); // 타자일 때 투수 화면 어둡게
        pitcherContainer.add(pitcherOverlay);
        pitcherContainer.setComponentZOrder(pitcherOverlay, 0);

        // ── 타자 컨테이너 (오른쪽) ──
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
        lblHitType.setBounds(30, 40, 80, 25);
        batterPanel.add(lblHitType);

        comboHitType = new JComboBox<>(new String[]{"당겨치기", "밀어치기", "직선타"});
        comboHitType.setBounds(120, 40, 150, 25);
        batterPanel.add(comboHitType);

        JLabel lblTiming = new JLabel("타이밍:");
        lblTiming.setBounds(30, 100, 80, 25);
        batterPanel.add(lblTiming);

        comboTiming = new JComboBox<>(new String[]{"빠르게", "보통", "늦게"});
        comboTiming.setBounds(120, 100, 150, 25);
        batterPanel.add(comboTiming);

        btnHit = new JButton("타격!");
        btnHit.setBounds(100, 220, 150, 40);
        btnHit.setFont(new Font("굴림", Font.BOLD, 16));
        batterPanel.add(btnHit);

        btnHit.addActionListener(e -> {
            String hitType = (String) comboHitType.getSelectedItem();
            String timing = (String) comboTiming.getSelectedItem();
            addGameLog("타격: " + hitType + " / " + timing);
            // TODO: 서버로 타격 정보 전송
        });

        // 타자 오버레이 - 투수일 때 타자 화면 어둡게
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
        batterOverlay.setVisible(isPitcher); // 투수일 때 타자 화면 어둡게
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
    }

    // 게임 로그 추가
    public void addGameLog(String message) {
        txtGameLog.append(message + "\n");
        txtGameLog.setCaretPosition(txtGameLog.getDocument().getLength());
    }

    // 역할 전환 (이닝 끝날 때 호출)
    public void switchRole() {
        isPitcher = !isPitcher;
        pitcherOverlay.setVisible(!isPitcher); // 타자일 때 투수 어둡게
        batterOverlay.setVisible(isPitcher);   // 투수일 때 타자 어둡게
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