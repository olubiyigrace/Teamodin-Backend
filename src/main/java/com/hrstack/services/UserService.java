package com.hrstack.services;

import com.hrstack.dto.requestDto.RefreshTokenRequest;
import com.hrstack.dto.requestDto.RegisterUserRequest;
import com.hrstack.dto.responseDto.UserResponse;
import com.hrstack.utils.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    void createUser(RegisterUserRequest registerUserRequest);
    List<UserResponse> getAllUser();
    void updateUser(String id, RegisterUserRequest registerUserRequest);
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
