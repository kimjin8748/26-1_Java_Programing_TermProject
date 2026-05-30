package com.baseball.client.gui;

import com.baseball.client.controller.GameController;
import com.baseball.common.protocol.GameMessage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;

public class MainFrame extends JFrame {
    private GameController controller;

    private CardLayout cardLayout;
    private JPanel mainContainer;

    private LoginPanel loginPanel;
    private WaitingPanel waitingPanel;
    private GamePanel gamePanel;

    public MainFrame(GameController controller) {
        this.controller = controller;

        setTitle("네트워크 야구 게임");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setPreferredSize(
            java.awt.Toolkit.getDefaultToolkit().getScreenSize()
        );

        loginPanel = new LoginPanel(controller);
        waitingPanel = new WaitingPanel();
        gamePanel = new GamePanel(controller);

        mainContainer.add(loginPanel, "LOGIN");
        mainContainer.add(waitingPanel, "WAITING");
        mainContainer.add(gamePanel, "GAME");

        add(mainContainer);
        setLocationRelativeTo(null);
        cardLayout.show(mainContainer, "LOGIN");
    }

    public void showWaitingPanel() {
        cardLayout.show(mainContainer, "WAITING");
    }

    public void showBoardPanel() {
        cardLayout.show(mainContainer, "GAME");
    }

    public void showBoardPanelWithName(String myName, String opponentName) {
        gamePanel.setPlayerNames(myName, opponentName);
        cardLayout.show(mainContainer, "GAME");
    }

    public void updateGameScreen(GameMessage msg) {
        gamePanel.updateScreen(msg);
    }
}