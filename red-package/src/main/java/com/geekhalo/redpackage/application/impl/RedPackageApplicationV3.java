package com.geekhalo.redpackage.application.impl;

import com.geekhalo.redpackage.application.RedPackageApplication;
import com.geekhalo.redpackage.domain.RedPackage;
import com.geekhalo.redpackage.domain.RedPackageActivity;
import com.geekhalo.redpackage.domain.UserRedPackage;
import com.geekhalo.redpackage.repository.RedPackageActivityRepository;
import com.geekhalo.redpackage.repository.RedPackageRepository;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class RedPackageApplicationV3
        extends AbstractRedPackageAppliction
        implements RedPackageApplication {

    private static final String QUEUE_KEY = "red.package.queue.v3.%s";

    @Autowired
    private RedisTemplate<String, RedPackage> redisTemplate;

    @Autowired
    private RedPackageActivityRepository activityRepository;

    @Autowired
    private RedPackageRepository redPackageRepository;




    @Override
    @Transactional
    public RedPackageActivity createActivity(int total, int size) {
        RedPackageActivity redPackageActivity = new RedPackageActivity();
        redPackageActivity.init(total, size);

        String queueKey = String.format(QUEUE_KEY, redPackageActivity.getId());

        while (redPackageActivity.hasBalance()){
            RedPackageCollector collector = new RedPackageCollector();
            redPackageActivity.draw(5000, collector);
            List<RedPackage> redPackages = collector.redPackages.stream()
                    .filter(redPackage -> redPackage != null)
                    .collect(Collectors.toList());
            this.redPackageRepository.save(redPackages);
            // 将 RedPackage 添加到 Redis 的 List 中
            this.redisTemplate.boundListOps(queueKey).rightPushAll(redPackages.toArray(new RedPackage[redPackages.size()]));
        }

        this.activityRepository.save(redPackageActivity);

        return redPackageActivity;
    }

    class RedPackageCollector implements Consumer<RedPackage>{
        private final List<RedPackage> redPackages = Lists.newArrayList();
        @Override
        public void accept(RedPackage redPackage) {
            if (redPackage != null) {
                redPackages.add(redPackage);
            }
        }
    }

    @Override
    @Transactional
    public UserRedPackage draw(String activityId, String userId) {
        String queueKey = String.format(QUEUE_KEY, activityId);
        // 从 Redis 的 List 中获取红包
        RedPackage redPackage = this.redisTemplate.boundListOps(queueKey).leftPop();
        if (redPackage == null){
            return null;
        }

        UserRedPackage userRedPackage = new UserRedPackage();
        userRedPackage.setActivityId(activityId);
        userRedPackage.setUserId(userId);
        userRedPackage.setRedPackageId(redPackage.getId());
        userRedPackage.setAmount(redPackage.getAmount());
        this.userRedPackageRepository.save(userRedPackage);

        redPackage.disble();
        this.redPackageRepository.update(redPackage);

        return userRedPackage;
    }
}
