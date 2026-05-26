package com.baseball.server.network;

import com.baseball.common.protocol.MessageType;
import com.baseball.common.protocol.GameMessage;
import com.baseball.common.model.GameState;
import com.baseball.common.model.PitchData;
import com.baseball.common.model.SwingData;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private GameServer server;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                GameMessage message = (GameMessage) in.readObject();
                MessageType type = message.getType();

                switch (type) {
                    case LOGIN:
                        String username = (String) message.getData();
                        System.out.println("[서버] " + username + " 님이 접속했습니다.");

                        // 접속 순서에 따라 역할 배정
                        int myOrder = server.incrementAndGetPlayerCount();
                        if (myOrder == 1) {
                            sendMessage(new GameMessage(MessageType.ROLE_PITCHER));
                            System.out.println("[서버] " + username + " → 투수 배정");
                        } else {
                            sendMessage(new GameMessage(MessageType.ROLE_BATTER));
                            System.out.println("[서버] " + username + " → 타자 배정");
                        }

                        if (server.getClientCount() == 2) {
                            System.out.println("[서버] 2명 매칭 완료! 게임 시작!");
                            server.broadcast(new GameMessage(MessageType.MATCH_COMPLETE));
                            server.broadcast(new GameMessage(MessageType.STATE_UPDATE, server.getGameState()));
                        }
                        break;

                    case ACTION_PITCH:
                        PitchData pitch = (PitchData) message.getData();
                        server.setPendingPitch(pitch);
                        server.getGameState().setLastMessage("투수가 공을 던졌습니다! 타자는 타이밍을 선택하세요.");
                        server.broadcast(new GameMessage(MessageType.STATE_UPDATE, server.getGameState()));
                        
                        // 타자에게 타격 활성화 신호 보내기
                        server.broadcast(new GameMessage(MessageType.ACTION_PITCH));
                        break;
                    case ACTION_SWING:
                        SwingData swing = (SwingData) message.getData();
                        PitchData lastPitch = server.getPendingPitch();

                        if (lastPitch == null) {
                            sendMessage(new GameMessage(MessageType.STATE_UPDATE, server.getGameState()));
                            System.out.println("[서버 경고] 타자가 투구 전에 스윙을 시도했습니다.");
                            break;
                        }

                        GameMessage swingResult = server.getUmpire().judgeSwing(lastPitch, swing);
                        processUmpireResult(swingResult);
                        break;

                    case ACTION_TAKE:
                        PitchData takePitch = server.getPendingPitch();
                        GameMessage takeResult = server.getUmpire().judgeTake(takePitch);
                        processUmpireResult(takeResult);
                        break;

                    case DISCONNECT:
                        System.out.println("[서버] 클라이언트가 게임을 종료했습니다.");
                        return;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("클라이언트와의 연결이 끊어졌습니다.");
        } finally {
            closeResources();
        }
    }

    public void sendMessage(GameMessage msg) {
        try {
            out.reset();
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeResources() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("클라이언트 자원 반납이 완료되었습니다.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processUmpireResult(GameMessage resultMsg) {
        GameState state = server.getGameState();
        MessageType resultType = resultMsg.getType();

        if (resultType == MessageType.RESULT_HIT) {
            int advanceBases = (Integer) resultMsg.getData();
            server.getBaseManager().processHit(state, advanceBases);

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
                server.getBaseManager().processHit(state, 1);
                state.setLastMessage("볼넷! 타자가 1루로 걸어 나갑니다.");
            }
        }

        // 판정 결과 브로드캐스트
        server.broadcast(resultMsg);

        // 3아웃 공수교대 체크
        if (state.getOutCount() >= 3) {
            state.resetInning();

            boolean wasTop = state.isTop();
            state.setTop(!wasTop);

            if (!state.isTop()) {
                // 초 → 말로 전환
                state.setLastMessage("3아웃! 공수 교대! " + state.getInning() + "회 말 시작!");
            } else {
                // 말 → 초로 전환 → 이닝 증가
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
                    server.broadcast(new GameMessage(MessageType.STATE_UPDATE, state));
                    server.broadcast(new GameMessage(MessageType.GAME_OVER, result));
                    return;
                }

                state.setLastMessage("3아웃! 공수 교대! " + state.getInning() + "회 초 시작!");
                server.broadcast(new GameMessage(MessageType.INNING_OVER));
            }

            server.broadcast(new GameMessage(MessageType.SWAP_TURN));
        }

        // 최종 전광판 상태 브로드캐스트
        server.broadcast(new GameMessage(MessageType.STATE_UPDATE, state));
    }
}