package com.baseball.common.protocol;

import java.io.Serializable;

/**
 * 소켓을 통해 실제로 주고받을 '편지 봉투' 객체.
 * 행동의 종류(type)와 실제 데이터(data)를 하나로 묶어서 전송합니다.
 */
public class GameMessage implements Serializable {
    private static final long serialVersionUID = 1L; // 직렬화 필수

    private MessageType type; // 편지의 목적 (예: ACTION_PITCH)
    private Object data;      // 편지의 내용물 (예: PitchData 객체)

    // 생성자
    public GameMessage(MessageType type, Object data) {
        this.type = type;
        this.data = data;
    }

    // 데이터가 필요 없는 단순 알림용 생성자 (예: SWAP_TURN)
    public GameMessage(MessageType type) {
        this.type = type;
        this.data = null;
    }

    // Getters
    public MessageType getType() { 
        return type; 
    }
    
    public Object getData() { 
        return data; 
    }
}