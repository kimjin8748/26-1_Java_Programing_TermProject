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

// Runnable을 구현하여 독립적인 스레드에서 돌아가도록 설정
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
            // 🚨 [매우 중요] 반드시 ObjectOutputStream을 먼저 생성하고 flush() 해야 데드락에 빠지지 않습니다!
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // 클라이언트가 보내는 메시지를 무한히 듣는 루프
            while (true) {
                // MessageType이 아니라 GameMessage(편지 봉투) 전체를 읽어옵니다.
                GameMessage message = (GameMessage) in.readObject();
                MessageType type = message.getType();
                
                // 메시지 타입에 따라 분기 처리
                switch (type) {
                case LOGIN:
                    String username = (String) message.getData();
                    System.out.println("[서버] " + username + " 님이 접속했습니다.");
                    break;

                case ACTION_PITCH:
                    // 1. 투수가 공을 던지면, 서버에 그 공을 잠시 저장합니다.
                    PitchData pitch = (PitchData) message.getData();
                    server.setPendingPitch(pitch); 
                    
                    // 2. 투구 사실을 양쪽에 알립니다.
                    server.getGameState().setLastMessage("투수가 공을 던졌습니다! 타자는 타이밍을 선택하세요.");
                    server.broadcast(new GameMessage(MessageType.STATE_UPDATE, server.getGameState()));
                    break;

                case ACTION_SWING:
                    // 1. 타자의 스윙 데이터와 아까 서버에 저장해둔 투구 데이터를 꺼냅니다.
                    SwingData swing = (SwingData) message.getData();
                    PitchData lastPitch = server.getPendingPitch();
                    
                    // 투수가 공을 던지지 않았는데 스윙을 시도한 경우 방어
                    if (lastPitch == null) {
                        sendMessage(new GameMessage(MessageType.STATE_UPDATE, server.getGameState()));
                        System.out.println("[서버 경고] 타자가 투구 전에 스윙을 시도했습니다.");
                        break; 
                    }
                    
                    // 2. 심판(Umpire)에게 판정을 맡깁니다.
                    GameMessage swingResult = server.getUmpire().judgeSwing(lastPitch, swing);
                    
                    // 3. 판정 결과에 따라 로직을 분기합니다.
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
            // 예외가 발생하든 정상 종료하든 무조건 자원을 반납합니다.
            closeResources();
        }
    }
    
    // 클라이언트에게 객체를 전송할 때도 GameMessage를 사용합니다.
    public void sendMessage(GameMessage msg) {
        try {
        	out.reset();
            out.writeObject(msg);
            out.flush(); // 파이프에 남은 데이터를 억지로 밀어내기
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 스트림과 소켓을 안전하게 닫아주는 메모리 누수 방지 로직
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
    
    /**
     * 심판의 판정 결과를 받아 게임 상태(GameState)에 반영하고 브로드캐스트합니다.
     */
    private void processUmpireResult(GameMessage resultMsg) {
        GameState state = server.getGameState();
        MessageType resultType = resultMsg.getType();

        if (resultType == MessageType.RESULT_HIT) {
            // 안타인 경우 BaseManager를 호출해 진루 처리
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
                // 볼넷 처리 (1루타와 동일하게 1칸 진루)
                server.getBaseManager().processHit(state, 1);
                state.setLastMessage("볼넷! 타자가 1루로 걸어 나갑니다.");
            }
        }

        // 3아웃 공수 교대 체크
        if (state.getOutCount() >= 3) {
            state.resetInning();
            state.setTop(!state.isTop()); // 초/말 뒤집기
            state.setLastMessage("3아웃! 공수 교대됩니다.");
            server.broadcast(new GameMessage(MessageType.SWAP_TURN));
        }

        // 변경된 최종 전광판 상태를 모든 클라이언트(투수, 타자)에게 전송
        server.broadcast(new GameMessage(MessageType.STATE_UPDATE, state));
    }
}