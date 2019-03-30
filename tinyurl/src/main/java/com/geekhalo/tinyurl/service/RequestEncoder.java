package com.geekhalo.tinyurl.service;

import lombok.Value;

public interface RequestEncoder {

    /**
     * 将策略与code 进行再编码
     * @param strategy
     * @param code
     * @return
     */
    String encode(String strategy, String code);

    /**
     * 从请求参数中解密策略和code
     * @param request
     * @return
     */
    StrategyAndCode decode(String request);

    @Value
    class StrategyAndCode {
        private String strategy;
        private String code;
    }
}
