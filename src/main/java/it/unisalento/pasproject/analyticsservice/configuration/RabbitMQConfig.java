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

    // ------  TASK & ASSIGNMENT  ------ //


    @Value("${rabbitmq.exchange.taskData.name}")
    private String taskDataExchange;

    @Value("${rabbitmq.queue.analyticsAssignment.name}")
    private String analyticsAssignmentQueue;

    @Value("${rabbitmq.routing.analyticsAssignment.key}")
    private String analyticsAssignmentRoutingKey;

    @Value("${rabbitmq.queue.analyticsNewTask.name}")
    private String analyticsNewTaskQueue;

    @Value("${rabbitmq.routing.newTask.key}")
    private String newTaskRoutingKey;

    @Value("${rabbitmq.routing.taskexecution.key}")
    private String taskExecutionRoutingKey;

    @Value("${rabbitmq.queue.analyticsTaskExecution.name}")
    private String analyticsTaskExecutionQueue;

    @Bean
    public TopicExchange taskDataExchange() {
        return new TopicExchange(taskDataExchange);
    }

    @Bean
    public Queue analyticsAssignmentQueue() {
        return new Queue(analyticsAssignmentQueue);
    }

    @Bean
    public Binding analyticsAssignmentBinding() {
        return BindingBuilder.bind(analyticsAssignmentQueue()).to(taskDataExchange()).with(analyticsAssignmentRoutingKey);
    }

    @Bean
    public Queue analyticsNewTaskQueue() {
        return new Queue(analyticsNewTaskQueue);
    }

    @Bean
    public Binding analyticsNewTaskBinding() {
        return BindingBuilder.bind(analyticsNewTaskQueue()).to(taskDataExchange()).with(newTaskRoutingKey);
    }

    @Bean
    public Queue analyticsTaskExecutionQueue() {
        return new Queue(analyticsTaskExecutionQueue);
    }

    @Bean
    public Binding analyticsTaskExecutionBinding() {
        return BindingBuilder.bind(analyticsTaskExecutionQueue()).to(taskDataExchange()).with(taskExecutionRoutingKey);
    }

    // ------  END TASK & ASSIGNMENT  ------ //

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
