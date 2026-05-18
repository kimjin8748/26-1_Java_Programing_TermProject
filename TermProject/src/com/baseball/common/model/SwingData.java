package com.baseball.common.model;

import java.io.Serializable;

public class SwingData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int hitDirection; // 1: 밀어치기, 2: 당겨치기
    private char timing;      // 'f': 빠르게, 's': 느리게, 'w': 기다리기

    public SwingData(String consoleInput) {
        // "1f" 같은 입력이 들어오면 분리해서 저장합니다.
        this.hitDirection = Character.getNumericValue(consoleInput.charAt(0));
        this.timing = consoleInput.charAt(1);
    }

    public int getHitDirection() { return hitDirection; }
    public char getTiming() { return timing; }
}