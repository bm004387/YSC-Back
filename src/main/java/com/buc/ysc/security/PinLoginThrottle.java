package com.buc.ysc.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** PIN 로그인 시 사용자와 원격 주소 조합별 시도 횟수를 제한합니다. */
@Component
public class PinLoginThrottle {

    private static final int MAX_FAILURES = 10;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final String KEY_PREFIX = "auth:pin-fail:";

    private final StringRedisTemplate redisTemplate;

    public PinLoginThrottle(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isBlocked(String usrId, String remoteAddress) {
        String value = redisTemplate.opsForValue().get(key(usrId, remoteAddress));
        return value != null && Integer.parseInt(value) >= MAX_FAILURES;
    }

    public void recordFailure(String usrId, String remoteAddress) {
        String key = key(usrId, remoteAddress);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, WINDOW);
        }
    }

    public void clearFailures(String usrId, String remoteAddress) {
        redisTemplate.delete(key(usrId, remoteAddress));
    }

    private String key(String usrId, String remoteAddress) {
        try {
            String value = usrId + ":" + remoteAddress;
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return KEY_PREFIX + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", exception);
        }
    }
}
