package com.hrstack.utils;

import com.hrstack.enums.ReportsTo;
import com.hrstack.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    @NotNull(message = "Role cannot be null")
    private Role role;

    @NotNull(message = "User has to report to someone")
    private ReportsTo reportsTo;

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    @NotBlank(message = "Department is required")
    private String department;
}
