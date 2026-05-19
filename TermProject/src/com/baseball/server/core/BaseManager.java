package com.baseball.server.core;

import com.baseball.common.model.GameState;

public class BaseManager {

    /**
     * 안타가 발생했을 때 주자들을 이동시키고 발생한 점수를 반환(또는 GameState에 반영)합니다.
     * @param state 현재 게임 상태 객체
     * @param advanceBases 진루할 칸 수 (1:단타, 2:2루타, 3:3루타, 4:홈런)
     */
    public void processHit(GameState state, int advanceBases) {
        boolean[] bases = state.getBases(); // 현재 주자 상황 (0: 1루, 1: 2루, 2: 3루)
        int scoreGenerated = 0;             // 이번 타석에서 발생한 점수

        // 1. 기존 루상에 있는 주자들을 먼저 이동시킵니다. (3루 주자부터 역순으로 계산)
        for (int i = 2; i >= 0; i--) {
            if (bases[i]) { // 해당 루에 주자가 있다면
                int nextBase = i + advanceBases; // 이동 후의 위치

                if (nextBase >= 3) {
                    // 3루(인덱스 2)를 넘어가면 홈인! (득점)
                    scoreGenerated++;
                } else {
                    // 아직 루상에 남아있다면 새로운 위치로 이동
                    bases[nextBase] = true;
                }
                
                // 원래 있던 자리는 일단 비웁니다.
                bases[i] = false; 
            }
        }

        // 2. 공을 친 '타자 주자'를 루에 배치합니다.
        if (advanceBases >= 4) {
            // 홈런인 경우 타자 본인도 홈인!
            scoreGenerated++;
        } else {
            // 홈런이 아니면 타자는 안타 종류에 맞는 루(인덱스: advanceBases - 1)에 안착합니다.
            bases[advanceBases - 1] = true;
        }

        // 3. 발생한 점수를 현재 공격 팀(초/말)에 더해줍니다.
        if (scoreGenerated > 0) {
            if (state.isTop()) {
                state.addAwayScore(scoreGenerated);
            } else {
                state.addHomeScore(scoreGenerated);
            }
            state.setLastMessage(advanceBases + "루타 작렬! " + scoreGenerated + "득점에 성공합니다!");
        } else {
            state.setLastMessage(advanceBases + "루타를 쳤습니다! 주자 진루합니다.");
        }

        // 4. 타격이 완료되었으므로 볼 카운트(볼, 스트라이크)를 초기화합니다.
        state.resetBattingCount();
    }
}