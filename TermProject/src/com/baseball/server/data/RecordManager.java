package com.baseball.server.data;

import com.baseball.common.model.GameState;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RecordManager {

    /**
     * 파라미터로 받은 username을 기반으로 개인별 텍스트 파일에 기록을 저장합니다.
     */
    public static void savePersonalRecord(String username, GameState state) {
        // 비정상적인 접근 방어
        if (username == null || username.isEmpty()) {
            username = "비회원"; 
        }

        // 저장할 상위 폴더(data) 지정 및 자동 생성 로직
        String directoryPath = "data";
        File dir = new File(directoryPath);
        if (!dir.exists()) {
            dir.mkdirs(); // data 폴더가 없으면 자동으로 생성해 줍니다.
            System.out.println("[시스템] '" + directoryPath + "' 폴더가 없어서 새로 생성했습니다.");
        }

        // 유저별로 파일 이름을 다르게 생성 (예: data/Player1_history.txt)
        String filePath = directoryPath + "/" + username + "_history.txt";

        try (PrintWriter out = new PrintWriter(new FileWriter(filePath, true))) {
            
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            String winnerInfo;
            if (state.getAwayScore() > state.getHomeScore()) {
                winnerInfo = "원정팀 승리!";
            } else if (state.getAwayScore() < state.getHomeScore()) {
                winnerInfo = "홈팀 승리!";
            } else {
                winnerInfo = "무승부";
            }

            // 파일에 기록할 텍스트 (누구의 기록인지 명시)
            String recordLine = String.format("[%s] 님 플레이 기록 | %s | %d회 종료 | 원정 %d : %d 홈 | 결과: %s", 
                    username, now, state.getInning(), state.getAwayScore(), state.getHomeScore(), winnerInfo);
            
            out.println(recordLine);
            System.out.println("[DB] " + username + " 님의 개인 전적이 저장되었습니다.");
            
        } catch (IOException e) {
            System.out.println("[DB 오류] " + username + " 님의 전적 저장 실패.");
            e.printStackTrace();
        }
    }
}