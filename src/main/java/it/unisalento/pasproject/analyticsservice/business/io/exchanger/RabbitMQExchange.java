package it.unisalento.pasproject.analyticsservice.business.io.exchanger;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("RabbitMQExchange")
public class RabbitMQExchange implements MessageExchangeStrategy{


    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMQExchange(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public <T> T exchangeMessage(String message, String routingKey, String exchange, Class<T> object) {

        // Inviare il messaggio e attendere una risposta
        ParameterizedTypeReference<T> typeReference = new ParameterizedTypeReference<T>() {};
        T res = rabbitTemplate.convertSendAndReceiveAsType(exchange, routingKey,message, typeReference);

        //Message response = rabbitTemplate.sendAndReceive(exchange, routingKey, request);

        // Verificare se è stata ricevuta una risposta
        if (res == null) {
            throw new UsernameNotFoundException(message);
        }

        // Restituire la risposta
        return res;
    }

    @Override
    public <T, R> R exchangeMessage(T message, String routingKey, String exchange, Class<R> responseType) {
        rabbitTemplate.setReplyTimeout(1000); // Timeout di 1 secondo
        return rabbitTemplate.convertSendAndReceiveAsType(exchange, routingKey, message,
                new ParameterizedTypeReference<R>() {});
    }
}
