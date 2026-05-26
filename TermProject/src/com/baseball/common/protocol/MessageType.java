package com.baseball.common.protocol;

public enum MessageType {
    // 1. 접속 및 방 관리
    LOGIN,
    MATCHING_REQ,
    MATCH_COMPLETE,
    DISCONNECT,

    // 2. 역할 배정
    ROLE_PITCHER,   // 서버 -> 클라이언트: 너는 투수야
    ROLE_BATTER,    // 서버 -> 클라이언트: 너는 타자야

    // 3. 클라이언트 행동
    ACTION_PITCH,
    ACTION_SWING,
    ACTION_TAKE,

    // 4. 서버 판정
    RESULT_BALL,
    RESULT_STRIKE,
    RESULT_HIT,
    RESULT_OUT,

    // 5. 게임 흐름
    STATE_UPDATE,
    SWAP_TURN,
    INNING_OVER,
    GAME_OVER
}