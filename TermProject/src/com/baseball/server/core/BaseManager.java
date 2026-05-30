package com.baseball.server.core;

import com.baseball.common.model.GameState;

public class BaseManager {

    public void processHit(GameState state, int advanceBases) {
        boolean[] bases = state.getBases();
        int scoreGenerated = 0;

        // 1. 기존 루상 주자 이동
        for (int i = 2; i >= 0; i--) {
            if (bases[i]) {
                int nextBase = i + advanceBases;
                if (nextBase >= 3) {
                    scoreGenerated++;
                } else {
                    bases[nextBase] = true;
                }
                bases[i] = false;
            }
        }

        // 2. 타자 주자 배치
        if (advanceBases >= 4) {
            // 홈런 - 타자도 홈인
            scoreGenerated++;
        } else {
            bases[advanceBases - 1] = true;
        }

        // 3. 점수 반영 및 메시지
        if (scoreGenerated > 0) {
            if (state.isTop()) {
                state.addAwayScore(scoreGenerated);
            } else {
                state.addHomeScore(scoreGenerated);
            }

            if (advanceBases >= 4) {
                state.setLastMessage("홈런! 🎉 " + scoreGenerated + "득점! 타구가 담장을 넘어갑니다!");
            } else {
                state.setLastMessage(advanceBases + "루타 작렬! " + scoreGenerated + "득점에 성공합니다!");
            }
        } else {
            if (advanceBases >= 4) {
                state.setLastMessage("홈런! 🎉 타구가 담장을 넘어갑니다!");
            } else {
                state.setLastMessage(advanceBases + "루타를 쳤습니다! 주자 진루합니다.");
            }
        }

        // 4. 볼 카운트 초기화
        state.resetBattingCount();
    }
}