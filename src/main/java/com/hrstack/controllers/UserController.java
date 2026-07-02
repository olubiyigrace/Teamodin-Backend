package com.hrstack.controllers;

import com.hrstack.services.UserService;
import com.hrstack.utils.ApiResponse;
import com.hrstack.utils.ChangePasswordRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
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
}
