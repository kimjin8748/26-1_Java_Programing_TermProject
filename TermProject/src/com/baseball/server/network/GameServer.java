package com.baseball.server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {
    private static final int PORT = 8080;
    // 현재 접속 중인 클라이언트들을 관리하는 리스트
    private List<ClientHandler> clients = new ArrayList<>();

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("야구 게임 서버가 포트 " + PORT + "에서 시작되었습니다.");

            while (true) {
                // 클라이언트 접속 대기 (접속할 때까지 여기서 코드가 멈춤)
                Socket clientSocket = serverSocket.accept();
                System.out.println("새로운 클라이언트가 접속했습니다: " + clientSocket.getInetAddress());

                // 해당 클라이언트를 전담할 스레드 생성 및 실행
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                
                Thread clientThread = new Thread(handler);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new GameServer().startServer();
    }
}