package com.geekhalo.redpackage.domain;

import lombok.Data;

@Data
public class RedPackage {
    public static final int STATUS_ENABLE = 1;
    public static final int STATUS_DISABLE = 0;

    private String id;
    // 活动id
    private String activityId;
    // 版本，用户控制并发
    private int version;
    // 红包状态
    private int status;
    // 红包金额
    private int amount;

    /**
     * 初始化
     */
    public void init(){
        setVersion(0);
        setStatus(STATUS_ENABLE);
    }

    /**
     * 禁用，及红包失效
     */
    public void disble(){
        setStatus(STATUS_DISABLE);
    }
}
