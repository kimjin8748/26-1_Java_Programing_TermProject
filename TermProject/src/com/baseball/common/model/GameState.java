package com.baseball.common.model;
import java.io.Serializable;

/**
 * 게임의 현재 상태를 담는 객체.
 * 서버가 상태를 갱신한 후 클라이언트에게 브로드캐스트합니다.
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    // 1. 경기 흐름 (Inning & Score)
    private int inning;         // 현재 이닝 (예: 1 ~ 3)
    private boolean isTop;      // true: 초(선공/원정 공격), false: 말(후공/홈 공격)
    private int awayScore;      // 원정팀 점수
    private int homeScore;      // 홈팀 점수

    // 2. 볼 카운트 (B-S-O)
    private int ballCount;      // 0 ~ 3
    private int strikeCount;    // 0 ~ 2
    private int outCount;       // 0 ~ 2

    // 3. 주자 상황 (Bases)
    // 인덱스 0: 1루, 인덱스 1: 2루, 인덱스 2: 3루 (true면 주자 있음)
    private boolean[] bases;

    // 4. UI(화면)에 띄울 시스템 메시지
    private String lastMessage; // 예: "145km/h 직구! 헛스윙 스트라이크!"

    // ⚾ 생성자: 게임이 처음 시작될 때의 초기값 세팅
    public GameState() {
        this.inning = 1;
        this.isTop = true;      // 1회 초 시작
        this.awayScore = 0;
        this.homeScore = 0;
        this.ballCount = 0;
        this.strikeCount = 0;
        this.outCount = 0;
        this.bases = new boolean[3]; // 배열 생성 시 기본값은 모두 false(주자 없음)
        this.lastMessage = "플레이 볼! 경기를 시작합니다.";
    }

    // ==========================================
    // 상태 변경 메서드 (서버의 Umpire/BaseManager가 호출함)
    // ==========================================

    // 타석이 끝났을 때(안타, 볼넷, 아웃 등) 카운트 초기화
    public void resetBattingCount() {
        this.ballCount = 0;
        this.strikeCount = 0;
    }

    // 공수 교대 시 또는 이닝 종료 시 호출
    public void resetInning() {
        resetBattingCount();
        this.outCount = 0;
        this.bases = new boolean[3]; // 주자 올 리셋
    }

    // ==========================================
    // Getters & Setters (캡슐화를 위해 필수)
    // ==========================================
    
    public int getInning() { return inning; }
    public void setInning(int inning) { this.inning = inning; }

    public boolean isTop() { return isTop; }
    public void setTop(boolean top) { isTop = top; }

    public int getAwayScore() { return awayScore; }
    public void addAwayScore(int score) { this.awayScore += score; }

    public int getHomeScore() { return homeScore; }
    public void addHomeScore(int score) { this.homeScore += score; }

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