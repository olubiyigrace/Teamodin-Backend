package com.hrstack.services;

import com.hrstack.dto.RegisterUserRequest;
import com.hrstack.dto.requestDto.RefreshTokenRequest;
import com.hrstack.utils.LoginRequest;
import com.hrstack.utils.LoginResponse;

public interface UserService {
    void create(final RegisterUserRequest request);
    void resendVerificationOtp(final String email);
    LoginResponse login(final LoginRequest request);
    LoginResponse refreshToken(final RefreshTokenRequest request);
}
