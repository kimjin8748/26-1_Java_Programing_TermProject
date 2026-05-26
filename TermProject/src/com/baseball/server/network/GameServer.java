package com.baseball.server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.baseball.common.model.GameState;
import com.baseball.common.model.PitchData;
import com.baseball.common.protocol.GameMessage;
import com.baseball.server.core.BaseManager;
import com.baseball.server.core.Umpire;

public class GameServer {
    private static final int PORT = 8080;
    // 현재 접속 중인 클라이언트들을 관리하는 리스트
    private List<ClientHandler> clients = new ArrayList<>();
    
    private GameState gameState = new GameState();
    private Umpire umpire = new Umpire();
    private BaseManager baseManager = new BaseManager();
    private PitchData pendingPitch; // 투수가 던진 공을 타자가 칠 때까지 잠시 보관하는 변수

    public void startServer() {
        // 기존 startServer() 로직과 동일
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
    
    public int getClientCount() {
        return clients.size();
    }
    
    // Getters & Setters
    public GameState getGameState() { return gameState; }
    public Umpire getUmpire() { return umpire; }
    public BaseManager getBaseManager() { return baseManager; }
    
    public PitchData getPendingPitch() { return pendingPitch; }
    public void setPendingPitch(PitchData pitch) { this.pendingPitch = pitch; }

    public static void main(String[] args) {
        new GameServer().startServer();
    }
}