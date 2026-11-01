package com.studies.books_service;

import com.studies.books_service.domain.idempotency.Idempotency;
import com.studies.books_service.domain.idempotency.IdempotencyStatus;
import com.studies.books_service.repository.IdempotencyStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class IdempotencyStoreTest {

    @Autowired
    private IdempotencyStore store;

    @Test
    void shouldReserveOnlyOnceWhenCalledConcurrently() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();
        int successCount = 0;

        try {
            Callable<Boolean> task = () -> {
                latch.await();
                return store.tryReserve(idempotencyKey, "requestHash");
            };

            for(int i = 0; i < 20; i++) {
                futures.add(executorService.submit(task));
            }

            latch.countDown();

            for (Future<Boolean> future : futures) {
                if (future.get(10, TimeUnit.SECONDS)) {
                    successCount++;
                }
            }
        } finally {
            latch.countDown();
            executorService.shutdownNow();
        }


        assertThat(successCount).isEqualTo(1);
    }

    @Test
    void shouldReturnEmptyWhenKeyDoesNotExist() {
        assertThat(store.find("non-existent-key")).isEmpty();
    }

    @Test
    void shouldReturnCompletedWithOriginalHashAfterComplete() {
        String key = UUID.randomUUID().toString();
        String requestHash = "requestHash";
        store.tryReserve(key, requestHash);
        store.complete(key, 201, "responseBody");

        Idempotency record = store.find(key).orElseThrow();
        assertThat(record.status()).isEqualTo(IdempotencyStatus.COMPLETED);
        assertThat(record.requestHash()).isEqualTo(requestHash);
        assertThat(record.httpStatus()).isEqualTo(201);
    }
}