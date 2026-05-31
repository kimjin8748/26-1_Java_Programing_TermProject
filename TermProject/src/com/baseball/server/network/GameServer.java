package com.baseball.server.network;

// ⭐ [추가됨] 파일 저장과 시간 기록을 위한 필수 클래스 임포트
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.SwingUtilities;

import com.baseball.common.model.GameState;
import com.baseball.common.model.PitchData;
import com.baseball.common.model.SwingData;
import com.baseball.common.protocol.GameMessage;
import com.baseball.common.protocol.MessageType;
import com.baseball.server.core.BaseManager;
import com.baseball.server.core.Umpire;
import com.baseball.server.data.RecordManager;

public class GameServer {
    private static final int PORT = 8080;
    private List<ClientHandler> clients = new ArrayList<>();
    private List<String> playerNames = new ArrayList<>();

    private GameState gameState = new GameState();
    private Umpire umpire = new Umpire();
    private BaseManager baseManager = new BaseManager();
    private PitchData pendingPitch;
    private int playerCount = 0;

    // AI 모드
    private boolean aiMode = false;
    private boolean aiIsPitcher = false;
    private Random rand = new Random();

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("야구 게임 서버가 포트 " + PORT + "에서 시작되었습니다.");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void broadcast(GameMessage msg) {
        for (ClientHandler client : clients) {
            client.sendMessage(msg);
        }
    }

    public synchronized int incrementAndGetPlayerCount() {
        return ++playerCount;
    }

    public synchronized void addPlayerName(String name) {
        playerNames.add(name);
    }

    public String[] getPlayerNames() {
        return playerNames.toArray(new String[0]);
    }

    // 클라이언트 연결 끊김 처리
    public synchronized void removeClient(ClientHandler handler) {
        boolean wasP = handler.isPitcher();
        clients.remove(handler);
        System.out.println("[서버] 클라이언트 연결 끊김. 남은 클라이언트: " + clients.size());

        if (clients.size() == 1 && !aiMode) {
            aiIsPitcher = wasP;
            System.out.println("[서버] AI 모드 시작! AI 역할: " + (aiIsPitcher ? "투수" : "타자"));
            broadcast(new GameMessage(MessageType.STATE_UPDATE, gameState));

            SwingUtilities.invokeLater(() -> startAiMode());
        }
    }

    // AI 모드 시작
    public void startAiMode() {
        if (aiMode) return;
        aiMode = true;

        if (aiIsPitcher) {
            // AI가 투수면 3초 후 투구
            scheduleAiPitch(3000);
        }
        // AI가 타자면 실제 투수가 던질 때 ClientHandler에서 처리
    }

    // AI 투구 예약
    private void scheduleAiPitch(int delay) {
        javax.swing.Timer t = new javax.swing.Timer(delay, e -> performAiPitch());
        t.setRepeats(false);
        t.start();
    }

    // AI 타격 예약
    private void scheduleAiSwing(int delay) {
        javax.swing.Timer t = new javax.swing.Timer(delay, e -> performAiSwing());
        t.setRepeats(false);
        t.start();
    }

    // AI 투구
    public synchronized void performAiPitch() {
        if (!aiMode || clients.isEmpty()) return;

        int speed = 80 + rand.nextInt(60);
        char[] types = {'f', 'c', 's'};
        char type = types[rand.nextInt(3)];
        PitchData aiPitch = new PitchData(speed + "" + type);
        setPendingPitch(aiPitch);
        gameState.setLastMessage("[AI] 투수가 " + speed + "km/h 공을 던졌습니다!");
        broadcast(new GameMessage(MessageType.STATE_UPDATE, gameState));
        broadcast(new GameMessage(MessageType.ACTION_PITCH));
        System.out.println("[서버] AI 투구: " + speed + "km/h " + type);
    }

    // AI 타격
    public synchronized void performAiSwing() {
        if (!aiMode || clients.isEmpty()) return;

        PitchData pitch = getPendingPitch();
        if (pitch == null) {
            System.out.println("[서버] AI 타격 실패: 투구 없음");
            return;
        }

        int typeIdx = rand.nextInt(3) + 1;
        char[] timings = {'f', 'e', 'l'};
        char timing = timings[rand.nextInt(3)];
        SwingData aiSwing = new SwingData(typeIdx + "" + timing);

        setPendingPitch(null);
        GameMessage swingResult = umpire.judgeSwing(pitch, aiSwing);
        processUmpireResult(swingResult);
        System.out.println("[서버] AI 타격: " + typeIdx + timing);
    }

