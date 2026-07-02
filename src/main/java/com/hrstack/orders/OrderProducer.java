package com.hrstack.orders;

import com.hrstack.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrderProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendMessage(ProducerMessage request) {
        rabbitTemplate.convertAndSend(RabbitConfig.QUEUE, request);
    }
}
