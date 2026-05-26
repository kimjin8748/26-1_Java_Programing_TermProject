package com.baseball.client.gui;

import java.awt.EventQueue;
import javax.swing.*;

public class LoginPanel {

    private JFrame frame;
    private JTextField textFieldStudentId;
    private JTextField textFieldName;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                LoginPanel window = new LoginPanel();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public LoginPanel() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("야구 게임 - 접속");
        frame.setBounds(100, 100, 400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JLabel lblStudentId = new JLabel("학번:");
        lblStudentId.setBounds(80, 80, 60, 25);
        frame.getContentPane().add(lblStudentId);

        textFieldStudentId = new JTextField();
        textFieldStudentId.setBounds(150, 80, 150, 25);
        textFieldStudentId.setColumns(10);
        frame.getContentPane().add(textFieldStudentId);

        JLabel lblName = new JLabel("이름:");
        lblName.setBounds(80, 120, 60, 25);
        frame.getContentPane().add(lblName);

        textFieldName = new JTextField();
        textFieldName.setBounds(150, 120, 150, 25);
        textFieldName.setColumns(10);
        frame.getContentPane().add(textFieldName);

        JButton btnConnect = new JButton("접속");
        btnConnect.setBounds(150, 170, 150, 30);
        frame.getContentPane().add(btnConnect);

        // 접속 버튼 이벤트
        btnConnect.addActionListener(e -> {
            String studentId = textFieldStudentId.getText().trim();
            String name = textFieldName.getText().trim();

            if (studentId.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "학번과 이름을 입력해주세요!");
                return;
            }

            System.out.println("접속: " + studentId + " / " + name);

            // TODO: ServerConnection 연결 후 WaitingPanel 띄우기
            frame.dispose();
            WaitingPanel waiting = new WaitingPanel(studentId, name);
            waiting.show();
        });
    }
}