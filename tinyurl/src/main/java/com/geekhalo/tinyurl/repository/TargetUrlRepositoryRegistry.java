package com.geekhalo.tinyurl.repository;

import com.geekhalo.tinyurl.repository.impl.RedisCacheBasedTargetUrlRepository;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service
public class TargetUrlRepositoryRegistry {
    private final Map<String, TargetUrlRepository> registry = Maps.newHashMap();

    /**
     * 注入 默认策略
     */
    @Autowired
    private RedisCacheBasedTargetUrlRepository defaultTargetUrlRepository;

    /**
     * 注入 ApplicationContext 环境中所有实现策略
     * @param repositories
     */
    @Autowired
    public void setTargetUrlRepository(Collection<TargetUrlRepository> repositories){
        repositories.forEach(targetUrlRepository -> {
            this.registry.put(targetUrlRepository.getStrategyName(), targetUrlRepository);
        });
    }

    /**
     * 根据策略名称，获得具体的实现策略
     * @param strategyName
     * @return
     */
    public TargetUrlRepository getRepositoryByStrategyName(String strategyName){
        TargetUrlRepository targetUrlRepository = this.registry.get(strategyName);
        if (targetUrlRepository != null){
            return targetUrlRepository;
        }
        // 如果没有找到对应策略，使用默认策略
        return defaultTargetUrlRepository;
    }
}
