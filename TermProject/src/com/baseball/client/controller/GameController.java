package com.baseball.client.controller;

import com.baseball.client.network.GameClient;
import com.baseball.client.gui.MainFrame;
import com.baseball.common.model.PitchData;
import com.baseball.common.model.SwingData;
import com.baseball.common.protocol.GameMessage;
import com.baseball.common.protocol.MessageType;

import javax.swing.SwingUtilities;

public class GameController {
    private MainFrame view;
    private GameClient networkClient;

    public GameController() {
        this.view = new MainFrame(this);
        this.networkClient = new GameClient(this);
    }

    public void startGame() {
        view.setVisible(true);
        networkClient.connect();
    }

    // ==========================================
    // ⬇️ View에서 발생하는 이벤트 처리 (Action)
    // ==========================================
    
    public void login(String username) {
        // 1. 서버에 로그인(접속) 메시지 전송
        networkClient.send(new GameMessage(MessageType.LOGIN, username));
        
        // 2. 뷰(GUI)에게 "대기 화면으로 전환해!" 라고 지시
        view.showWaitingPanel(); 
    }

    public void sendPitch(String input) {
        networkClient.send(new GameMessage(MessageType.ACTION_PITCH, new PitchData(input)));
    }

    public void sendSwing(String input) {
        networkClient.send(new GameMessage(MessageType.ACTION_SWING, new SwingData(input)));
    }

    public void sendTake() {
        networkClient.send(new GameMessage(MessageType.ACTION_TAKE));
    }

    // ==========================================
    // ⬆️ 서버로부터 도착한 메시지 처리 (Response)
    // ==========================================
    
    public void onMessageReceived(GameMessage msg) {
        SwingUtilities.invokeLater(() -> {
            MessageType type = msg.getType();

            if (type == MessageType.MATCH_COMPLETE) {
                // 매칭이 완료되었다면 게임판 화면으로 전환
                view.showBoardPanel();
            } else {
                // 그 외의 게임 진행 메시지(STATE_UPDATE 등)는 게임판을 갱신
                view.updateGameScreen(msg); 
            }
        });
    }
    
    public static void main(String[] args) {
        new GameController().startGame();
    }
}