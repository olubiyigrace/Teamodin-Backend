package com.hrstack.utils;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequest {
    @NotBlank(message = "Username should not be empty")
    private String email;
    @NotBlank(message = "Password should not be empty")
    private String password;
}