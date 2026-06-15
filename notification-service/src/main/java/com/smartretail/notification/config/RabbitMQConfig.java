package com.smartretail.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queues.user-registered}")
    private String userRegisteredQueue;

    @Value("${rabbitmq.queues.order-created}")
    private String orderCreatedQueue;

    @Value("${rabbitmq.queues.order-cancelled}")
    private String orderCancelledQueue;

    @Value("${rabbitmq.queues.low-stock}")
    private String lowStockQueue;

    @Value("${rabbitmq.queues.restock-created}")
    private String restockCreatedQueue;

    @Value("${rabbitmq.queues.restock-completed}")
    private String restockCompletedQueue;

    @Bean
    public TopicExchange smartRetailExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean public Queue userRegisteredQueue() { return new Queue(userRegisteredQueue, true); }
    @Bean public Queue orderCreatedQueue() { return new Queue(orderCreatedQueue, true); }
    @Bean public Queue orderCancelledQueue() { return new Queue(orderCancelledQueue, true); }
    @Bean public Queue lowStockQueue() { return new Queue(lowStockQueue, true); }
    @Bean public Queue restockCreatedQueue() { return new Queue(restockCreatedQueue, true); }
    @Bean public Queue restockCompletedQueue() { return new Queue(restockCompletedQueue, true); }

    @Bean
    public Binding userRegisteredBinding() {
        return BindingBuilder.bind(userRegisteredQueue()).to(smartRetailExchange()).with("user.registered");
    }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder.bind(orderCreatedQueue()).to(smartRetailExchange()).with("order.created");
    }

    @Bean
    public Binding orderCancelledBinding() {
        return BindingBuilder.bind(orderCancelledQueue()).to(smartRetailExchange()).with("order.cancelled");
    }

    @Bean
    public Binding lowStockBinding() {
        return BindingBuilder.bind(lowStockQueue()).to(smartRetailExchange()).with("product.low.stock");
    }

    @Bean
    public Binding restockCreatedBinding() {
        return BindingBuilder.bind(restockCreatedQueue()).to(smartRetailExchange()).with("restock.created");
    }

    @Bean
    public Binding restockCompletedBinding() {
        return BindingBuilder.bind(restockCompletedQueue()).to(smartRetailExchange()).with("restock.completed");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
