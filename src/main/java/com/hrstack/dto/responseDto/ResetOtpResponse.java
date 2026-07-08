package com.hrstack.dto.responseDto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetOtpResponse {
    private String resetToken;
}
