package com.tradebeyond.backend.redis;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    public Long addSetValue(String key, Object value) {
        SetOperations<String, Object> opsForSet = redisTemplate.opsForSet();
        return opsForSet.add(key, value, 1, TimeUnit.DAYS);
    }

    public Object getLatestVersion(String key) {
        ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
        return opsForValue.get(key);
    }

    public void setValue(String key, String value) {
        ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
        opsForValue.set(key, value, 1, TimeUnit.HOURS);
    }

    public Boolean isMember(String key, String member) {
        SetOperations<String, Object> opsForSet = redisTemplate.opsForSet();
        return opsForSet.isMember(key, member);
    }

    public Long cacheUserCount(DefaultRedisScript<Long> lua, String key, Integer ttl, Integer count) {
        return redisTemplate.execute(lua, Collections.singletonList(key),ttl, count);
    }

}
