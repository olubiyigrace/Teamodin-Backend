package com.hrstack.mappers;

import com.hrstack.dto.requestDto.RegisterUserRequest;
import com.hrstack.dto.responseDto.UserResponse;
import com.hrstack.entities.User;
import com.hrstack.enums.InviteStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final PasswordEncoder passwordEncoder;

    public User toEntity(RegisterUserRequest request){
        return User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .status(InviteStatus.PENDING)
                .reportsTo(request.getReportsTo())
                .jobTitle(request.getJobTitle())
                .department(request.getDepartment())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(request.getRole())
                .build();
    }

    public UserResponse toResponse(User user){
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .jobTitle(user.getJobTitle())
                .department(user.getDepartment())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
