package com.hrstack.services;

import com.hrstack.dto.requestDto.RefreshTokenRequest;
import com.hrstack.dto.requestDto.RegisterUserRequest;
import com.hrstack.dto.responseDto.UserResponse;
import com.hrstack.enums.InviteStatus;
import com.hrstack.utils.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    void createUser(RegisterUserRequest registerUserRequest);
    PageResponse<UserResponse> getAllUsersByStatus(InviteStatus inviteStatus, int page, int size);
    PageResponse<UserResponse> getAllUsers(int page, int size);
    void updateUser(String id, UpdateUserRequest request);
    void deleteUser(String id);
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(RefreshTokenRequest request);
    void changePassword(ChangePasswordRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(String token, ResetPasswordRequest request);
    void logout(HttpServletRequest request);
    void editProfile(EditProfileRequest editProfileRequest);
    void uploadProfilePicture(MultipartFile file);

}
