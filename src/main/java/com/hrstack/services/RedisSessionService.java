package com.hrstack.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrstack.entities.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisSessionService {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private static final String SESSION_PREFIX = "session:";
    private static final String USER_SESSIONS_PREFIX = "user-sessions:";
    private static final String REFRESH_PREFIX = "refresh:";


    public void saveSession(UserSession session) {
        try {
            redisTemplate.opsForValue().set(
                    SESSION_PREFIX + session.getSessionId(),
                    objectMapper.writeValueAsString(session),
                    Duration.ofMillis(accessTokenExpiration)
            );
            redisTemplate.opsForSet().add("user-sessions:" + session.getUserId(), session.getSessionId());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveRefreshSession(UserSession session) {
        try {
            redisTemplate.opsForValue().set(
                    REFRESH_PREFIX + session.getSessionId(),
                    objectMapper.writeValueAsString(session),
                    Duration.ofMillis(refreshTokenExpiration)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public UserSession getSession(String sessionId) {
        return get(SESSION_PREFIX + sessionId);

    }

    public UserSession getRefreshSession(String sessionId) {
        return get(REFRESH_PREFIX + sessionId);

    }

    public boolean isSessionActive(String sessionId) {
        return redisTemplate.hasKey(SESSION_PREFIX + sessionId);
    }

    public List<UserSession> getUserSessions(String userId) {
        Set<String> sessionIds = redisTemplate.opsForSet().members(USER_SESSIONS_PREFIX + userId);
        if (sessionIds == null || sessionIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<UserSession> sessions = new ArrayList<>();
        for (String sessionId : sessionIds) {
            UserSession session = getSession(sessionId);

            if (session != null) {
                sessions.add(session);
            }
        }
        return sessions;
    }

    public void revokeSession(String sessionId) {
        UserSession session = getSession(sessionId);
        if (session == null) {
            return;
        }
        redisTemplate.delete(SESSION_PREFIX + sessionId);
        redisTemplate.delete(REFRESH_PREFIX + sessionId);
        redisTemplate.opsForSet().remove(
                USER_SESSIONS_PREFIX + session.getUserId(),
                sessionId
        );
    }

    public void revokeAllSessions(String userId) {
        Set<String> sessionIds = redisTemplate.opsForSet().members(USER_SESSIONS_PREFIX + userId);
        if (sessionIds == null) {
            return;
        }
        for (String sessionId : sessionIds) {
            redisTemplate.delete(SESSION_PREFIX + sessionId);
            redisTemplate.delete(REFRESH_PREFIX + sessionId);
        }
        redisTemplate.delete(USER_SESSIONS_PREFIX + userId);
    }

    private UserSession get(String key) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, UserSession.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}