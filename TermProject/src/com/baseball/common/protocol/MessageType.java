package com.baseball.common.protocol;

/**
 * 서버와 클라이언트 간의 통신 메시지 타입을 정의하는 열거형
 */
public enum MessageType {
    // 1. 접속 및 방 관리 (Connection & Room)
    LOGIN,              // 클라이언트 -> 서버: 게임 서버 접속 요청
    MATCHING_REQ,       // 클라이언트 -> 서버: 게임 매칭(방 입장) 요청
    MATCH_COMPLETE,     // 서버 -> 클라이언트: 매칭 완료 및 게임 시작 알림
    DISCONNECT,         // 클라이언트 -> 서버: 게임 종료 및 연결 끊기

    // 2. 클라이언트의 행동 (Client Actions)
    ACTION_PITCH,       // 클라이언트(투수) -> 서버: 공을 던짐 (구속 데이터 포함)
    ACTION_SWING,       // 클라이언트(타자) -> 서버: 방망이를 휘두름 (스윙 여부)
    ACTION_TAKE,        // 클라이언트(타자) -> 서버: 타자가 공을 지켜봄

    // 3. 서버의 판정 및 결과 브로드캐스트 (Server Broadcasts)
    RESULT_BALL,        // 서버 -> 클라이언트: 볼 판정
    RESULT_STRIKE,      // 서버 -> 클라이언트: 스트라이크 판정
    RESULT_HIT,         // 서버 -> 클라이언트: 안타 발생 (단타, 2루타 등 상세 정보 포함)
    RESULT_OUT,         // 서버 -> 클라이언트: 아웃 발생 (플라이 아웃, 땅볼 등)

    // 4. 게임 상태 동기화 및 흐름 제어 (Game Flow)
    STATE_UPDATE,       // 서버 -> 클라이언트: 현재 전광판 상태(점수, 주자, 아웃카운트) 동기화
    SWAP_TURN,          // 서버 -> 클라이언트: 공수 교대 알림
    INNING_OVER,        // 서버 -> 클라이언트: 이닝 종료 알림
    GAME_OVER           // 서버 -> 클라이언트: 최종 게임 종료 및 승패 알림
}