package com.hrstack.dto.requestDto;

import com.hrstack.enums.OtpPurpose;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtpRequest {
    private String email;
    private OtpPurpose purpose;
}
