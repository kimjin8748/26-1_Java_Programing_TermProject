package com.baseball.client.gui;

import javax.swing.*;
import javax.swing.text.*;
import com.baseball.client.controller.GameController;

public class LoginPanel extends JPanel {
    private GameController controller;
    private JTextField textFieldStudentId;
    private JTextField textFieldName;
    private JLabel lblErrorStudentId;
    private JLabel lblErrorName;

    public LoginPanel(GameController controller) {
        this.controller = controller;
        initialize();
    }

    // 글자 수 제한 필터
    private DocumentFilter createLengthFilter(int maxLength) {
        return new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null) return;
                if ((fb.getDocument().getLength() + string.length()) <= maxLength) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) return;
                if ((fb.getDocument().getLength() - length + text.length()) <= maxLength) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        };
    }

    private void initialize() {
        this.setLayout(new java.awt.GridBagLayout());

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(null);
        centerPanel.setPreferredSize(new java.awt.Dimension(400, 300));

        JLabel lblStudentId = new JLabel("학번:");
        lblStudentId.setBounds(80, 60, 60, 25);
        centerPanel.add(lblStudentId);

        textFieldStudentId = new JTextField();
        textFieldStudentId.setBounds(150, 60, 150, 25);
        textFieldStudentId.setColumns(10);
        // 학번 10글자 제한
        ((AbstractDocument) textFieldStudentId.getDocument())
            .setDocumentFilter(createLengthFilter(10));
        centerPanel.add(textFieldStudentId);

        lblErrorStudentId = new JLabel("⚠️ 학번은 숫자만 입력해주세요!");
        lblErrorStudentId.setBounds(80, 87, 250, 20);
        lblErrorStudentId.setForeground(java.awt.Color.RED);
        lblErrorStudentId.setFont(new java.awt.Font("굴림", java.awt.Font.PLAIN, 11));
        lblErrorStudentId.setVisible(false);
        centerPanel.add(lblErrorStudentId);

        JLabel lblName = new JLabel("이름:");
        lblName.setBounds(80, 110, 60, 25);
        centerPanel.add(lblName);

        textFieldName = new JTextField();
        textFieldName.setBounds(150, 110, 150, 25);
        textFieldName.setColumns(10);
        // 이름 10글자 제한
        ((AbstractDocument) textFieldName.getDocument())
            .setDocumentFilter(createLengthFilter(10));
        centerPanel.add(textFieldName);

        lblErrorName = new JLabel("⚠️ 이름은 한글 또는 영어만 입력해주세요!");
        lblErrorName.setBounds(80, 137, 280, 20);
        lblErrorName.setForeground(java.awt.Color.RED);
        lblErrorName.setFont(new java.awt.Font("굴림", java.awt.Font.PLAIN, 11));
        lblErrorName.setVisible(false);
        centerPanel.add(lblErrorName);

        JButton btnConnect = new JButton("접속");
        btnConnect.setBounds(150, 170, 150, 30);
        centerPanel.add(btnConnect);

        // 실시간 학번 검증
        textFieldStudentId.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validateStudentId(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validateStudentId(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validateStudentId(); }
        });

        // 실시간 이름 검증
        textFieldName.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validateName(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validateName(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validateName(); }
        });

        btnConnect.addActionListener(e -> {
            String studentId = textFieldStudentId.getText().trim();
            String name = textFieldName.getText().trim();

            if (studentId.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "학번과 이름을 입력해주세요!");
                return;
            }
            if (!studentId.matches("\\d+")) {
                lblErrorStudentId.setVisible(true);
                textFieldStudentId.requestFocus();
                return;
            }
            if (!name.matches("[a-zA-Z가-힣]+")) {
                lblErrorName.setVisible(true);
                textFieldName.requestFocus();
                return;
            }

            String username = studentId + "_" + name;
            System.out.println("접속 시도: " + username);
            controller.login(username);
        });

        this.add(centerPanel, new java.awt.GridBagConstraints());
    }

    private void validateStudentId() {
        String text = textFieldStudentId.getText().trim();
        if (text.isEmpty() || text.matches("\\d+")) {
            lblErrorStudentId.setVisible(false);
        } else {
            lblErrorStudentId.setVisible(true);
        }
    }

    private void validateName() {
        String text = textFieldName.getText().trim();
        if (text.isEmpty() || text.matches("[a-zA-Z가-힣]+")) {
            lblErrorName.setVisible(false);
        } else {
            lblErrorName.setVisible(true);
        }
    }
}