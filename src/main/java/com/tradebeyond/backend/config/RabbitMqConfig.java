package com.tradebeyond.backend.config;

import com.tradebeyond.backend.enums.OutBoxStatusEnum;
import com.tradebeyond.backend.mapper.OutboxEventDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

@Slf4j
@Configuration
public class RabbitMqConfig {
    @Autowired
    private OutboxEventDao outboxEventDao;

    public static final String ORDER_QUEUE = "order_queue";
    public static final String ORDER_RETRY_QUEUE = "order_retry_queue";
    public static final String ORDER_DEAD_QUEUE = "order_dead_queue";

    public static final String ORDER_ROUTING_KEY = "order_routingKey";
    public static final String ORDER_RETRY_ROUTING_KEY = "order_retry_routingKey";
    public static final String ORDER_DEAD_ROUTING_KEY = "order_dead_routingKey";

    public static final String ORDER_EXCHANGE = "order_exchange";
    public static final String ORDER_RETRY_EXCHANGE = "order_retry_exchange";
    public static final String ORDER_DEAD_EXCHANGE = "order_dead_exchange";


    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean(name = "rabbitTemplateMain")
    public RabbitTemplate rabbitTemplateMain(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setMessageConverter(messageConverter());
        rabbitTemplate.setConfirmCallback((correlationData, ack, s) -> {
            handleConfirm(correlationData, ack, s, OutBoxStatusEnum.PUBLISHED.getCode(), OutBoxStatusEnum.IN_FLIGHT.getCode());
        });
        rabbitTemplate.setReturnsCallback(this::handleReturn);
        return rabbitTemplate;
    }

    @Bean(name = "rabbitTemplateDead")
    public RabbitTemplate rabbitTemplateDead(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setMessageConverter(messageConverter());
        rabbitTemplate.setConfirmCallback((correlationData, ack, s) -> {
            handleConfirm(correlationData, ack, s, OutBoxStatusEnum.DEAD_PUBLISHED.getCode(), OutBoxStatusEnum.DEAD_IN_FLIGHT.getCode());
        });
        rabbitTemplate.setReturnsCallback(this::handleReturn);
        return rabbitTemplate;
    }

    private void handleConfirm(CorrelationData correlationData, boolean ack, String s, Integer newStatus, Integer oldStatus) {
        if (correlationData == null) {
            log.error("correlationData is null");
        } else {
            String eventId = correlationData.getId();
            if (ack) {
                log.info("correlationData id:{} is ack", eventId);
                int updated = outboxEventDao.updateStatusByEventId(newStatus,
                        eventId, new Date(), oldStatus);
                if (updated != 1) {
                    log.warn("correlationData id:{} update failed", eventId);
                }
            } else {
                log.error("correlationData id:{} is not ack e:{}", eventId, s);
                int updated = outboxEventDao.updateRetryByEventId(OutBoxStatusEnum.FAILED.getCode(),
                        new Date(System.currentTimeMillis() + 20000), eventId, new Date());
                if (updated != 1) {
                    log.warn("ConfirmCallback updateRetryByEventId affectedRows={}, eventId={}", updated, eventId);
                }
            }
        }
    }

    public void handleReturn(ReturnedMessage returnedMessage){
        String messageId = returnedMessage.getMessage().getMessageProperties().getMessageId();
        int updated = outboxEventDao.updateRetryByEventId(OutBoxStatusEnum.UNROUTABLE.getCode(),
                new Date(System.currentTimeMillis() + 20000), messageId, new Date());
        if (updated != 1) {
            log.warn("ReturnsCallback updateRetryByEventId affectedRows={}, eventId={}", updated, messageId);
        }
    }

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE)
                .ttl(10000)
                .deadLetterExchange(ORDER_RETRY_EXCHANGE)
                .deadLetterRoutingKey(ORDER_RETRY_ROUTING_KEY).build();
    }
    @Bean
    public Exchange orderExchange() {
        return ExchangeBuilder.directExchange(ORDER_EXCHANGE).build();
    }
    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue())
                .to(orderExchange())
                .with(ORDER_ROUTING_KEY).noargs();
    }

    @Bean
    public Queue orderRetryQueue() {
        return QueueBuilder.durable(ORDER_RETRY_QUEUE)
                .ttl(5000)
                .deadLetterExchange(ORDER_EXCHANGE)
                .deadLetterRoutingKey(ORDER_ROUTING_KEY).build();
    }
    @Bean
    public Exchange orderRetryExchange() {
        return ExchangeBuilder.directExchange(ORDER_RETRY_EXCHANGE).build();
    }
    @Bean
    public Binding orderRetryBinding() {
        return BindingBuilder.bind(orderRetryQueue())
                .to(orderRetryExchange())
                .with(ORDER_ROUTING_KEY).noargs();
    }

    @Bean
    public Queue orderDeadQueue() {
        return QueueBuilder.durable(ORDER_DEAD_QUEUE).build();
    }
    @Bean
    public Exchange orderDeadExchange() {
        return ExchangeBuilder.directExchange(ORDER_DEAD_EXCHANGE).build();
    }
    @Bean
    public Binding orderDeadBinding() {
        return BindingBuilder.bind(orderDeadQueue())
                .to(orderDeadExchange())
                .with(ORDER_DEAD_ROUTING_KEY).noargs();
    }




}
