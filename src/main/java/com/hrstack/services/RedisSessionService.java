package com.hrstack.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisSessionService {
    private final StringRedisTemplate redisTemplate;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private static final String SESSION_PREFIX = "session:";
    private static final String REFRESH_PREFIX = "refresh:";



    public void saveSession(String sessionId, String userId) {
        redisTemplate.opsForValue().set(
                SESSION_PREFIX + sessionId,
                userId,
                Duration.ofMillis(accessTokenExpiration));
    }

    public void saveRefreshSession(String sessionId, String userId) {
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + sessionId,
                userId,
                Duration.ofMillis(refreshTokenExpiration));
    }

    public boolean isSessionActive(String sessionId) {
        Boolean exists = redisTemplate.hasKey(SESSION_PREFIX + sessionId);
        return Boolean.TRUE.equals(exists);
    }

    public boolean isRefreshSessionActive(String sessionId) {
        Boolean exists = redisTemplate.hasKey(REFRESH_PREFIX + sessionId);
        return Boolean.TRUE.equals(exists);
    }

    public void deleteSession(String sessionId) {
        redisTemplate.delete(SESSION_PREFIX + sessionId);
    }

    public void deleteRefreshSession(String sessionId) {
        redisTemplate.delete(REFRESH_PREFIX + sessionId);
    }

    public void deleteAll(String sessionId) {
        redisTemplate.delete(SESSION_PREFIX + sessionId);
        redisTemplate.delete(REFRESH_PREFIX + sessionId);
    }

    public void extendSession(String sessionId, String userId) {
        saveSession(sessionId, userId);
    }
}
