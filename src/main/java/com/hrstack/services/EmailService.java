package com.hrstack.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@EnableAsync
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromAddress;


    public void sendOtpEmail(String to, String otp) {
        sendSimpleEmail(
                to,
                "Verify your HRStack account",
                "Your verification code is: " + otp + "\n\nEnter this code to verify your account."
        );
    }

    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false);
            helper.setFrom(fromAddress, "HR Stack");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            emailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
            throw new IllegalStateException("Failed to send email to " + to, e);
        }
    }

    public void sendOtpVerificationEmail(String email, String otp) {
        Map<String, Object> model = new HashMap<>();
        model.put("otp", otp);

        try {
            sendVerificationEmail(
                    email,
                    "Verify your HRStack account",
                    "verifyAccount",
                    model
            );
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Failed to send verification email to " + email, e);
        }
    }

    public void sendOtpPasswordResetEmail(String email, String otp) {
        Map<String, Object> model = new HashMap<>();
        model.put("otp", otp);

        try {
            sendVerificationEmail(
                    email,
                    "Reset Password",
                    "resetPassword.html",
                    model
            );
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Failed to send verification email to " + email, e);
        }
    }

    @Async
    public void sendVerificationEmail(String to, String subject, String templateName, Map<String, Object> templateModel)
            throws jakarta.mail.MessagingException, UnsupportedEncodingException {

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromAddress, "HR Stack");
        helper.setTo(to);
        helper.setSubject(subject);

        Context context = new Context();
        context.setVariables(templateModel);
        String htmlContent = templateEngine.process(templateName, context);

        helper.setText(htmlContent, true);
        emailSender.send(message);
        log.info("Verification email sent to {}", to);
    }
}
