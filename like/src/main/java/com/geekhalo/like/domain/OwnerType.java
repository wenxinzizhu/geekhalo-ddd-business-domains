package com.geekhalo.like.domain;

import com.geekhalo.ddd.lite.codegen.converter.GenCodeBasedEnumConverter;
import com.geekhalo.ddd.lite.domain.support.CodeBasedEnum;
import com.geekhalo.ddd.lite.domain.support.SelfDescribedEnum;

@GenCodeBasedEnumConverter
public enum OwnerType implements CodeBasedEnum<OwnerType>, SelfDescribedEnum {
    USER(1, "普通用户");
    private final int code;
    private final String descr;

    OwnerType(int code, String descr) {
        this.code = code;
        this.descr = descr;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getDescription() {
        return this.descr;
    }
}
