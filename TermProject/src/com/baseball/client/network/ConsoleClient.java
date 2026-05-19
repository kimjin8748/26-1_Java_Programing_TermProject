package com.baseball.client.network;

import com.baseball.common.model.GameState;
import com.baseball.common.model.PitchData;
import com.baseball.common.model.SwingData;
import com.baseball.common.protocol.GameMessage;
import com.baseball.common.protocol.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ConsoleClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8080;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void connect() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("=================================================");
            System.out.println(" ⚾ 야구 게임 서버에 연결되었습니다! ⚾");
            System.out.println(" [명령어 안내]");
            System.out.println(" 1. 접속: /login [이름]");
            System.out.println(" 2. 투구: /pitch [구속+구질] (예: /pitch 145c)");
            System.out.println(" 3. 스윙: /swing [방향+타이밍] (예: /swing 1f)");
            System.out.println(" 4. 대기: /take");
            System.out.println(" 5. 종료: /quit");
            System.out.println("=================================================");

            // 1. 서버 메시지를 비동기로 계속 듣는 [수신 전용 스레드] 시작
            Thread receiverThread = new Thread(this::receiveMessages);
            receiverThread.start();

            // 2. 메인 스레드는 사용자의 키보드 입력을 처리합니다.
            handleUserInput();

        } catch (IOException e) {
            System.out.println("[오류] 서버에 접속할 수 없습니다. 서버가 켜져 있는지 확인하세요.");
        }
    }

    /**
     * [수신 스레드] 서버로부터 날아오는 객체를 계속 읽어서 콘솔에 출력합니다.
     */
    private void receiveMessages() {
        try {
            while (true) {
                GameMessage msg = (GameMessage) in.readObject();
                MessageType type = msg.getType();

                // 상태 업데이트(전광판 갱신) 메시지일 경우 예쁘게 포맷팅하여 출력
                if (type == MessageType.STATE_UPDATE) {
                    GameState state = (GameState) msg.getData();
                    System.out.println("\n-------------------------------------------------");
                    System.out.println(" 📢 [중계석] " + state.getLastMessage());
                    System.out.println(" 📊 [전광판] " + state.getInning() + "회 " + (state.isTop() ? "초" : "말") + 
                                       " | 원정 " + state.getAwayScore() + " : " + state.getHomeScore() + " 홈");
                    System.out.println(" ⚾ [카운트] " + state.getBallCount() + "B " + state.getStrikeCount() + "S " + state.getOutCount() + "O");
                    System.out.println(" 🏃 [주 자] 1루(" + (state.getBases()[0]?"O":"X") + 
                                       ") 2루(" + (state.getBases()[1]?"O":"X") + 
                                       ") 3루(" + (state.getBases()[2]?"O":"X") + ")");
                    System.out.println("-------------------------------------------------");
                } 
                else if (type == MessageType.SWAP_TURN) {
                    System.out.println("\n🔄 공수 교대! 🔄");
                }
                
                System.out.print("\n입력 대기중 > "); // 메시지 출력 후 다시 프롬프트 표시
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("\n서버와의 연결이 끊어졌습니다.");
            System.exit(0);
        }
    }

    /**
     * [발신 스레드] 키보드 입력을 분석하여 해당하는 GameMessage를 조립해 서버로 전송합니다.
     */
    private void handleUserInput() {
        Scanner scanner = new Scanner(System.in);
        try {
            while (true) {
                System.out.print("입력 대기중 > ");
                String input = scanner.nextLine();
                if (input.trim().isEmpty()) continue;

                String[] parts = input.split(" ");
                String command = parts[0];

                if (command.equals("/quit")) {
                    send(new GameMessage(MessageType.DISCONNECT));
                    break;
                } else if (command.equals("/login") && parts.length > 1) {
                    send(new GameMessage(MessageType.LOGIN, parts[1]));
                } else if (command.equals("/pitch") && parts.length > 1) {
                    send(new GameMessage(MessageType.ACTION_PITCH, new PitchData(parts[1])));
                } else if (command.equals("/swing") && parts.length > 1) {
                    send(new GameMessage(MessageType.ACTION_SWING, new SwingData(parts[1])));
                } else if (command.equals("/take")) {
                    send(new GameMessage(MessageType.ACTION_TAKE));
                } else {
                    System.out.println("알 수 없는 명령어거나 인자값이 부족합니다.");
                }
            }
        } finally {
            scanner.close();
            closeResources();
        }
    }

    private void send(GameMessage msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeResources() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ConsoleClient().connect();
    }
}