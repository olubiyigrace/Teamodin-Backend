package com.hrstack.services;

import com.hrstack.dto.RegisterUserRequest;
import com.hrstack.entities.User;
import com.hrstack.enums.OtpPurpose;
import com.hrstack.exceptions.DuplicateResourceException;
import com.hrstack.exceptions.InvalidRequestException;
import com.hrstack.dto.OtpRequest;
import com.hrstack.mappers.UserMapper;
import com.hrstack.orders.OrderProducer;
import com.hrstack.orders.ProducerMessage;
import com.hrstack.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final OrderProducer orderProducer;


    @Override
    public void create(RegisterUserRequest request) {
        Optional<User> existingUser = userRepository.findByWorkspaceUrl(request.getWorkspaceUrl());
        if(existingUser.isPresent()){
            throw new DuplicateResourceException("Workspace already exists");
        }
        if (!request.getPassword().equals(request.getReEnterPassword())){
            throw new InvalidRequestException("Passwords do not match");
        }
        User newUser = userMapper.toEntity(request);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(newUser);

        String otp = otpService.createOtp(
                OtpRequest.builder()
                        .email(request.getEmail())
                        .purpose(OtpPurpose.VERIFY_ACCOUNT)
                        .build()
        );

        orderProducer.sendMessage(
                ProducerMessage.builder()
                        .email(request.getEmail())
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
