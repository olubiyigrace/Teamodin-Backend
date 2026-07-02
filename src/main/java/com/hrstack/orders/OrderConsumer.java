package com.hrstack.orders;

import com.hrstack.config.RabbitConfig;
import com.hrstack.enums.OtpPurpose;
import com.hrstack.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {
    private final EmailService emailService;

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void consumeMessage(@Payload ProducerMessage request) {
        if (request.getPurpose().equals(OtpPurpose.VERIFY_ACCOUNT) && request.getOtp() != null ) {
            emailService.sendOtpVerificationEmail(request.getEmail(), request.getOtp());
            return;
        }
        if (request.getPurpose().equals(OtpPurpose.RESET_PASSWORD) && request.getOtp() != null ) {
            emailService.sendOtpPasswordResetEmail(request.getEmail(), request.getOtp());
            return;
        }
        emailService.sendSimpleEmail(
                request.getEmail(),
                "HRStack Notification",
                "Your request has been received and is being processed."
        );
    }
}
