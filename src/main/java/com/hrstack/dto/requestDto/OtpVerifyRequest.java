package com.hrstack.dto.requestDto;

import com.hrstack.enums.OtpPurpose;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpVerifyRequest {
    private String plainOtp;
    private String adminEmail;
    private OtpPurpose purpose;
}
