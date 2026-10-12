package com.studies.books_service.service;

import com.studies.books_service.config.RabbitMQConfig;
import com.studies.books_service.dto.event.BookFinishedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BookEventLister {
    private static final Logger log = LoggerFactory.getLogger(BookEventLister.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_FINISHED)
    public void handleBookFinishedEvent(BookFinishedEvent message) {
      //  throw new IllegalStateException("teste");
        log.info("Evento book.finished recebido: {}", message);
    }
}
