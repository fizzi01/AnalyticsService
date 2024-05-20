package it.unisalento.pasproject.analyticsservice.business.io.producer;

public interface MessageProducerStrategy {
    <T> void sendMessage(T messageDTO,String routingKey, String exchange);
    <T> void sendMessage(T messageDTO,String routingKey, String exchange, String replyTo);

}
