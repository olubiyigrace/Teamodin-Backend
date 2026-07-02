package com.hrstack.controllers;

import com.hrstack.services.UserService;
import com.hrstack.utils.ApiResponse;
import com.hrstack.utils.ChangePasswordRequest;
import com.hrstack.utils.UpdateUserProfileRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1")
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;


    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(changePasswordRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Password changed successfully", null));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        userService.logout(request);
        return ResponseEntity.ok(ApiResponse.success(true, "Logout successful", null));
    }

    @PostMapping("/edit-admin-profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> update(@Valid @RequestBody UpdateUserProfileRequest request) {
        userService.update(request);
        return ResponseEntity.ok(ApiResponse.success(true, "Profile updated successfully", null));
    }
}
