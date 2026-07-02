package com.hrstack.services;

import com.hrstack.dto.requestDto.RegisterUserRequest;
import com.hrstack.dto.requestDto.RefreshTokenRequest;
import com.hrstack.utils.*;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {
    void create(RegisterUserRequest request);
    void resendVerificationOtp(String email);
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(RefreshTokenRequest request);
    void changePassword(ChangePasswordRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(String token, ResetPasswordRequest request);
    void logout(HttpServletRequest request);
    void update(UpdateUserProfileRequest registerUserRequest);
}
