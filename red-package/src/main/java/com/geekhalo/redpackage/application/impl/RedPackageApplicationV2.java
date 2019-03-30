package com.geekhalo.redpackage.application.impl;

import com.geekhalo.redpackage.application.RedPackageApplication;
import com.geekhalo.redpackage.domain.RedPackage;
import com.geekhalo.redpackage.domain.RedPackageActivity;
import com.geekhalo.redpackage.domain.UserRedPackage;
import com.geekhalo.redpackage.repository.RedPackageActivityRepository;
import com.geekhalo.redpackage.repository.RedPackageRepository;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;

@Service
public class RedPackageApplicationV2
        extends AbstractRedPackageAppliction
        implements RedPackageApplication {

    @Autowired
    private RedPackageActivityRepository activityRepository;

    @Autowired
    private RedPackageRepository redPackageRepository;




    @Override
    @Transactional
    public RedPackageActivity createActivity(int total, int size) {
        // 创建红包活动
        RedPackageActivity redPackageActivity = new RedPackageActivity();
        redPackageActivity.init(total, size);

        // 生成并保存 RedPackage
        while (redPackageActivity.hasBalance()){
            RedPackageCollector collector = new RedPackageCollector();
            redPackageActivity.draw(5000, collector);
            this.redPackageRepository.save(collector.redPackages);
        }

        // 保存红包活动
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
        // 获取可以红包
        RedPackage redPackage = this.redPackageRepository.getEnableByActivity(activityId);
        // 没有可用红包
        if (redPackage == null){
            return null;
        }

        // 保存红包结果
        UserRedPackage userRedPackage = new UserRedPackage();
        userRedPackage.setActivityId(activityId);
        userRedPackage.setUserId(userId);
        userRedPackage.setRedPackageId(redPackage.getId());
        userRedPackage.setAmount(redPackage.getAmount());
        this.userRedPackageRepository.save(userRedPackage);

        // 更新 RedPackage 状态，同时进行并发控制
        redPackage.disble();
        this.redPackageRepository.update(redPackage);

        return userRedPackage;
    }
}
