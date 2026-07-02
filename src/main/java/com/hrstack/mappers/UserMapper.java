package com.hrstack.mappers;

import com.hrstack.dto.requestDto.RegisterUserRequest;
import com.hrstack.entities.User;
import com.hrstack.enums.Role;
import com.hrstack.properties.WorkspaceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final WorkspaceProperties workspaceProperties;

    public User toEntity(RegisterUserRequest request) {
        return User.builder()
                .companyName(request.getCompanyName())
                .workspaceUrl(workspaceProperties.getBaseUrl() + request.getWorkspaceUrl())
                .email(request.getEmail())
                .adminName(request.getAdminName())
                .role(Role.ADMIN)
                .build();
    }
}
