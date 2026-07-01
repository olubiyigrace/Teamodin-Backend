package com.hrstack.utils;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Password should not be empty")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*+=])(?=\\S+$).{8,}$",
            message = "Password must contain at least one uppercase, one lowercase, one digit, a character and no whitespace")
    private String newPassword;

    @NotBlank(message = "Password should not be empty")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*+=])(?=\\S+$).{8,}$",
            message = "Password must contain at least one uppercase, one lowercase, one digit, a character and no whitespace")
    private String oldPassword;

    @NotBlank(message = "Password should not be empty")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*+=])(?=\\S+$).{8,}$",
            message = "Password must contain at least one uppercase, one lowercase, one digit, a character and no whitespace")
    private String confirmPassword;
}
