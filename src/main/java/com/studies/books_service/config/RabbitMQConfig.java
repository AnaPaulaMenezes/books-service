package com.studies.books_service.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "books.exchange";
    public static final String QUEUE_FINISHED = "books.finished.queue";
    public static final String ROUTING_KEY_FINISHED = "book.finished";

    public static final String DLX = "books.dlx";
    public static final String QUEUE_FINISHED_DLQ = "books.finished.dlq";
    public static final String ROUTING_KEY_FINISHED_DLQ = "book.finished.dlq";

    @Bean
    public TopicExchange booksExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue finishedQueue() {
        return QueueBuilder.durable(QUEUE_FINISHED)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(ROUTING_KEY_FINISHED_DLQ)
                .build();
    }

    @Bean
    public Binding finishedBinding(Queue finishedQueue, TopicExchange booksExchange) {
        return BindingBuilder.bind(finishedQueue)
                .to(booksExchange)
                .with(ROUTING_KEY_FINISHED);
    }

    @Bean
    public MessageConverter jsonMessageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX);
    }

    @Bean
    public Queue finishedDeadLetterQueue() {
        return QueueBuilder.durable(QUEUE_FINISHED_DLQ).build();
    }

    @Bean
    public Binding finishedDlqBinding(Queue finishedDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(finishedDeadLetterQueue)
                .to(deadLetterExchange)
                .with(ROUTING_KEY_FINISHED_DLQ);
    }

}
