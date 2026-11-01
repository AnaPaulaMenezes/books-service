package com.studies.books_service.service;

import com.studies.books_service.domain.idempotency.Idempotency;
import com.studies.books_service.domain.idempotency.IdempotencyStatus;
import com.studies.books_service.domain.idempotency.IdempotentResponse;
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

    @Autowired
    private IdempotencyStore idempotencyStore;

    @Autowired
    private JsonMapper jsonMapper;


    public IdempotentResponse execute(String key, Object body, Supplier<IdempotentResponse> action){
        String requestHash = this.hash(body);

        boolean isReserved = idempotencyStore.tryReserve(key, requestHash);

        if (!isReserved) {
            return handleExistingKey(key, requestHash);
        }
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

    private IdempotentResponse handleExistingKey(String key, String requestHash) {
        Optional<Idempotency> optionalKey = idempotencyStore.find(key);

        if (optionalKey.isEmpty()) {
            throw new IllegalStateException("Idempotency key not found");
        }

        Idempotency existingKey = optionalKey.get();

        if (existingKey.status() == IdempotencyStatus.IN_PROGRESS) {
            throw new IllegalStateException("Idempotency Conflict Exception");
        }

        if (existingKey.status() == IdempotencyStatus.COMPLETED && !existingKey.requestHash().equals(requestHash)) {
            throw new IllegalStateException("Idempotency Key Reuse Exception");
        }

        return new IdempotentResponse(existingKey.httpStatus(), existingKey.responseBody());
    }

}