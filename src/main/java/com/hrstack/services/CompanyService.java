package com.hrstack.services;

import com.hrstack.dto.requestDto.RegisterCompanyRequest;

public interface CompanyService {
    void create(RegisterCompanyRequest request);
    void resendVerificationOtp(String email);
}
