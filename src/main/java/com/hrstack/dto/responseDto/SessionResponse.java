package com.hrstack.dto.responseDto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SessionResponse {
    private String sessionId;
    private String device;
    private String ipAddress;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean currentSession;
}
