package com.studies.books_service.service;

import com.studies.books_service.domain.idempotency.Idempotency;
import com.studies.books_service.domain.idempotency.IdempotencyStatus;
import com.studies.books_service.dto.idempotency.IdempotentResponse;
import com.studies.books_service.exceptions.IdempotencyConflictException;
import com.studies.books_service.exceptions.IdempotencyKeyReuseException;
import com.studies.books_service.repository.IdempotencyStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class IdempotencyService {

    private static final int MAX_ATTEMPTS = 2;
    @Autowired
    private IdempotencyStore idempotencyStore;

    @Autowired
    private JsonMapper jsonMapper;


    public IdempotentResponse execute(String key, Object body, Supplier<IdempotentResponse> action){
        String requestHash = this.hash(body);

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            boolean isReserved = idempotencyStore.tryReserve(key, requestHash);
            if (isReserved) {
                return runAndComplete(key, action);
            }

            Optional<Idempotency> existingKey = idempotencyStore.find(key);

            if (existingKey.isPresent()) {
                return handleExistingKey(existingKey.get(), requestHash);
            }
        }

        throw new IdempotencyConflictException("Idempotency Key Conflict: Unable to reserve or find the idempotency key after multiple attempts.");
    }

    private String hash(Object requestBody)  {
        try{
            String bodyAsString = jsonMapper.writeValueAsString(requestBody);
            byte[] bodyBytes = bodyAsString.getBytes(StandardCharsets.UTF_8);
            byte[] hashBytes = MessageDigest.getInstance("SHA-256").digest(bodyBytes);
            return HexFormat.of().formatHex(hashBytes);
        }catch (NoSuchAlgorithmException e){
            throw new RuntimeException("SHA-256 not available", e);
        }

    }

    private IdempotentResponse runAndComplete(String key, Supplier<IdempotentResponse> action){
        IdempotentResponse result;
        try {
            result = action.get();
        }catch (RuntimeException e) {
            idempotencyStore.release(key);
            throw e;
        }

        idempotencyStore.complete(key, result.httpStatus(), result.responseBody());
        return result;
    }

    private IdempotentResponse handleExistingKey(Idempotency existingKey, String requestHash) {
        if (existingKey.status() == IdempotencyStatus.IN_PROGRESS) {
            throw new IdempotencyConflictException("Idempotency Key Conflict: Another request with the same key is already in progress.");
        }

        if (!existingKey.requestHash().equals(requestHash)) {
            throw new IdempotencyKeyReuseException("Idempotency Key Reuse: A request with the same key has completed with a different body.");
        }

        return new IdempotentResponse(existingKey.httpStatus(), existingKey.responseBody());
    }

}