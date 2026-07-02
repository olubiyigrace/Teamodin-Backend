package com.hrstack.mappers;

import com.hrstack.dto.RegisterUserRequest;
import com.hrstack.entities.User;
import com.hrstack.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    public User toEntity(RegisterUserRequest request){
        return User.builder()
                .companyName(request.getCompanyName())
                .workspaceUrl(request.getWorkspaceUrl())
                .email(request.getEmail())
                .role(Role.ADMIN)
                .build();
    }
}
