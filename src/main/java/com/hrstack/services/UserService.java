package com.hrstack.services;

import com.hrstack.dto.RegisterUserRequest;
import com.hrstack.dto.requestDto.RefreshTokenRequest;
import com.hrstack.utils.*;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {
    void create(final RegisterUserRequest request);
    void resendVerificationOtp(final String email);
    LoginResponse login(final LoginRequest request);
    LoginResponse refreshToken(final RefreshTokenRequest request);
    void changePassword(ChangePasswordRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(String token, ResetPasswordRequest request);
    void logout(HttpServletRequest request);
}
