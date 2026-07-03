package com.hrstack.controllers;

import com.hrstack.services.UserService;
import com.hrstack.utils.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PutMapping("/edit-profile")
    public ResponseEntity<ApiResponse<String>> editProfile(@Valid @RequestBody EditProfileRequest request) {
        userService.editProfile(request);
        return ResponseEntity.ok(ApiResponse.success(true, "Profile updated successfully", null));
    }

    @PostMapping(value = "/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        userService.uploadProfilePicture(file);
        return ResponseEntity.ok(ApiResponse.success(true, "Profile picture uploaded successfully.", null));
    }
}
