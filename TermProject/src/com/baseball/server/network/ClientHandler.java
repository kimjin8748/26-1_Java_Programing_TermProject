package com.baseball.server.network;

import com.baseball.common.protocol.MessageType;
import com.baseball.common.protocol.GameMessage;
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
                        // Object 타입의 데이터를 PitchData로 안전하게 형변환
                        PitchData pitch = (PitchData) message.getData();
                        System.out.println("[서버] 투수 투구 - 구속: " + pitch.getSpeed() + "km/h, 구질: " + pitch.getPitchType());
                        break;

                    case ACTION_SWING:
                        // Object 타입의 데이터를 SwingData로 안전하게 형변환
                        SwingData swing = (SwingData) message.getData();
                        System.out.println("[서버] 타자 스윙 - 방향: " + swing.getHitDirection() + ", 타이밍: " + swing.getTiming());
                        break;

                    case ACTION_TAKE:
                        System.out.println("[서버] 타자가 공을 지켜봅니다.");
                        break;

                    case DISCONNECT:
                        System.out.println("[서버] 클라이언트가 게임을 종료했습니다.");
                        return; // while 무한 루프를 빠져나가 스레드를 정상 종료시킵니다.

                    default:
                        System.out.println("[서버] 알 수 없는 메시지 타입입니다: " + type);
                        break;
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
            out.writeObject(msg);
            out.flush(); // 파이프에 남은 데이터를 억지로 밀어내기
        } catch (IOException e) {
            e.printStackTrace();//ddddddddd
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
}