    // 심판 결과 처리
    public synchronized void processUmpireResult(GameMessage resultMsg) {
        GameState state = gameState;
        MessageType resultType = resultMsg.getType();

        if (resultType == MessageType.RESULT_HIT) {
            int advanceBases = (Integer) resultMsg.getData();
            baseManager.processHit(state, advanceBases);

        } else if (resultType == MessageType.RESULT_OUT) {
            state.addOut();
            state.setLastMessage((String) resultMsg.getData());
            state.resetBattingCount();

        } else if (resultType == MessageType.RESULT_STRIKE) {
            state.addStrike();
            state.setLastMessage((String) resultMsg.getData());
            if (state.getStrikeCount() >= 3) {
                state.addOut();
                state.setLastMessage("삼진 아웃!");
                state.resetBattingCount();
            }

        } else if (resultType == MessageType.RESULT_BALL) {
            state.addBall();
            state.setLastMessage((String) resultMsg.getData());
            if (state.getBallCount() >= 4) {
                baseManager.processHit(state, 1);
                state.setLastMessage("볼넷! 타자가 1루로 걸어 나갑니다.");
            }
        }

        broadcast(resultMsg);

        if (state.getOutCount() >= 3) {
            state.resetInning();
            boolean wasTop = state.isTop();
            state.setTop(!wasTop);

            if (!state.isTop()) {
                state.setLastMessage("3아웃! 공수 교대! " + state.getInning() + "회 말 시작!");
            } else {
                state.setInning(state.getInning() + 1);

                if (state.getInning() > 3) {
                    String winner;
                    if (state.getAwayScore() > state.getHomeScore()) {
                        winner = "원정팀 승리!";
                    } else if (state.getHomeScore() > state.getAwayScore()) {
                        winner = "홈팀 승리!";
                    } else {
                        winner = "무승부!";
                    }
                    String result = "게임 종료! 원정: " + state.getAwayScore()
                                  + " / 홈: " + state.getHomeScore()
                                  + " → " + winner;
                    broadcast(new GameMessage(MessageType.STATE_UPDATE, state));
                    
                    // 클라이언트에게 종료 메시지를 쏘기 직전에 전적 파일 저장
                    saveRecordsForAll(state);

                    broadcast(new GameMessage(MessageType.GAME_OVER, result));
                    aiMode = false;
                    return;
                }

                state.setLastMessage("3아웃! 공수 교대! " + state.getInning() + "회 초 시작!");
                broadcast(new GameMessage(MessageType.INNING_OVER));
            }

            // 공수교대 후 AI 역할도 바뀜
            aiIsPitcher = !aiIsPitcher;
            System.out.println("[서버] 공수교대 후 AI 역할: " + (aiIsPitcher ? "투수" : "타자"));
            broadcast(new GameMessage(MessageType.SWAP_TURN));

            // 공수교대 후 AI가 투수면 자동 투구
            if (aiIsPitcher) {
                SwingUtilities.invokeLater(() -> scheduleAiPitch(2000));
            }
        }

        broadcast(new GameMessage(MessageType.STATE_UPDATE, state));

        // 판정 후 AI가 투수면 다음 투구
        if (aiMode && aiIsPitcher && state.getOutCount() < 3) {
            SwingUtilities.invokeLater(() -> scheduleAiPitch(2000));
        }
    }

    // ==========================================
    //  플레이어 전적을 txt 파일로 누적 저장하는 메서드
    // ==========================================
    public synchronized void saveRecordsForAll(GameState state) {
        System.out.println("[서버] RecordManager 클래스에 파일 저장을 요청합니다.");
        
        for (String username : playerNames) {
            if (username == null || username.trim().isEmpty()) continue;
            
            RecordManager.savePersonalRecord(username, state);
        }
    }

    public int getClientCount() { return clients.size(); }
    public int getPlayerCount() { return playerCount; }
    public GameState getGameState() { return gameState; }
    public Umpire getUmpire() { return umpire; }
    public BaseManager getBaseManager() { return baseManager; }
    public PitchData getPendingPitch() { return pendingPitch; }
    public void setPendingPitch(PitchData pitch) { this.pendingPitch = pitch; }
    public boolean isAiMode() { return aiMode; }
    public boolean isAiPitcher() { return aiIsPitcher; }

    public static void main(String[] args) {
        new GameServer().startServer();
    }
}