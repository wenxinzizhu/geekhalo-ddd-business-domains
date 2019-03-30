package com.geekhalo.tinyurl.repository.impl;

import com.geekhalo.tinyurl.domain.TargetUrl;
import com.geekhalo.tinyurl.repository.TargetUrlRepository;
import com.geekhalo.tinyurl.domain.TargetUrl;
import com.geekhalo.tinyurl.repository.TargetUrlRepository;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component("localCacheBasedTinyUrlRepository")
public class LocalCacheBasedTargetUrlRepository implements TargetUrlRepository {
    private final LoadingCache<Long, TargetUrl> localCache;

    @Autowired
    private RedisCacheBasedTargetUrlRepository targetUrlRepository;

    LocalCacheBasedTargetUrlRepository(){
        this.localCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build(new CacheLoader<Long, TargetUrl>() {
                    @Override
                    public TargetUrl load(Long id) throws Exception {
                        return targetUrlRepository.getById(id);
                    }
                });
    }

    @Override
    public String getStrategyName() {
        return "l";
    }

    @Override
    public void save(TargetUrl targetUrl) {
        this.targetUrlRepository.save(targetUrl);
    }


    @Override
    public TargetUrl getById(Long id) {
        return this.localCache.getUnchecked(id);
    }

}
