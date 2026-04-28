package com.untitled.ggobook.component;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisLockManager {

    // 우리가 설정한 Upstash와 자동으로 연결되는 템플릿입니다.
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 🌟 이어쓰기 권한(락) 획득 시도
     * @param novelId 소설 ID
     * @param userId 요청한 유저 ID
     * @return 락 획득 성공 시 true, 누군가 쓰고 있다면 false
     */
    public Boolean lock(Long novelId, Long userId) {
        String key = "lock:novel:" + novelId; // 예: lock:novel:1

        // setIfAbsent: 키가 없을 때만 데이터를 저장합니다. (동시성 방어의 핵심!)
        // Duration.ofMinutes(10): 유저가 도망갈 것을 대비해 10분 뒤 자동 소멸시킵니다.
        return redisTemplate.opsForValue()
                .setIfAbsent(key, String.valueOf(userId), Duration.ofMinutes(20));
    }

    /**
     * 🌟 락 해제 (작성 완료 또는 취소 시)
     * @param novelId 소설 ID
     */
    public void unlock(Long novelId) {
        String key = "lock:novel:" + novelId;
        redisTemplate.delete(key); // 열쇠 반납 (키 삭제)
    }
}