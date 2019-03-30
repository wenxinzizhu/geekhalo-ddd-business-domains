package com.geekhalo.tinyurl.service.impl;

import com.geekhalo.tinyurl.domain.NumberType;
import com.geekhalo.tinyurl.service.NumberGenerator;
import com.geekhalo.tinyurl.service.NumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisBasedSingleIdNumberGenerator implements NumberGenerator {
    private static final String ID_GEN_KEY = "number.%s.gen";
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public Long nextNumber(NumberType type) {
        String key = String.format(ID_GEN_KEY, type.toString().toLowerCase());
        return stringRedisTemplate.opsForValue().increment(key);
    }
}
