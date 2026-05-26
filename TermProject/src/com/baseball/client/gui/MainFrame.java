package com.baseball.client.gui;

import com.baseball.client.controller.GameController;
import com.baseball.common.protocol.GameMessage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;

public class MainFrame extends JFrame {
    private GameController controller;
    
    // 카드를 겹쳐서 보여줄 레이아웃과 빈 쟁반(컨테이너)
    private CardLayout cardLayout;
    private JPanel mainContainer;

    // 팀원분이 만드신 3개의 패널 객체
    private LoginPanel loginPanel;
    private WaitingPanel waitingPanel;
    private GamePanel gamePanel;

    public MainFrame(GameController controller) {
        this.controller = controller;
        
        // 1. 기본 창(Frame) 설정
        setTitle("네트워크 야구 게임");
        setSize(800, 600); // 팀원이 설정한 패널 크기에 맞춰 수정하세요
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 창을 화면 정중앙에 띄움

        // 2. 레이아웃 설정
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // 3. 패널 객체 생성 (버튼 이벤트를 위해 컨트롤러를 넘겨줌)
        loginPanel = new LoginPanel(controller);
        waitingPanel = new WaitingPanel(); // 대기 화면은 조작할 버튼이 없으므로 컨트롤러 생략 가능
        gamePanel = new GamePanel(controller);

        // 4. 컨테이너에 패널들을 이름표와 함께 등록
        mainContainer.add(loginPanel, "LOGIN");
        mainContainer.add(waitingPanel, "WAITING");
        mainContainer.add(gamePanel, "GAME");

        // 5. 창에 컨테이너 부착
        add(mainContainer);
        
        // 처음 켜면 무조건 로그인 화면부터 보여줍니다.
        cardLayout.show(mainContainer, "LOGIN"); 
    }

    // ==========================================
    // 컨트롤러가 호출할 화면 전환 및 갱신 메서드들
    // ==========================================

    public void showWaitingPanel() {
        cardLayout.show(mainContainer, "WAITING");
    }

    public void showBoardPanel() {
        cardLayout.show(mainContainer, "GAME");
    }

    public void updateGameScreen(GameMessage msg) {
        // 실제 게임 화면을 그리는 것은 BoardPanel이므로 그쪽으로 데이터를 넘겨줍니다.
    	gamePanel.updateScreen(msg); 
    }
}