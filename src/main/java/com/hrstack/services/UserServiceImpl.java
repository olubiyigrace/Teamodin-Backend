package com.hrstack.services;

import com.hrstack.dto.RegisterUserRequest;
import com.hrstack.dto.requestDto.RefreshTokenRequest;
import com.hrstack.entities.User;
import com.hrstack.enums.OtpPurpose;
import com.hrstack.exceptions.DuplicateResourceException;
import com.hrstack.exceptions.InvalidRequestException;
import com.hrstack.dto.requestDto.OtpRequest;
import com.hrstack.mappers.UserMapper;
import com.hrstack.orders.OrderProducer;
import com.hrstack.orders.ProducerMessage;
import com.hrstack.repositories.UserRepository;
import com.hrstack.security.JwtService;
import com.hrstack.utils.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final OrderProducer orderProducer;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CurrentUserUtil currentUserUtil;
    private final RedisSessionService redisSessionService;


    @Override
    public void create(RegisterUserRequest request) {
        Optional<User> existingUser = userRepository.findByWorkspaceUrl(request.getWorkspaceUrl());
        if(existingUser.isPresent()){
            throw new DuplicateResourceException("Workspace already exists");
        }
        if (!request.getPassword().equals(request.getReEnterPassword())){
            throw new InvalidRequestException("Passwords do not match");
        }
        User newUser = userMapper.toEntity(request);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(newUser);

        String otp = otpService.createOtp(
                OtpRequest.builder()
                        .email(request.getEmail())
                        .purpose(OtpPurpose.VERIFY_ACCOUNT)
                        .build()
        );
        orderProducer.sendMessage(
                ProducerMessage.builder()
                        .email(request.getEmail())
                        .otp(otp)
                        .purpose(OtpPurpose.VERIFY_ACCOUNT)
                        .build()
        );
    }

    @Override
    public void resendVerificationOtp(String email) {
        String otp = otpService.resendOtp(
                        OtpRequest.builder()
                                .email(email)
                                .purpose(OtpPurpose.VERIFY_ACCOUNT)
                                .build()
                );
        orderProducer.sendMessage(
                ProducerMessage.builder()
                        .email(email)
                        .otp(otp)
                        .purpose(OtpPurpose.VERIFY_ACCOUNT)
                        .build()
        );
    }

    @Override
    public LoginResponse login(final LoginRequest request) {
        final Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        final User user = (User) authentication.getPrincipal();
        if (!Boolean.TRUE.equals(user.getIsVerified())) {
            throw new InvalidRequestException("User not verified");
        }
        String sessionId = UUID.randomUUID().toString();
        redisSessionService.saveSession(
                sessionId,
                user.getId()
        );
        redisSessionService.saveRefreshSession(
                sessionId,
                user.getId()
        );
        String accessToken = jwtService.generateAccessToken(
                user.getWorkspaceUrl(),
                user.getId(),
                user.getRole().name(),
                sessionId
                );
        String refreshToken = jwtService.generateRefreshToken(
                user.getWorkspaceUrl(),
                user.getId(),
                user.getRole().name(),
                sessionId);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new InvalidRequestException("Invalid refresh token");
        }

        String sessionId = jwtService.getSessionId(refreshToken);
        if (!redisSessionService.isRefreshSessionActive(sessionId)) {
            throw new InvalidRequestException("Refresh session expired");
        }

        String userId = jwtService.getUserIdFromRefreshToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        redisSessionService.saveSession(sessionId, user.getId());
        redisSessionService.saveRefreshSession(sessionId, user.getId());

        String newAccessToken = jwtService.generateAccessToken(
                user.getWorkspaceUrl(),
                user.getId(),
                user.getRole().name(),
                sessionId);
        String newRefreshToken = jwtService.generateRefreshToken(
                user.getWorkspaceUrl(),
                user.getId(),
                user.getRole().name(),
                sessionId);
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .build();
    }


    @Override
    public void changePassword(ChangePasswordRequest request) {
        User loggedInUser = currentUserUtil.getLoggedInUser();

        boolean matches = passwordEncoder.matches(request.getOldPassword(), loggedInUser.getPassword());
        if (!matches) throw new InvalidRequestException("Old password is incorrect");

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("Passwords do not match");
        }
        if (passwordEncoder.matches(request.getNewPassword(), loggedInUser.getPassword())) {
            throw new InvalidRequestException("Cannot reuse old password");
        }
        loggedInUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(loggedInUser);
        log.info("password changed successfully");
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User existingUser = userRepository.findByEmail(request.getEmail());
        if(existingUser == null){
            throw new InvalidRequestException(request.getEmail() + " not found.");
        }
        String otp = otpService.createOtp(
                OtpRequest.builder()
                        .email(request.getEmail())
                        .purpose(OtpPurpose.RESET_PASSWORD)
                        .build()
        );
        orderProducer.sendMessage(
                ProducerMessage.builder()
                        .email(request.getEmail())
                        .otp(otp)
                        .purpose(OtpPurpose.RESET_PASSWORD)
                        .build()
        );
        log.info("Reset link sent");
    }

    @Override
    public void resetPassword(String resetToken, ResetPasswordRequest request) {
        if(!jwtService.validatePasswordResetToken(resetToken)){
            throw  new InvalidRequestException("Invalid token");
        }
        String email = jwtService.getEmailFromResetToken(resetToken);
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new InvalidRequestException("User not found");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("Passwords do not match");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void logout(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidRequestException("Invalid authorization header");
        }

        String token = authHeader.substring(7);
        jwtService.validateToken(token);
        if (!jwtService.isAccessToken(token)) {
            throw new InvalidRequestException("Invalid access token");
        }

        String sessionId = jwtService.getSessionId(token);
       redisSessionService.deleteAll(sessionId);
    }
}
