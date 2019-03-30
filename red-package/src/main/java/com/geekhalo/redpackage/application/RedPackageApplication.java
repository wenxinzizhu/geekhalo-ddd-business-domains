package com.geekhalo.redpackage.application;

import com.geekhalo.redpackage.domain.RedPackageActivity;
import com.geekhalo.redpackage.domain.UserRedPackage;

import java.util.List;

public interface RedPackageApplication {

    /**
     * 创建红包活动
     * @param total 总金额，单位为分
     * @param size 红包总数
     * @return
     */
    RedPackageActivity createActivity(int total, int size);

    /**
     * 红包抽取
     * @param activityId
     * @param userId
     * @return
     */
    UserRedPackage draw(String activityId, String userId);

    /**
     * 获取用户的红包
     * @param userId
     * @return
     */
    List<UserRedPackage> getByUser(String userId);

    /**
     * 获取用户的红包
     * @param userId
     * @param activity
     * @return
     */
    List<UserRedPackage> getByUserAndActivity(String userId, String activity);
}
