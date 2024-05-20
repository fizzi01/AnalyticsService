package it.unisalento.pasproject.analyticsservice.business.io.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("RabbitMQProducer")
public class RabbitMQProducer implements MessageProducerStrategy {

    /**
     * Logger instance for logging events.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQProducer.class);

    /**
     * RabbitTemplate instance for sending messages to RabbitMQ.
     */

    private final RabbitTemplate rabbitTemplate;

    /**
     * Constructor for the RabbitMQJsonProducer.
     *
     * @param rabbitTemplate The RabbitTemplate instance to use for sending messages.
     */
    @Autowired
    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public <T> void sendMessage(T messageDTO, String routingKey, String exchange) {
        LOGGER.info(String.format("RabbitMQ message sent: %s", messageDTO.toString()));
        rabbitTemplate.convertAndSend(exchange, routingKey, messageDTO);
    }

    /**
     * Method to send a Object message to RabbitMQ.
     * <p>
     * Utilizzando il replyTo header nei messaggi,
     * il servizio ricevente può automaticamente inviare
     * la risposta alla coda appropriata senza necessità di
     * configurazioni aggiuntive o hardcoded.
     *</p>
     * @param messageDTO The Object to send as a JSON message.
     * @param routingKey The routing key to use when sending the message.
     * @param exchange The exchange to send the message to.
     * @param replyTo The QUEUE to receive the message.
     */
    @Override
    public <T> void sendMessage(T messageDTO, String routingKey, String exchange, String replyTo) {
        LOGGER.info(String.format("RabbitMQ message sent: %s", messageDTO.toString()));
        rabbitTemplate.convertAndSend(exchange, routingKey, messageDTO, m -> {
            m.getMessageProperties().setReplyTo(replyTo);
            return m;
        });
    }
}
