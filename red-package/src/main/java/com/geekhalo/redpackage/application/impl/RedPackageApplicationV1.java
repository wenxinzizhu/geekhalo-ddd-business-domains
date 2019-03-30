package com.geekhalo.redpackage.application.impl;

import com.geekhalo.redpackage.application.RedPackageApplication;
import com.geekhalo.redpackage.domain.RedPackage;
import com.geekhalo.redpackage.domain.RedPackageActivity;
import com.geekhalo.redpackage.domain.UserRedPackage;
import com.geekhalo.redpackage.repository.RedPackageActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

@Service
public class RedPackageApplicationV1
        extends AbstractRedPackageAppliction
        implements RedPackageApplication {

    @Autowired
    private RedPackageActivityRepository activityRepository;


    @Override
    @Transactional
    public RedPackageActivity createActivity(int total, int size) {
        RedPackageActivity redPackageActivity = new RedPackageActivity();
        redPackageActivity.init(total, size);
        this.activityRepository.save(redPackageActivity);
        return redPackageActivity;
    }

    @Override
    @Transactional
    public UserRedPackage draw(String activityId, String userId) {
        RedPackageActivity redPackageActivity = this.activityRepository.getById(activityId);
        // 如果红包不存在，获取红包没有余额，直接返回 null，表示没有抢到红包
        if (redPackageActivity == null || !redPackageActivity.hasBalance()){
            return null;
        }

        // 从 RedPackageActivity 抽取红包
        UserRedPackageSaveTask userRedPackageSaveTask = new UserRedPackageSaveTask(activityId, userId);
        redPackageActivity.draw(1, userRedPackageSaveTask);

        // 更新 RedPackageActivity
        this.activityRepository.update(redPackageActivity);
        // 保存 UserRedPackage
        userRedPackageRepository.save(userRedPackageSaveTask.userRedPackage);
        return userRedPackageSaveTask.userRedPackage;
    }

    class UserRedPackageSaveTask implements Consumer<RedPackage> {
        private final String activityId;
        private final String userId;
        private transient UserRedPackage userRedPackage;

        UserRedPackageSaveTask(String activityId, String userId) {
            this.activityId = activityId;
            this.userId = userId;
        }

        @Override
        public void accept(RedPackage redPackage) {
            this.userRedPackage = new UserRedPackage();
            this.userRedPackage.setUserId(this.userId);
            this.userRedPackage.setActivityId(this.activityId);
            this.userRedPackage.setRedPackageId("none");
            this.userRedPackage.setAmount(redPackage.getAmount());
        }
    }

}
