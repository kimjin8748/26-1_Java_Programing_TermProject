package com.baseball.common.model;

import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private int inning;
    private boolean isTop;
    private int awayScore;
    private int homeScore;

    private int[] awayScoreByInning = new int[3];
    private int[] homeScoreByInning = new int[3];

    private int ballCount;
    private int strikeCount;
    private int outCount;

    private boolean[] bases;
    private String lastMessage;

    public GameState() {
        this.inning = 1;
        this.isTop = true;
        this.awayScore = 0;
        this.homeScore = 0;
        this.awayScoreByInning = new int[3];
        this.homeScoreByInning = new int[3];
        this.ballCount = 0;
        this.strikeCount = 0;
        this.outCount = 0;
        this.bases = new boolean[3];
        this.lastMessage = "플레이 볼! 경기를 시작합니다.";
    }

    public void resetBattingCount() {
        this.ballCount = 0;
        this.strikeCount = 0;
    }

    public void resetInning() {
        resetBattingCount();
        this.outCount = 0;
        this.bases = new boolean[3];
    }

    // Getters & Setters
    public int getInning() { return inning; }
    public void setInning(int inning) { this.inning = inning; }

    public boolean isTop() { return isTop; }
    public void setTop(boolean top) { isTop = top; }

    public int getAwayScore() { return awayScore; }
    public void addAwayScore(int score) {
        this.awayScore += score;
        this.awayScoreByInning[this.inning - 1] += score;
    }

    public int getHomeScore() { return homeScore; }
    public void addHomeScore(int score) {
        this.homeScore += score;
        this.homeScoreByInning[this.inning - 1] += score;
    }

    public int[] getAwayScoreByInning() { return awayScoreByInning; }
    public int[] getHomeScoreByInning() { return homeScoreByInning; }

    public int getBallCount() { return ballCount; }
    public void addBall() { this.ballCount++; }

    public int getStrikeCount() { return strikeCount; }
    public void addStrike() { this.strikeCount++; }

    public int getOutCount() { return outCount; }
    public void addOut() { this.outCount++; }

    public boolean[] getBases() { return bases; }
    public void setBases(boolean[] bases) { this.bases = bases; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
}