package com.hrstack.dto.requestDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterCompanyRequest {
    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Workspace URL is required")
    private String workspaceUrl;

    @NotBlank(message = "Password should not be empty")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*+=])(?=\\S+$).{8,}$",
            message = "Password must contain at least one uppercase, one lowercase, one digit, a character and no whitespace")
    private String adminPassword;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*+=])(?=\\S+$).{8,}$",
            message = "Password must contain at least one uppercase, one lowercase, one digit, a character and no whitespace")
    private String reEnterPassword;

    @NotBlank(message = "Email is required")
    @Email(message = "example@email.com")
    private String adminEmail;
}
