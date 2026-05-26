package com.baseball.client.gui;

import java.awt.EventQueue;
import javax.swing.*;

import com.baseball.client.controller.GameController;

public class LoginPanel extends JPanel{
	//게임 컨트롤러(지휘관)를 기억할 변수
	private GameController controller;

    private JTextField textFieldStudentId;
    private JTextField textFieldName;

    public LoginPanel(GameController controller) {
    	this.controller = controller;
        initialize();
    }

    private void initialize() {
    	this.setLayout(null);
        this.setSize(400, 300); // 패널 크기만 잡아줍니다.

        JLabel lblStudentId = new JLabel("학번:");
        lblStudentId.setBounds(80, 80, 60, 25);
        this.add(lblStudentId);

        textFieldStudentId = new JTextField();
        textFieldStudentId.setBounds(150, 80, 150, 25);
        textFieldStudentId.setColumns(10);
        this.add(textFieldStudentId);

        JLabel lblName = new JLabel("이름:");
        lblName.setBounds(80, 120, 60, 25);
        this.add(lblName);

        textFieldName = new JTextField();
        textFieldName.setBounds(150, 120, 150, 25);
        textFieldName.setColumns(10);
        this.add(textFieldName);

        JButton btnConnect = new JButton("접속");
        btnConnect.setBounds(150, 170, 150, 30);
        this.add(btnConnect);

        // 접속 버튼 이벤트
        btnConnect.addActionListener(e -> {
            String studentId = textFieldStudentId.getText().trim();
            String name = textFieldName.getText().trim();

            if (studentId.isEmpty() || name.isEmpty()) {
            	JOptionPane.showMessageDialog(this, "학번과 이름을 입력해주세요!");
                return;
            }

            String username = studentId + "_" + name; 
            System.out.println("접속 시도: " + username);

            controller.login(username);
        });
    }
}