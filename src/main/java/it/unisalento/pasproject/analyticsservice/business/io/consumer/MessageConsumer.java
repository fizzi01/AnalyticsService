package it.unisalento.pasproject.analyticsservice.business.io.consumer;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Setter
public class MessageConsumer {
    private MessageConsumerStrategy strategy;

    @Autowired
    public MessageConsumer(MessageConsumerStrategy strategy) {
        this.strategy = strategy;
    }

    public <T> T consumeMessage(T message) {
        return strategy.consumeMessage(message);
    }

    public String consumeMessage(String message, String queueName) {
        return strategy.consumeMessage(message, queueName);
    }
}
