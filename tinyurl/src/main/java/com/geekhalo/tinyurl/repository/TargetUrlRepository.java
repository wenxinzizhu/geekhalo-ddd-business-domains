package com.geekhalo.tinyurl.repository;

import com.geekhalo.tinyurl.domain.TargetUrl;

public interface TargetUrlRepository {
    /**
     * 获取策略名称
     * @return
     */
    String getStrategyName();

    /**
     * 添加链接
     * @param targetUrl
     */
    void save(TargetUrl targetUrl);


    /**
     * 获取连接
     * @param id
     * @return
     */
    TargetUrl getById(Long id);
}
