package it.unisalento.pasproject.analyticsservice.business.io.exchanger;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Setter
public class MessageExchanger {
    
    
    private MessageExchangeStrategy strategy;

    @Autowired
    public MessageExchanger(MessageExchangeStrategy messageExchangeStrategy) {
        this.strategy = messageExchangeStrategy;
    }

    public <T> T exchangeMessage(String message, String routingKey,String exchange, Class<T> object) {
        return strategy.exchangeMessage(message, routingKey, exchange,object);
    }

    public <T, R> R exchangeMessage(T message, String routingKey, String exchange, Class<R> responseType) {
        return strategy.exchangeMessage(message, routingKey, exchange, responseType);
    }
}
