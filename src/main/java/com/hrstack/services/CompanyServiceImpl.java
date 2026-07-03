package com.hrstack.services;

import com.hrstack.dto.requestDto.OtpRequest;
import com.hrstack.dto.requestDto.RegisterCompanyRequest;
import com.hrstack.entities.Company;
import com.hrstack.entities.User;
import com.hrstack.enums.OtpPurpose;
import com.hrstack.enums.Role;
import com.hrstack.exceptions.DuplicateResourceException;
import com.hrstack.exceptions.InvalidRequestException;
import com.hrstack.mappers.CompanyMapper;
import com.hrstack.orders.OrderProducer;
import com.hrstack.orders.ProducerMessage;
import com.hrstack.properties.WorkspaceProperties;
import com.hrstack.repositories.CompanyRepository;
import com.hrstack.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CompanyServiceImpl implements CompanyService{
    private final WorkspaceProperties workspaceProperties;
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final OrderProducer orderProducer;


    @Override
    public void create(RegisterCompanyRequest request) {
        Optional<Company> existingWorkspace = companyRepository.findByWorkspaceUrl(workspaceProperties.getBaseUrl() + request.getWorkspaceUrl());
        if(existingWorkspace.isPresent()){
            throw new DuplicateResourceException("Workspace already exists");
        }
        if (!request.getAdminPassword().equals(request.getReEnterPassword())){
            throw new InvalidRequestException("Passwords do not match");
        }
        Company newWorkspace = companyMapper.toEntity(request);
        newWorkspace.setAdminPassword(passwordEncoder.encode(request.getAdminPassword()));
        companyRepository.save(newWorkspace);
        try {
            createAdminUser(newWorkspace);
        } catch (final Exception e) {
            log.error("Unable to create workspace", e);
            throw e;
        }
    }

    private void createAdminUser(Company company){
        Optional<User> existingUser = userRepository.findByEmail(company.getAdminEmail());
        if (existingUser.isPresent()) {
            log.debug("User already exists");
            throw new DuplicateResourceException("User already exists");
        }
        User adminUser = User.builder()
                .email(company.getAdminEmail())
                .password(company.getAdminPassword())
                .company(company)
                .role(Role.ADMIN)
                .build();
        userRepository.save(adminUser);

        String otp = otpService.createOtp(
                OtpRequest.builder()
                        .email(company.getAdminEmail())
                        .purpose(OtpPurpose.VERIFY_ACCOUNT)
                        .build()
        );
        orderProducer.sendMessage(
                ProducerMessage.builder()
                        .email(company.getAdminEmail())
                        .otp(otp)
                        .purpose(OtpPurpose.VERIFY_ACCOUNT)
                        .build()
        );
    }

    @Override
    public void resendVerificationOtp(String email) {
        String otp = otpService.resendOtp(
                OtpRequest.builder()
                        .email(email)
                        .purpose(OtpPurpose.VERIFY_ACCOUNT)
                        .build()
        );
        orderProducer.sendMessage(
                ProducerMessage.builder()
                        .email(email)
                        .otp(otp)
                        .purpose(OtpPurpose.VERIFY_ACCOUNT)
                        .build()
        );
    }
}
