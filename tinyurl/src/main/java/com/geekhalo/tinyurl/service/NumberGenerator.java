package com.geekhalo.tinyurl.service;

import com.geekhalo.tinyurl.domain.NumberType;

public interface NumberGenerator {
    /**
     * 生成自增 Key
     * @return
     */
    Long nextNumber(NumberType type);
}
