package com.hrstack.controllers;


import com.hrstack.dto.requestDto.RefreshTokenRequest;
import com.hrstack.dto.requestDto.RegisterCompanyRequest;
import com.hrstack.security.JwtService;
import com.hrstack.services.CompanyService;
import com.hrstack.services.OtpService;
import com.hrstack.dto.requestDto.OtpVerifyRequest;
import com.hrstack.services.UserService;
import com.hrstack.utils.*;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final OtpService otpService;
    private final CompanyService companyService;
    private final JwtService jwtService;

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterCompanyRequest request) {
        companyService.create(request);
        return ResponseEntity.ok(ApiResponse.success(true, "Registration successful. Check your email for the verification code.", null));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        otpService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success(true, "Verification successful", null));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<String>> resendOtp(@RequestParam String email) {
        companyService.resendVerificationOtp(email);
        return ResponseEntity.ok(ApiResponse.success(true, "OTP sent successfully.", null));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        final LoginResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success(true, "login successful", response));
    }

    @GetMapping("/accept-invite")
    public void acceptInvite(@RequestParam String token, HttpServletResponse response) throws IOException {
        Claims claims = jwtService.validateWorkspaceInviteToken(token);
        userService.validateWorkspaceInvite(token, claims);
        response.sendRedirect("/api/v1/invited-users-login?token=" + token);
    }

    @GetMapping("/invited-users-login")
    public ModelAndView invitedUsersLoginPage(@RequestParam String token) {
        ModelAndView mv = new ModelAndView("invited-users-login");
        mv.addObject("token", token);
        return mv;
    }

    @PostMapping("/invited-user-login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody InvitedUserLoginRequest request) {
        final LoginResponse response = userService.invitedUserLogin(request);
        return ResponseEntity.ok(ApiResponse.success(true, "login successful", response));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        final LoginResponse response = userService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(true, "Success!", response));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(true, "Password reset OTP sent successfully", null));
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<ResetOtpResponse>> verifyResetOtp(@Valid @RequestBody VerifyResetOtpRequest request) {
        String resetToken = otpService.verifyResetOtp(request);
        return ResponseEntity.ok(ApiResponse.success(true, "OTP verified successfully",
                ResetOtpResponse.builder().resetToken(resetToken).build()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword
            (@RequestHeader("Authorization") String authorizationHeader, @Valid @RequestBody ResetPasswordRequest request) {
        String resetToken = authorizationHeader.substring(7);
        userService.resetPassword(resetToken, request);
        return ResponseEntity.ok(ApiResponse.success(true, "Password reset successfully", null));
    }
}
