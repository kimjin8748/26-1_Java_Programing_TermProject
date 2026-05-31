package com.baseball.client.network;

import com.baseball.client.controller.GameController;
import com.baseball.common.protocol.GameMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GameClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8080;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    //GUI와 소통하기 위한 컨트롤러 참조 변수
    private GameController controller;

    // 생성자: 객체가 만들어질 때 컨트롤러를 넘겨받습니다.
    public GameClient(GameController controller) {
        this.controller = controller;
    }

    public void connect() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            
            // 스트림 초기화 시 반드시 out부터 열고 flush()
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("[네트워크] 서버에 성공적으로 연결되었습니다!");

            // 서버 메시지를 비동기로 계속 듣는 [수신 전용 스레드] 시작
            Thread receiverThread = new Thread(this::receiveMessages);
            receiverThread.start();

           

        } catch (IOException e) {
            System.out.println("[오류] 서버에 접속할 수 없습니다. 서버가 켜져 있는지 확인하세요.");
        }
    }

    /**
     * [수신 스레드] 서버로부터 날아오는 객체를 계속 읽어서 컨트롤러로 전달합니다.
     */
    private void receiveMessages() {
        try {
            while (true) {
                // 서버에서 보낸 편지(GameMessage)를 받습니다.
                GameMessage msg = (GameMessage) in.readObject();
                
               
                //  컨트롤러에게 받은 메시지를 그대로 던져줍니다.
                controller.onMessageReceived(msg);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[네트워크] 서버와의 연결이 끊어졌습니다.");
            closeResources();
        }
    }

    /**
     * [발신 메서드] 컨트롤러가 버튼 클릭 입력을 받으면 이 메서드를 호출하여 서버로 전송합니다.
     */
    public void send(GameMessage msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("[오류] 메시지 전송에 실패했습니다.");
            e.printStackTrace();
        }
    }

    private void closeResources() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}