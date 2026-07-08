package com.hrstack.services.serviceImpl;

import com.hrstack.dto.requestDto.*;
import com.hrstack.dto.responseDto.*;
import com.hrstack.entities.Company;
import com.hrstack.entities.User;
import com.hrstack.entities.UserSession;
import com.hrstack.enums.InviteStatus;
import com.hrstack.enums.OtpPurpose;
import com.hrstack.enums.Role;
import com.hrstack.enums.UserProfileStatus;
import com.hrstack.exceptions.DuplicateResourceException;
import com.hrstack.exceptions.InvalidRequestException;
import com.hrstack.exceptions.UnauthorizedException;
import com.hrstack.mappers.UserMapper;
import com.hrstack.orders.OrderProducer;
import com.hrstack.orders.ProducerMessage;
import com.hrstack.properties.JwtProperties;
import com.hrstack.repositories.UserRepository;
import com.hrstack.security.JwtService;
import com.hrstack.services.*;
import com.hrstack.utils.*;
import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final OrderProducer orderProducer;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CurrentUserUtil currentUserUtil;
    private final RedisSessionService redisSessionService;
    private final CloudinaryService cloudinaryService;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final IpAddressUtil ipAddressUtil;
    private final UserAgentUtil userAgentUtil;
    private final JwtProperties jwtProperties;



    @Override
    public void createUser(RegisterUserRequest request) {
        User loggedInUser = currentUserUtil.getLoggedInUser();
        if(loggedInUser.getFirstName().isBlank() && loggedInUser.getLastName().isBlank()){
            throw new UnauthorizedException("Edit your profile to have a name as the admin");
        }
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            log.debug("User with the email '{}' already exists.", request.getEmail());
            throw new DuplicateResourceException("Workspace user already exists");
        }
        if (request.getRole() == Role.ADMIN) {
            throw new InvalidRequestException(" Admin role cannot be selected");
        }
        User newUser = userMapper.toEntity(request);
        newUser.setCompany(Company.builder().id(loggedInUser.getCompanyId()).build());
        newUser.setStatus(InviteStatus.PENDING);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(newUser);

        String inviteToken = jwtService.generateWorkspaceInviteToken(newUser.getId(), newUser.getEmail(), loggedInUser.getCompanyId());
        newUser.setInviteToken(inviteToken);
        newUser.setExpiresAt(LocalDateTime.now().plusDays(7));
        userRepository.save(newUser);

        Map<String, Object> model = new HashMap<>();
        model.put("name", request.getFirstName() + " " + request.getLastName());
        model.put("adminName", loggedInUser.getFirstName() + " " + loggedInUser.getLastName());
        model.put("companyName", loggedInUser.getCompany().getCompanyName());
        model.put("role", newUser.getRole());
        model.put("password", request.getPassword());
        model.put("inviteLink", "https://app.hrstack.com/api/v1/accept-invite?token=" + inviteToken);

        try {
            emailService.sendVerificationEmail(
                    request.getEmail(),
                    loggedInUser.getCompany().getCompanyName() + " Workspace Invite",
                    "workspaceInvite",
                    model
            );
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to send invite to " + request.getEmail(), e);
        }
    }

    @Override
    public void resendInvite(String id, ResendInviteRequest request){
        User loggedInUser = currentUserUtil.getLoggedInUser();

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("User does not exist"));
        if(!existingUser.getStatus().equals(InviteStatus.DECLINED)){
            throw  new InvalidRequestException("Invite can only be resent to users with a declined invite status");
        }
        String inviteToken = jwtService.generateWorkspaceInviteToken(existingUser.getId(), existingUser.getEmail(), loggedInUser.getCompanyId());
        existingUser.setInviteToken(inviteToken);
        existingUser.setExpiresAt(LocalDateTime.now().plusDays(7));
        existingUser.setStatus(InviteStatus.PENDING);
        existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(existingUser);

        Map<String, Object> model = new HashMap<>();
        model.put("name", existingUser.getFirstName() + " " + existingUser.getLastName());
        model.put("adminName", loggedInUser.getFirstName() + " " + loggedInUser.getLastName());
        model.put("companyName", loggedInUser.getCompany().getCompanyName());
        model.put("role", existingUser.getRole());
        model.put("password", request.getPassword());
        model.put("inviteLink", "https://app.hrstack.com/api/v1/accept-invite?token=" + inviteToken);

        try {
            emailService.sendVerificationEmail(
                    existingUser.getEmail(),
                    loggedInUser.getCompany().getCompanyName() + " Workspace Invite",
                    "workspaceInvite",
                    model
            );
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to send invite to " + existingUser.getEmail(), e);
        }
    }

    @Override
    public PageResponse<UserResponse> getAllUsersByStatus(InviteStatus inviteStatus, int page, int size) {
        User loggedInUser = currentUserUtil.getLoggedInUser();
        if (loggedInUser.getCompanyId() == null) {
            throw new UnauthorizedException("User is not linked to any company");
        }
        if (inviteStatus == null) {
            throw new InvalidRequestException("Invite status is required");
        }
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<User> users = userRepository.findByCompanyAndStatus(loggedInUser.getCompany(), inviteStatus, pageRequest);
        return PageResponse.of(users.map(userMapper::toResponse));
    }

    @Override
    public PageResponse<UserResponse> getAllUsers(int page, int size) {
        User loggenInuser = currentUserUtil.getLoggedInUser();
        final PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        final Page<User> loanApplications = userRepository.findByCompany(loggenInuser.getCompany(), pageRequest);
        final Page<UserResponse> loanApplicationResponses = loanApplications.map(userMapper::toResponse);
        return PageResponse.of(loanApplicationResponses);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void updateUser(String id, UpdateUserRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("User with the id " + id + " does not exist."));
        existingUser.setDepartment(request.getDepartment());
        existingUser.setReportsTo(request.getReportsTo());
        existingUser.setJobTitle(request.getJobTitle());
        existingUser.setRole(request.getRole());
        existingUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(existingUser);
    }

    @Override
    public User validateWorkspaceInvite(String token, Claims claims) {
        User user = userRepository.findByInviteToken(token)
                .orElseThrow(() -> new InvalidRequestException("Invitation not found."));
        if (user.getStatus() == InviteStatus.ACCEPTED) {
            throw new InvalidRequestException("Invitation has already been accepted.");
        }
        if (user.getStatus() == InviteStatus.DECLINED) {
            throw new InvalidRequestException("Invitation has already been declined.");
        }
        if (LocalDateTime.now().isAfter(user.getExpiresAt())) {
            user.setStatus(InviteStatus.DECLINED);
            user.setInviteToken(null);
            user.setExpiresAt(null);
            userRepository.save(user);
            throw new InvalidRequestException("Invitation has expired.");
        }

        String invitedUserId = claims.get("userId", String.class);
        String invitedCompanyId = claims.get("companyId", String.class);
        String invitedEmail = claims.getSubject();
        if (!user.getId().equals(invitedUserId)) {
            throw new InvalidRequestException("Invalid invitation.");
        }
        if (!user.getCompanyId().equals(invitedCompanyId)) {
            throw new InvalidRequestException("Invalid company invitation.");
        }
        if (!user.getEmail().equalsIgnoreCase(invitedEmail)) {
            throw new InvalidRequestException("This invitation belongs to another email.");
        }
        return user;
    }

    @Override
    public LoginResponse invitedUserLogin(InvitedUserLoginRequest request, HttpServletRequest httpServletRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User authenticatedUser = (User) authentication.getPrincipal();
        Claims claims = jwtService.validateWorkspaceInviteToken(request.getInviteToken());

        User invitedUser = validateWorkspaceInvite(request.getInviteToken(), claims);
        if (!authenticatedUser.getId().equals(invitedUser.getId())) {
            throw new InvalidRequestException("This invitation does not belong to this account.");
        }
        invitedUser.setStatus(InviteStatus.ACCEPTED);
        invitedUser.setUserProfileStatus(UserProfileStatus.ACTIVE);
        invitedUser.setInviteToken(null);
        invitedUser.setExpiresAt(null);
        userRepository.save(invitedUser);

        String sessionId = UUID.randomUUID().toString();
        UserSession session = UserSession.builder()
                .sessionId(sessionId)
                .userId(invitedUser.getId())
                .companyId(invitedUser.getCompanyId())
                .role(invitedUser.getRole().name())
                .ipAddress(ipAddressUtil.getClientIp(httpServletRequest))
                .userAgent(userAgentUtil.getUserAgent(httpServletRequest))
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtProperties.getRefreshTokenExpiration())))
                .build();
        redisSessionService.saveSession(session);
        redisSessionService.saveRefreshSession(session);

        String accessToken = jwtService.generateAccessToken(
                authenticatedUser.getCompanyId(),
                authenticatedUser.getId(),
                authenticatedUser.getRole().name(),
                sessionId
        );
        String refreshToken = jwtService.generateRefreshToken(
                authenticatedUser.getCompanyId(),
                authenticatedUser.getId(),
                authenticatedUser.getRole().name(),
                sessionId
        );
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest httpServletRequest) {
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                ));
        User user = (User) authentication.getPrincipal();
        if (user.getRole().equals(Role.ADMIN) && user.getCompany().getIsVerified().equals(false)) {
            throw new UnauthorizedException("Verify your email to continue");
        }
        if (!user.getRole().equals(Role.ADMIN) && !user.getUserProfileStatus().equals(UserProfileStatus.ACTIVE)) {
            throw new UnauthorizedException("Only an active user can login");
        }

        String sessionId = UUID.randomUUID().toString();
        UserSession session = UserSession.builder()
                .sessionId(sessionId)
                .userId(user.getId())
                .companyId(user.getCompanyId())
                .role(user.getRole().name())
                .ipAddress(ipAddressUtil.getClientIp(httpServletRequest))
                .userAgent(userAgentUtil.getUserAgent(httpServletRequest))
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtProperties.getRefreshTokenExpiration())))
                .build();
        redisSessionService.saveSession(session);
        redisSessionService.saveRefreshSession(session);

        Map<String, Object> model = new HashMap<>();
        model.put("firstName", user.getFirstName());
        model.put("loginTime", session.getCreatedAt());
        model.put("device", session.getUserAgent());
        model.put("ipAddress", session.getIpAddress());
        model.put("securityUrl", "https://app.hrstack.com/settings/security");
        model.put("changePasswordUrl", "https://app.hrstack.com/settings/security/password");

        try {
            emailService.sendVerificationEmail(
                    request.getEmail(),
                    "New Login Alert!",
                    "login",
                    model);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        String accessToken = jwtService.generateAccessToken(
                user.getCompanyId(),
                user.getId(),
                user.getRole().name(),
                sessionId
        );
        String refreshToken = jwtService.generateRefreshToken(
                user.getCompanyId(),
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

        jwtService.validateToken(refreshToken);
        String sessionId = jwtService.getSessionId(refreshToken);
        UserSession session = redisSessionService.getRefreshSession(sessionId);
        if (session == null) {
            throw new InvalidRequestException("Refresh session expired");
        }

        String tokenUserId = jwtService.getUserIdFromRefreshToken(refreshToken);
        if (!session.getUserId().equals(tokenUserId)) {
            throw new UnauthorizedException("Invalid refresh session");
        }

        User user = userRepository.findById(tokenUserId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtProperties.getRefreshTokenExpiration())));
        redisSessionService.saveSession(session);
        redisSessionService.saveRefreshSession(session);

        String newAccessToken = jwtService.generateAccessToken(
                user.getCompanyId(),
                user.getId(),
                user.getRole().name(),
                sessionId);
        String newRefreshToken = jwtService.generateRefreshToken(
                user.getCompanyId(),
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
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if(existingUser.isEmpty()){
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
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isEmpty()) {
            throw new InvalidRequestException("User not found");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("Passwords do not match");
        }
        User user = existingUser.get();
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
        redisSessionService.revokeSession(sessionId);
    }

    @Override
    public List<SessionResponse> getActiveSessions(HttpServletRequest request) {
        User currentUser = currentUserUtil.getLoggedInUser();
        String token = request.getHeader(HttpHeaders.AUTHORIZATION)
                .substring(7);
        String currentSessionId = jwtService.getSessionId(token);
        List<UserSession> sessions =
                redisSessionService.getUserSessions(currentUser.getId());
        return sessions.stream()
                .map(session -> SessionResponse.builder()
                        .sessionId(session.getSessionId())
                        .device(session.getUserAgent())
                        .ipAddress(session.getIpAddress())
                        .createdAt(session.getCreatedAt())
                        .expiresAt(session.getExpiresAt())
                        .currentSession(session.getSessionId().equals(currentSessionId))
                        .build())
                .toList();
    }

    @Override
    public void logoutAllDevices() {
        User user = currentUserUtil.getLoggedInUser();
        redisSessionService.revokeAllSessions(user.getId());
    }

    @Override
    public void revokeSession(String sessionId) {
        User currentUser = currentUserUtil.getLoggedInUser();
        UserSession session = redisSessionService.getSession(sessionId);
        if (session == null) {
            throw new InvalidRequestException("Session not found");
        }
        if (!session.getUserId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Access denied");
        }
        redisSessionService.revokeSession(sessionId);
    }

    @Override
    public void editProfile(EditProfileRequest request) {
        User user = currentUserUtil.getLoggedInUser();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEditedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void uploadProfilePicture(MultipartFile file) {
        User user = currentUserUtil.getLoggedInUser();
        if (file.isEmpty()) {
            throw new InvalidRequestException("Please select an image.");
        }
        if (user.getImagePublicId() != null) {
            cloudinaryService.delete(user.getImagePublicId());
        }

        List<String> allowedTypes = List.of(
                "image/jpeg",
                "image/png",
                "image/jpg"
        );
        if (!allowedTypes.contains(file.getContentType())) {
            throw new InvalidRequestException("Only JPG and PNG images are allowed.");
        }
        if (file.getSize() > 3 * 1024 * 1024) {
            throw new InvalidRequestException("Image size must not exceed 3MB.");
        }
        ImageUploadResponse response = cloudinaryService.upload(file);
        user.setImageUrl(response.getImageUrl());
        user.setImagePublicId(response.getPublicId());
        userRepository.save(user);
    }

    @Override
    public void deactivateUser(String id) {
    }

    @Override
    public void suspendUser(String id) {
    }
}


