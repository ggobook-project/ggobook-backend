package com.untitled.ggobook.component;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisLockManager {

    private final RedisTemplate<String, String> redisTemplate;

    // 🌟 상수 정의: 락 기본 연장 시간(3분), 최대 허용 점유 시간(30분)
    private static final long LOCK_EXTEND_MINUTES = 3;
    private static final long MAX_LOCK_MINUTES = 30;

    /**
     * 🌟 이어쓰기 권한(락) 획득 시도
     * @param novelId 소설 ID
     * @param userId 요청한 유저 ID
     * @return 락 획득 성공 시 true, 누군가 쓰고 있다면 false
     */
    public Boolean lock(Long novelId, Long userId) {
        String key = "lock:novel:" + novelId;

        String value = userId + ":" + System.currentTimeMillis();

        // setIfAbsent: 키가 없을 때만 데이터를 저장 (동시성 방어)
        // 강제 종료(좀비 락)를 대비해 초기 수명은 3분으로 짧게 설정합니다.
        return redisTemplate.opsForValue()
                .setIfAbsent(key, value, Duration.ofMinutes(LOCK_EXTEND_MINUTES));
    }

    /**
     * 🌟 락 수명 연장 (하트비트) - 새로 추가된 로직
     * 프론트엔드에서 1분마다 호출하여 락이 풀리지 않게 3분씩 연장합니다.
     */
    public Boolean extendLock(Long novelId, Long userId) {
        String key = "lock:novel:" + novelId;
        String lockValue = redisTemplate.opsForValue().get(key);

        if (lockValue != null) {
            // 저장된 값 분리 -> parts[0]: 유저ID, parts[1]: 최초 획득 시간
            String[] parts = lockValue.split(":");
            String lockedUserId = parts[0];
            long lockedTime = Long.parseLong(parts[1]);

            // 1. 현재 락을 가진 사람이 연장을 요청한 유저(본인)인지 확인
            if (lockedUserId.equals(String.valueOf(userId))) {

                // 2. 현재 시간이 최초 획득 시간으로부터 30분이 지났는지 검사
                long currentTime = System.currentTimeMillis();
                long elapsedMinutes = (currentTime - lockedTime) / (60 * 1000);

                if (elapsedMinutes >= MAX_LOCK_MINUTES) {
                    // 최대 허용 시간(30분)을 초과했으므로 연장을 거부하고 락을 강제 회수(삭제)
                    redisTemplate.delete(key);
                    return false;
                }

                // 30분 이내라면 락 수명을 다시 3분으로 리셋하여 생존 연장
                return redisTemplate.expire(key, Duration.ofMinutes(LOCK_EXTEND_MINUTES));
            }
        }
        return false;
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