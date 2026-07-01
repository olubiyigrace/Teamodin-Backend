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
import com.hrstack.utils.LoginRequest;
import com.hrstack.utils.LoginResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        String accessToken = jwtService.generateAccessToken(
                user.getWorkspaceUrl(),
                user.getId(),
                user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(
                user.getWorkspaceUrl(),
                user.getId(),
                user.getRole().name());
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    @Override
    public LoginResponse refreshToken(final RefreshTokenRequest request) {
        final String refreshToken = request.getRefreshToken();
        if (!jwtService.isRefreshToken(refreshToken)) {
            log.debug("Invalid refresh token");
            throw new InvalidRequestException("Invalid refresh token");
        }
        jwtService.validateToken(refreshToken);
        String userId = jwtService.getUserIdFromRefreshToken(refreshToken);
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        final String newAccessToken = jwtService.generateAccessToken(
                user.getWorkspaceUrl(),
                user.getId(),
                user.getRole().name());
        final String newRefreshToken = jwtService.generateRefreshToken(
                user.getWorkspaceUrl(),
                user.getId(),
                user.getRole().name());
        return new LoginResponse(newAccessToken, newRefreshToken, "Bearer");
    }
//       User user = userRepository.findByEmail(request.getEmail());
////        if (user == null) {
////            throw new UsernameNotFoundException("User not found");
////        }
////
////        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
////            throw new InvalidRequestException("Invalid credentials");
////        }
////        if (!Boolean.TRUE.equals(user.getIsVerified())) {
////            throw new InvalidRequestException("User not verified");
////        }
//    @Override
//    public void logout(HttpServletRequest request) {
//        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            log.debug("Invalid authorization header");
//            throw new InvalidRequestException("Invalid authorization header");
//        }
//        String token = authHeader.substring(7);
//        UserSession userSession = userSessionRepository.findByAccessToken(token)
//                .orElseThrow(() -> new InvalidRequestException("Session already ended"));
//        Instant expiryDate = jwtService.extractExpiration(token).toInstant();
//
//        LogoutToken logoutToken = LogoutToken.builder()
//                .token(token)
//                .expiryDate(expiryDate)
//                .userSession(userSession)
//                .build();
//        logoutTokenRepository.save(logoutToken);
//
//        userSession.setRevoked(true);
//        userSession.setAccessToken("used");
//        userSessionRepository.save(userSession);
//    }
//
//
//    @Override
//    public void revokeSession(String accessToken) {
//        UserSession session = userSessionRepository.findByAccessToken(accessToken)
//                .orElseThrow(() -> new DuplicateResourceException("Session already ended"));
//        if (session.isRevoked()){
//            throw new UnauthorizedException("Session has been revoked");
//        }
//
//        Instant expiryDate = jwtService.extractExpiration(accessToken).toInstant();
//        LogoutToken logoutToken = LogoutToken.builder()
//                .token(accessToken)
//                .expiryDate(expiryDate)
//                .userSession(session)
//                .build();
//
//        session.setRevoked(true);
//        session.setAccessToken("used");
//        logoutTokenRepository.save(logoutToken);
//        userSessionRepository.save(session);
//    }
//
//    @Override
//    public void changePassword(ChangePasswordRequest request) {
//        User loggedInUser = currentUserUtil.getLoggedInUser();
//
//        boolean matches = passwordEncoder.matches(request.getOldPassword(), loggedInUser.getPassword());
//        if (!matches) throw new InvalidRequestException("Old password is incorrect");
//
//        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
//            throw new InvalidRequestException("Passwords do not match");
//        }
//        if (passwordEncoder.matches(request.getNewPassword(), loggedInUser.getPassword())) {
//            throw new InvalidRequestException("Cannot reuse old password");
//        }
//        loggedInUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
//        userRepository.save(loggedInUser);
//        log.info("password changed successfully");
//    }
//
//    @Override
//    public void forgotPassword(ForgotPasswordRequest request) throws MessagingException {
//        User user = userRepository.findByEmail(request.getEmail())
//                .orElseThrow(() -> new UsernameNotFoundException("Email not found"));
//
//        String token = UUID.randomUUID().toString();
//        user.setResetPasswordToken(passwordEncoder.encode(token));
//        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(10));
//        userRepository.save(user);
//
//        Map<String, Object> model = new HashMap<>();
//        model.put("name", user.getName());
//        model.put("resetUrl", "https://multitenantbanking.com/api/v1/auth/reset-password?token=" + token);
//
//        emailService.sendVerificationEmail(
//                user.getEmail(),
//                "Reset Password",
//                "forgotpassword",
//                model
//        );
//        log.info("Reset link sent");
//    }
}
