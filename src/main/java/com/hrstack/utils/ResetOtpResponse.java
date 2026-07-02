package com.hrstack.utils;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetOtpResponse {
    private String resetToken;
}
