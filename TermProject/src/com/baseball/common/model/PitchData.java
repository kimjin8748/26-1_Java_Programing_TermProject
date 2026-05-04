package com.baseball.common.model;

import java.io.Serializable;

public class PitchData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int speed;      // 구속 (예: 90)
    private char pitchType; // 구질 (예: 'c' 커브, 'f' 패스트볼, 'h' 체인지업, 's' 슬라이더)

    public PitchData(String consoleInput) {
        // "90c" 같은 입력이 들어오면 분리해서 저장합니다.
        int length = consoleInput.length();
        this.pitchType = consoleInput.charAt(length - 1);
        this.speed = Integer.parseInt(consoleInput.substring(0, length - 1));
    }

    public int getSpeed() { return speed; }
    public char getPitchType() { return pitchType; }
}