package it.unisalento.pasproject.analyticsservice.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ------  SECURITY  ------ //

    // Needed by authentication service

    @Value("${rabbitmq.exchange.security.name}")
    private String securityExchange;

    @Bean
    public TopicExchange securityExchange() {
        return new TopicExchange(securityExchange);
    }

    // ------  END SECURITY  ------ //

    // ------  ANALYTICS  ------ //

    //rabbitmq.routing.sendUpdatedAssignmentData.key=assignment.update
    //# Exchange comune per l'analytics
    //rabbitmq.exchange.analytics.name=analytics-exchange
    //rabbitmq.queue.analytics.name=analytics-data-queue

    @Value("${rabbitmq.exchange.analytics.name}")
    private String analyticsExchange;

    @Value("${rabbitmq.queue.analytics.name}")
    private String analyticsQueue;

    @Value("${rabbitmq.routing.updatedAssignmentData.key}")
    private String updatedAssignmentDataRoutingKey;

    @Bean
    public Queue analyticsQueue() {
        return new Queue(analyticsQueue);
    }

    @Bean
    public TopicExchange analyticsExchange() {
        return new TopicExchange(analyticsExchange);
    }

    @Bean
    public Binding updatedAssignmentDataBinding() {
        return BindingBuilder.bind(analyticsQueue()).to(analyticsExchange()).with(updatedAssignmentDataRoutingKey);
    }

    // ------  END ANALYTICS  ------ //

    /**
     * Creates a message converter for JSON messages.
     *
     * @return a new Jackson2JsonMessageConverter instance.
     */
    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Creates an AMQP template for sending messages.
     *
     * @param connectionFactory the connection factory to use.
     * @return a new RabbitTemplate instance.
     */
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
