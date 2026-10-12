package com.studies.books_service.service;

import com.studies.books_service.config.RabbitMQConfig;
import com.studies.books_service.dto.event.BookFinishedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class BookEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(BookEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public BookEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishBookFinishedEvent(BookFinishedEvent event) {

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_FINISHED,
                event
        );
        log.info("Evento book.finished publicado: bookId={}", event.bookId());
    }
}
