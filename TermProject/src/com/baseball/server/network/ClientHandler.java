	package com.baseball.server.network;

import com.baseball.common.protocol.MessageType;
import com.baseball.common.protocol.GameMessage;
import com.baseball.common.model.GameState;
import com.baseball.common.model.PitchData;
import com.baseball.common.model.SwingData;
import javax.swing.SwingUtilities;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private GameServer server;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    private boolean isPitcher = false;

    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
    }

    public boolean isPitcher() { return isPitcher; }

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
                        server.addPlayerName(username);
                        System.out.println("[서버] " + username + " 님이 접속했습니다.");

                        int myOrder = server.incrementAndGetPlayerCount();
                        if (myOrder == 1) {
                            isPitcher = true;
                            sendMessage(new GameMessage(MessageType.ROLE_PITCHER));
                            System.out.println("[서버] " + username + " → 투수 배정");
                        } else {
                            isPitcher = false;
                            sendMessage(new GameMessage(MessageType.ROLE_BATTER));
                            System.out.println("[서버] " + username + " → 타자 배정");
                        }

                        if (server.getClientCount() == 2) {
                            System.out.println("[서버] 2명 매칭 완료! 게임 시작!");
                            String[] names = server.getPlayerNames();
                            server.broadcast(new GameMessage(MessageType.MATCH_COMPLETE, names));
                            server.broadcast(new GameMessage(MessageType.STATE_UPDATE, server.getGameState()));
                        }
                        break;

                    case ACTION_PITCH:
                        PitchData pitch = (PitchData) message.getData();
                        server.setPendingPitch(pitch);
                        server.getGameState().setLastMessage("투수가 공을 던졌습니다! 타자는 타이밍을 선택하세요.");
                        server.broadcast(new GameMessage(MessageType.STATE_UPDATE, server.getGameState()));
                        server.broadcast(new GameMessage(MessageType.ACTION_PITCH));

                        // AI가 타자면 자동 타격
                        if (server.isAiMode() && !server.isAiPitcher()) {
                            SwingUtilities.invokeLater(() -> {
                                javax.swing.Timer t = new javax.swing.Timer(2000, e -> server.performAiSwing());
                                t.setRepeats(false);
                                t.start();
                            });
                        }
                        break;

                    case ACTION_SWING:
                        SwingData swing = (SwingData) message.getData();
                        PitchData lastPitch = server.getPendingPitch();

                        if (lastPitch == null) {
                            sendMessage(new GameMessage(MessageType.STATE_UPDATE, server.getGameState()));
                            System.out.println("[서버 경고] 타자가 투구 전에 스윙을 시도했습니다.");
                            break;
                        }

                        server.setPendingPitch(null);
                        GameMessage swingResult = server.getUmpire().judgeSwing(lastPitch, swing);
                        server.processUmpireResult(swingResult);

                        // AI가 투수면 다음 투구 준비
                        if (server.isAiMode() && server.isAiPitcher()) {
                            SwingUtilities.invokeLater(() -> {
                                javax.swing.Timer t = new javax.swing.Timer(2000, e -> server.performAiPitch());
                                t.setRepeats(false);
                                t.start();
                            });
                        }
                        break;

                    case ACTION_TAKE:
                        PitchData takePitch = server.getPendingPitch();
                        server.setPendingPitch(null);
                        GameMessage takeResult = server.getUmpire().judgeTake(takePitch);
                        server.processUmpireResult(takeResult);

                        // AI가 투수면 다음 투구 준비
                        if (server.isAiMode() && server.isAiPitcher()) {
                            SwingUtilities.invokeLater(() -> {
                                javax.swing.Timer t = new javax.swing.Timer(2000, e -> server.performAiPitch());
                                t.setRepeats(false);
                                t.start();
                            });
                        }
                        break;

                    case DISCONNECT:
                        System.out.println("[서버] 클라이언트가 게임을 종료했습니다.");
                        return;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("클라이언트와의 연결이 끊어졌습니다.");
        } finally {
            server.removeClient(this);
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
}