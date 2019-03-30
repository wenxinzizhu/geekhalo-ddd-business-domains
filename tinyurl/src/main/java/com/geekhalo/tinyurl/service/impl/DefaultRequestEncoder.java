package com.geekhalo.tinyurl.service.impl;

import com.geekhalo.tinyurl.service.RequestEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DefaultRequestEncoder implements RequestEncoder {
    @Override
    public String encode(String strategy, String code) {
        if (StringUtils.isEmpty(strategy)){
            return code;
        }else {
            return strategy + "-" + code;
        }
    }

    @Override
    public StrategyAndCode decode(String request) {
        String[] ss = request.split("-");
        if (ss.length == 0){
            return new StrategyAndCode("default", request);
        }else {
            return new StrategyAndCode(ss[0], ss[1]);
        }
    }
}
