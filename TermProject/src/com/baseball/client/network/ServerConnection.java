package com.baseball.client.network;

import com.baseball.common.protocol.GameMessage;
import com.baseball.common.protocol.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnection {
    private static final String SERVER_IP = "127.0.0.1"; // 내 컴퓨터 주소 (로컬 호스트)
    private static final int SERVER_PORT = 8080;         // 서버와 약속한 포트 번호

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // 서버에 접속하는 메서드
    public void connect() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            System.out.println("[클라이언트] 서버에 성공적으로 접속했습니다!");

            // 🚨 서버와 마찬가지로 반드시 출력 스트림(Out)부터 열고 flush() 해야 합니다!
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // 1. 접속하자마자 서버에 LOGIN 메시지 보내보기 (테스트)
            GameMessage loginMsg = new GameMessage(MessageType.LOGIN, "Player1");
            send(loginMsg);

        } catch (IOException e) {
            System.out.println("[클라이언트] 서버 접속에 실패했습니다. 서버가 켜져 있는지 확인하세요.");
            e.printStackTrace();
        }
    }

    // 서버로 메시지를 전송하는 메서드
    public void send(GameMessage msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 테스트를 위한 임시 메인 메서드
    public static void main(String[] args) {
        ServerConnection client = new ServerConnection();
        client.connect();
    }
}