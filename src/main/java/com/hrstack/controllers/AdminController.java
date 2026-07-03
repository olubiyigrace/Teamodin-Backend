package com.hrstack.controllers;

import com.hrstack.dto.requestDto.RegisterUserRequest;
import com.hrstack.dto.responseDto.UserResponse;
import com.hrstack.enums.InviteStatus;
import com.hrstack.services.UserService;
import com.hrstack.utils.ApiResponse;
import com.hrstack.utils.PageResponse;
import com.hrstack.utils.UpdateUserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserService userService;


    @PostMapping("/register-user")
    public ResponseEntity<ApiResponse<String>> createUser(@Valid @RequestBody RegisterUserRequest request){
        userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success(true, "User created successfully", null));
    }

    @GetMapping("/all-users-by-status")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsers(
            @RequestParam (required = false) InviteStatus inviteStatus,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(true, inviteStatus + " users retrieved successfully",
                userService.getAllUsersByStatus(inviteStatus, page, size)));
    }

    @GetMapping("/all-users")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllLoanApplications(
            @RequestParam(name = "page", defaultValue = "0") final int page,
            @RequestParam(name = "size", defaultValue = "10") final int size) {
        return ResponseEntity.ok(ApiResponse.success(true, "Loan applications retrieved successfully",
                userService.getAllUsers(page, size)));
    }

    @PatchMapping("/update-user")
    public ResponseEntity<ApiResponse<String>>  updateUser(@RequestParam String id, @Valid @RequestBody UpdateUserRequest request) {
        userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(true, "User updated successfully", null));

    }
}
