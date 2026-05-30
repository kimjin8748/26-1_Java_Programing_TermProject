package com.baseball.server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.baseball.common.model.GameState;
import com.baseball.common.model.PitchData;
import com.baseball.common.model.SwingData;
import com.baseball.common.protocol.GameMessage;
import com.baseball.common.protocol.MessageType;
import com.baseball.server.core.BaseManager;
import com.baseball.server.core.Umpire;

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
    private boolean aiIsPitcher = false; // AI가 투수인지 타자인지
    private javax.swing.Timer aiTimer;
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
        clients.remove(handler);
        System.out.println("[서버] 클라이언트 연결 끊김. 남은 클라이언트: " + clients.size());

        // 게임 중 한 명이 나가면 AI 모드 시작
        if (clients.size() == 1 && !aiMode) {
            broadcast(new GameMessage(MessageType.STATE_UPDATE, gameState));
            startAiMode();
        }
    }

    // AI 모드 시작
    public void startAiMode() {
        if (aiMode) return;
        aiMode = true;

        // AI는 현재 pendingPitch 상태에 따라 투수/타자 결정
        System.out.println("[서버] AI 모드 시작!");
        broadcast(new GameMessage(MessageType.STATE_UPDATE, gameState));

        // 3초 후 AI 첫 행동
        aiTimer = new javax.swing.Timer(3000, e -> {
            aiTimer.stop();
            performAiAction();
        });
        aiTimer.setRepeats(false);
        aiTimer.start();
    }

    // AI 행동
    public synchronized void performAiAction() {
        if (!aiMode || clients.isEmpty()) return;

        if (pendingPitch == null) {
            // AI 투구
            int speed = 80 + rand.nextInt(60);
            char[] types = {'f', 'c', 's'};
            char type = types[rand.nextInt(3)];
            PitchData aiPitch = new PitchData(speed + "" + type);
            setPendingPitch(aiPitch);
            gameState.setLastMessage("[AI] 투수가 " + speed + "km/h 공을 던졌습니다!");
            broadcast(new GameMessage(MessageType.STATE_UPDATE, gameState));
            broadcast(new GameMessage(MessageType.ACTION_PITCH));

            // 3초 후 AI 타격
            aiTimer = new javax.swing.Timer(3000, e -> {
                aiTimer.stop();
                performAiSwing();
            });
            aiTimer.setRepeats(false);
            aiTimer.start();

        } else {
            performAiSwing();
        }
    }

    // AI 타격
    private void performAiSwing() {
        if (!aiMode || clients.isEmpty()) return;

        PitchData pitch = getPendingPitch();
        if (pitch == null) {
            // 투구가 없으면 AI가 투구
            performAiAction();
            return;
        }

        int typeIdx = rand.nextInt(3) + 1;
        char[] timings = {'f', 'e', 'l'};
        char timing = timings[rand.nextInt(3)];
        SwingData aiSwing = new SwingData(typeIdx + "" + timing);

        setPendingPitch(null);
        GameMessage swingResult = umpire.judgeSwing(pitch, aiSwing);
        processUmpireResult(swingResult);

        // 다음 투구를 위해 3초 후 다시 AI 행동
        aiTimer = new javax.swing.Timer(3000, e -> {
            aiTimer.stop();
            performAiAction();
        });
        aiTimer.setRepeats(false);
        aiTimer.start();
    }

    // 심판 결과 처리 (ClientHandler와 공유)
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
                    broadcast(new GameMessage(MessageType.GAME_OVER, result));
                    if (aiTimer != null) aiTimer.stop();
                    aiMode = false;
                    return;
                }

                state.setLastMessage("3아웃! 공수 교대! " + state.getInning() + "회 초 시작!");
                broadcast(new GameMessage(MessageType.INNING_OVER));
            }

            broadcast(new GameMessage(MessageType.SWAP_TURN));
        }

        broadcast(new GameMessage(MessageType.STATE_UPDATE, state));
    }

    public int getClientCount() { return clients.size(); }
    public int getPlayerCount() { return playerCount; }
    public GameState getGameState() { return gameState; }
    public Umpire getUmpire() { return umpire; }
    public BaseManager getBaseManager() { return baseManager; }
    public PitchData getPendingPitch() { return pendingPitch; }
    public void setPendingPitch(PitchData pitch) { this.pendingPitch = pitch; }
    public boolean isAiMode() { return aiMode; }

    public static void main(String[] args) {
        new GameServer().startServer();
    }
}