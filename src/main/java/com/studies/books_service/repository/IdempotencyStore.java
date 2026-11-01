package com.studies.books_service.repository;

import com.studies.books_service.domain.idempotency.Idempotency;
import com.studies.books_service.domain.idempotency.IdempotencyStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Optional;


@Component
public class IdempotencyStore {
    private static final Duration RESERVATION_TTL = Duration.ofMinutes(5);
    private static final Duration COMPLETED_TTL = Duration.ofDays(1);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JsonMapper jsonMapper;


    public boolean tryReserve(String key, String requestHash)  {
            String idempotencyKey = this.buildKey(key);
            Idempotency value = new Idempotency(IdempotencyStatus.IN_PROGRESS, requestHash, null, null);
            Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(idempotencyKey, jsonMapper.writeValueAsString(value), RESERVATION_TTL);
            return Boolean.TRUE.equals(result);
    }

    public Optional<Idempotency> find(String key){
        String idempotencyKey = this.buildKey(key);
        var result = stringRedisTemplate.opsForValue().get(idempotencyKey);

        if (result == null){
            return Optional.empty();
        }

        Idempotency value = jsonMapper.readValue(result, Idempotency.class);
        return Optional.of(value);

    }

    public void complete(String key, int httpStatus, String responseBody ){
        Optional<Idempotency> actualRegister = this.find(key);
        if (actualRegister.isEmpty()){
            throw new IllegalStateException("Register not found");
        }

        Idempotency bodyValue = new Idempotency(IdempotencyStatus.COMPLETED, actualRegister.get().requestHash(), httpStatus, responseBody);
        String idempotencyKey = this.buildKey(key);
        stringRedisTemplate.opsForValue().set(idempotencyKey, jsonMapper.writeValueAsString(bodyValue), COMPLETED_TTL);
    }

    public void release(String key){
        String idempotencyKey = this.buildKey(key);
        stringRedisTemplate.delete(idempotencyKey);
    }

    private String buildKey(String key){
        return "idempotency:books:" + key;
    }
}

