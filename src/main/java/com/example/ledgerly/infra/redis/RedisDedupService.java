package com.example.ledgerly.infra.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisDedupService {

    private final StringRedisTemplate redis;
    private final Duration ttl;

    public RedisDedupService(StringRedisTemplate redis, @Value("${ledgerly.dedup.redisTtlSeconds}") long ttlSeconds) {
        this.redis = redis;
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    public boolean firstTime(String consumer, String eventId) {
        String key = "dedup:" + consumer + ":" + eventId;
        Boolean ok = redis.opsForValue().setIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(ok);
    }
}
