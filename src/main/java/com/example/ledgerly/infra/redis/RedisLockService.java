package com.example.ledgerly.infra.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

// AWS note (design-only):
// Redis here maps to ElastiCache. For strict safety, use a Lua script unlock.
// For cross-region, consider DynamoDB-based locks (depending on latency/consistency needs).

@Service
public class RedisLockService {

    private final StringRedisTemplate redis;
    private final Duration ttl;

    public RedisLockService(StringRedisTemplate redis, @Value("${ledgerly.lock.redisTtlMillis}") long ttlMillis) {
        this.redis = redis;
        this.ttl = Duration.ofMillis(ttlMillis);
    }

    public String tryLock(String lockKey) {
        String token = UUID.randomUUID().toString();
        Boolean ok = redis.opsForValue().setIfAbsent("lock:" + lockKey, token, ttl);
        return Boolean.TRUE.equals(ok) ? token : null;
    }

    public void unlock(String lockKey, String token) {
        // Simplified unlock (demo). In production you'd Lua-check token before delete.
        String key = "lock:" + lockKey;
        String cur = redis.opsForValue().get(key);
        if (token != null && token.equals(cur)) {
            redis.delete(key);
        }
    }
}
