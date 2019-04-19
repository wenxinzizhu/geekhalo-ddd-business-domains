package com.geekhalo.like.domain;

import com.geekhalo.ddd.lite.codegen.converter.GenCodeBasedEnumConverter;
import com.geekhalo.ddd.lite.domain.support.CodeBasedEnum;
import com.geekhalo.ddd.lite.domain.support.SelfDescribedEnum;

@GenCodeBasedEnumConverter
public enum TargetType implements CodeBasedEnum<TargetType>, SelfDescribedEnum {
    NEWS(1, "新闻");
    private final int code;
    private final String desc;

    TargetType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return desc;
    }
}
