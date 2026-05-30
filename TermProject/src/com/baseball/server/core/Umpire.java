package com.baseball.server.core;

import com.baseball.common.model.PitchData;
import com.baseball.common.model.SwingData;
import com.baseball.common.protocol.GameMessage;
import com.baseball.common.protocol.MessageType;

import java.util.Random;

public class Umpire {
    private Random random;

    public Umpire() {
        this.random = new Random();
    }

    /**
     * 1. 타자가 지켜봤을 때 (Take)의 판정
     */
    public GameMessage judgeTake(PitchData pitch) {
        int chance = random.nextInt(100); // 0 ~ 99 난수 생성
        
        // 투구 속도가 150 이상이면 제구가 흔들려 볼 확률 증가
        int strikeZoneChance = (pitch.getSpeed() >= 150) ? 35 : 45;

        if (chance < strikeZoneChance) {
            return new GameMessage(MessageType.RESULT_STRIKE, "스트라이크! 타자가 꼼짝 못하고 당합니다.");
        } else {
            return new GameMessage(MessageType.RESULT_BALL, "볼! 공이 스트라이크 존을 벗어났습니다.");
        }
    }

    /**
     * 2. 타자가 스윙했을 때 (Swing)의 판정
     */
    public GameMessage judgeSwing(PitchData pitch, SwingData swing) {
    	 
        // 이스터에그: 구속 0 + 당겨치기(1) + 완벽한 타이밍(f) = 무조건 홈런!
        if (pitch.getSpeed() == 0 
            && swing.getHitDirection() == 1 
            && swing.getTiming() == 'f') {
            return new GameMessage(MessageType.RESULT_HIT, 4);
        }
    	int roll = random.nextInt(100);

        // [1단계] 컨택(Contact) 계산 - 기본 컨택률 75%에서 구속에 따라 차감
        int contactChance = 75 - ((pitch.getSpeed() - 130) / 2);

        // 수싸움 보정: 투수의 구질과 타자의 타이밍(f/s)이 일치하면 컨택률 +15% 상승
        if (pitch.getPitchType() == swing.getTiming()) {
            contactChance += 15;
        }

        // 헛스윙 판정
        if (roll > contactChance) {
            return new GameMessage(MessageType.RESULT_STRIKE, "헛스윙! 투수의 구위에 방망이가 밀립니다.");
        }

        // [2단계] 타격 성공 - 안타 or 아웃 판정 (기본 타율 3할)
        int hitRoll = random.nextInt(100);
        if (hitRoll < 30) {
            
            // [3단계] 안타 종류 판정 (단타, 2루타, 3루타, 홈런)
            int baseRoll = random.nextInt(100);
            int advanceBases; // 진루 칸 수

            if (baseRoll < 60) {
                advanceBases = 1; // 60% 확률로 1루타
            } else if (baseRoll < 85) {
                advanceBases = 2; // 25% 확률로 2루타
            } else if (baseRoll < 95) {
                advanceBases = 3; // 10% 확률로 3루타
            } else {
                advanceBases = 4; // 5% 확률로 홈런
            }
            
            // 안타는 진루 로직을 위해 '정수(Integer)' 데이터를 클라이언트로 넘깁니다.
            return new GameMessage(MessageType.RESULT_HIT, advanceBases);
            
        } else {
            return new GameMessage(MessageType.RESULT_OUT, "아웃! 빗맞은 타구가 야수 정면으로 향합니다.");
        }
    }
}