package it.unisalento.pasproject.analyticsservice.business.io.exchanger;

public interface MessageExchangeStrategy {
    <T> T exchangeMessage(String message, String routingKey,String exchange, Class<T> object);
    <T, R> R exchangeMessage( T message, String routingKey, String exchange, Class<R> responseType);
}
