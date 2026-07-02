package com.hrstack.repositories;

import com.hrstack.entities.Otp;
import com.hrstack.enums.OtpPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findTopByEmailAndPurposeOrderByCreatedAtDesc(String email, OtpPurpose purpose);
    Optional<Otp> findByEmailAndPurpose(String email, OtpPurpose otpPurpose);
}

