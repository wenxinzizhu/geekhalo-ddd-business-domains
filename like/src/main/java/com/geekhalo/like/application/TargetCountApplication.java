package com.geekhalo.like.application;

import com.geekhalo.ddd.lite.domain.support.AbstractApplication;
import com.geekhalo.like.domain.Target;
import com.geekhalo.like.domain.count.TargetCount;
import com.geekhalo.like.domain.count.TargetCountRepository;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TargetCountApplication extends AbstractApplication {
    @Autowired
    private TargetCountRepository targetCountRepository;

    @Autowired
    private RedisTemplate<Target, TargetCount> targetCountRedisTemplate;

    public void incr(Target target, int by){
        this.syncerFor(this.targetCountRepository)
                .loadBy(() -> this.targetCountRepository.getByTarget(target))
                .instance(() -> TargetCount.create(target))
                .update(targetCount -> targetCount.incr(by))
                .call();
        this.targetCountRedisTemplate.delete(target);
    }

    public void decr(Target target, int by){
        this.syncerFor(this.targetCountRepository)
                .loadBy(()-> this.targetCountRepository.getByTarget(target))
                .instance(()-> TargetCount.create(target))
                .update(targetCount -> targetCount.decr(by))
                .call();
        this.targetCountRedisTemplate.delete(target);
    }

    public List<TargetCount> countOfTargets(List<Target> targets){
        List<TargetCount> result = Lists.newArrayList();
        List<TargetCount> dataFromCache = this.targetCountRedisTemplate.opsForValue().multiGet(targets);
        for (int i= 0; i< targets.size();i++){
            TargetCount targetCount = dataFromCache.get(i);
            if (targetCount == null){
                Target target = targets.get(i);
                Optional<TargetCount> targetCountOptional = this.targetCountRepository.getByTarget(target);
                if (targetCountOptional.isPresent()){
                    this.targetCountRedisTemplate.opsForValue().set(target, targetCountOptional.get());
                    targetCount = targetCountOptional.get();
                }

            }
            result.add(targetCount);
        }
        return result;
    }
}
