package com.geekhalo.redpackage.domain;

import lombok.Data;

@Data
public class UserRedPackage {
    // 用户id
    private String userId;
    // 活动id
    private String activityId;
    // 红包id
    private String redPackageId;
    // 红包金额
    private int amount;
}
