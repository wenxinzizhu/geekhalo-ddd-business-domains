package com.geekhalo.tinyurl.repository.impl;

import com.geekhalo.tinyurl.domain.TargetUrl;
import com.geekhalo.tinyurl.repository.TargetUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component("redisCacheBasedTinyUrlRepository")
public class RedisCacheBasedTargetUrlRepository implements TargetUrlRepository {
    @Autowired
    private RedisTemplate<Long, TargetUrl> redisTemplate;

    @Autowired
    private DBBasedTargetUrlRepository targetUrlRepository;


    @Override
    public String getStrategyName() {
        return "r";
    }

    @Override
    public void save(TargetUrl targetUrl) {
        this.targetUrlRepository.save(targetUrl);
    }


    @Override
    public TargetUrl getById(Long id) {
        return getFromCache(id);
    }

    private TargetUrl getFromCache(Long id){
        // 从缓存中获取
        TargetUrl targetUrl = redisTemplate.opsForValue().get(id);
        if (targetUrl == null){
            // 缓存未命中，从 DB 中获取
            targetUrl = this.targetUrlRepository.getById(id);
            if (targetUrl != null){
                // 将获取的数据存入存储中
                redisTemplate.opsForValue().set(id, targetUrl);
            }
        }
        return targetUrl;
    }
}
