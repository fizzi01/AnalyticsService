package it.unisalento.pasproject.analyticsservice.business.io.producer;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Setter
@Service
public class MessageProducer {

    private MessageProducerStrategy strategy;

    @Autowired
    public MessageProducer(MessageProducerStrategy strategy) {
        this.strategy = strategy;
    }


    public <T> void sendMessage(T messageDTO, String routingKey, String exchange) {
        strategy.sendMessage(messageDTO, routingKey, exchange);
    }


    public <T> void sendMessage(T messageDTO, String routingKey, String exchange, String replyTo) {
        strategy.sendMessage(messageDTO, routingKey, exchange, replyTo);
    }

}
