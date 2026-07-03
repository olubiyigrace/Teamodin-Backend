package com.hrstack.services;

import com.hrstack.dto.requestDto.RefreshTokenRequest;
import com.hrstack.dto.requestDto.RegisterUserRequest;
import com.hrstack.dto.responseDto.UserResponse;
import com.hrstack.entities.Company;
import com.hrstack.entities.User;
import com.hrstack.enums.InviteStatus;
import com.hrstack.enums.OtpPurpose;
import com.hrstack.enums.Role;
import com.hrstack.exceptions.DuplicateResourceException;
import com.hrstack.exceptions.InvalidRequestException;
import com.hrstack.dto.requestDto.OtpRequest;
import com.hrstack.exceptions.UnauthorizedException;
import com.hrstack.mappers.UserMapper;
import com.hrstack.orders.OrderProducer;
import com.hrstack.orders.ProducerMessage;
import com.hrstack.repositories.UserRepository;
import com.hrstack.security.JwtService;
import com.hrstack.utils.*;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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


    @Override
    public void createUser(RegisterUserRequest request) {
        User loggedInUser = currentUserUtil.getLoggedInUser();
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
            userRepository.save(newUser);

            Map<String, Object> model = new HashMap<>();
            model.put("name", request.getFirstName() + " " + request.getLastName());
            model.put("adminName", loggedInUser.getFirstName() + " " + loggedInUser.getLastName());
            model.put("companyName", loggedInUser.getCompany().getCompanyName());
            model.put("role", newUser.getRole());
            model.put("inviteLink", "https://hrstack.app/api/v1/invite?token=" + newUser.getId() + loggedInUser.getCompanyId());
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
    public PageResponse<UserResponse> getAllUsersByStatus(InviteStatus inviteStatus, int page, int size) {
        User loggedInUser = currentUserUtil.getLoggedInUser();
        if (loggedInUser.getCompanyId() == null) {
            throw new UnauthorizedException("User is not linked to any company");
        }
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<User> users = userRepository.findByCompanyAndStatus(loggedInUser.getCompany(), inviteStatus, pageRequest);
        if (inviteStatus == null) {
            throw new InvalidRequestException("Invite status is required");
        }
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
    public void updateUser(String id, UpdateUserRequest request) {
        Optional<User> existingUserUser = userRepository.findById(id);
        if (existingUserUser.isEmpty()){
            throw new InvalidRequestException("User with the id " + id + " does not exist.");
        }
        User foundUser = existingUserUser.get();
        foundUser.setDepartment(request.getDepartment());
        foundUser.setReportsTo(request.getReportsTo());
        foundUser.setJobTitle(request.getJobTitle());
        foundUser.setRole(request.getRole());
        foundUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(foundUser);
    }

    @Override
    public void deleteUser(String id) {

    }

    @Override
    public LoginResponse login(LoginRequest request) {
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                ));
        User user = (User) authentication.getPrincipal();
        if(user.getRole().equals(Role.ADMIN) && user.getCompany().getIsVerified().equals(false)){
            throw new UnauthorizedException("Verify your email to continue");
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

        String sessionId = jwtService.getSessionId(refreshToken);
        if (!redisSessionService.isRefreshSessionActive(sessionId)) {
            throw new InvalidRequestException("Refresh session expired");
        }

        String userId = jwtService.getUserIdFromRefreshToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));
        redisSessionService.saveSession(sessionId, user.getId());
        redisSessionService.saveRefreshSession(sessionId, user.getId());

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
       redisSessionService.deleteAll(sessionId);
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
}


