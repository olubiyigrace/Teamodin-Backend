package com.hrstack.services;

import com.hrstack.dto.requestDto.*;
import com.hrstack.dto.responseDto.LoginResponse;
import com.hrstack.dto.responseDto.PageResponse;
import com.hrstack.dto.responseDto.SessionResponse;
import com.hrstack.dto.responseDto.UserResponse;
import com.hrstack.entities.User;
import com.hrstack.enums.InviteStatus;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    void createUser(RegisterUserRequest registerUserRequest);
    void resendInvite(String id, ResendInviteRequest request);
    PageResponse<UserResponse> getAllUsersByStatus(InviteStatus inviteStatus, int page, int size);
    PageResponse<UserResponse> getAllUsers(int page, int size);
    void updateUser(String id, UpdateUserRequest request);
    User validateWorkspaceInvite(String token, Claims claims);
    LoginResponse invitedUserLogin(InvitedUserLoginRequest request, HttpServletRequest httpServletRequest);
    LoginResponse login(LoginRequest request, HttpServletRequest httpServletRequest);
    LoginResponse refreshToken(RefreshTokenRequest request);
    void changePassword(ChangePasswordRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(String token, ResetPasswordRequest request);
    void logout(HttpServletRequest request);
    List<SessionResponse> getActiveSessions(HttpServletRequest request);
    void logoutAllDevices();
    void revokeSession(String sessionId);
    void editProfile(EditProfileRequest editProfileRequest);
    void uploadProfilePicture(MultipartFile file);
    void deactivateUser(String id);
    void suspendUser(String id);
}
