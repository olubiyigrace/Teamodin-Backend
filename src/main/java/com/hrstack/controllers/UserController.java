package com.hrstack.controllers;

import com.hrstack.dto.requestDto.ChangePasswordRequest;
import com.hrstack.dto.requestDto.EditProfileRequest;
import com.hrstack.dto.responseDto.ApiResponse;
import com.hrstack.dto.responseDto.SessionResponse;
import com.hrstack.entities.UserSession;
import com.hrstack.services.RedisSessionService;
import com.hrstack.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/api/v1")
@RequiredArgsConstructor
@RestController
@PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','MANAGER')")
public class UserController {
    private final UserService userService;


    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(changePasswordRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Password changed successfully", null));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getSessions(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(true, "Sessions retrieved successfully", userService.getActiveSessions(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        userService.logout(request);
        return ResponseEntity.ok(ApiResponse.success(true, "Logout successful", null));
    }

    @PatchMapping("/edit-profile")
    public ResponseEntity<ApiResponse<String>> editProfile(@Valid @RequestBody EditProfileRequest request) {
        userService.editProfile(request);
        return ResponseEntity.ok(ApiResponse.success(true, "Profile updated successfully", null));
    }

    @PostMapping(value = "/upload-profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        userService.uploadProfilePicture(file);
        return ResponseEntity.ok(ApiResponse.success(true, "Profile picture uploaded successfully.", null));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAllDevices() {
        userService.logoutAllDevices();
        return ResponseEntity.ok(ApiResponse.success(true, "Logout successful!", null));
    }

    @DeleteMapping("/revoke-session")
    public ResponseEntity<ApiResponse<Void>> revokeSession(@RequestParam String sessionId) {
        userService.revokeSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(true, "Session revoked successfully", null));
    }
}
