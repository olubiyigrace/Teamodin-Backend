package com.hrstack.entities;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession implements Serializable {
    private String sessionId;
    private String userId;
    private String companyId;
    private String role;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}