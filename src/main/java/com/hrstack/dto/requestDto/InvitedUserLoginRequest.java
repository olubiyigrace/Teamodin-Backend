package com.hrstack.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvitedUserLoginRequest {
    @NotBlank(message = "Email should not be empty")
    private String email;

    @NotBlank(message = "Password should not be empty")
    private String password;

    @NotBlank(message = "Invite token should not be empty")
    private String inviteToken;
}
