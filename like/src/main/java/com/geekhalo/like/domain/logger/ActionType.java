package com.geekhalo.like.domain.logger;

import com.geekhalo.ddd.lite.codegen.converter.GenCodeBasedEnumConverter;
import com.geekhalo.ddd.lite.domain.support.CodeBasedEnum;

@GenCodeBasedEnumConverter
public enum ActionType implements CodeBasedEnum<ActionType> {
    LIKE(1), CANCEL(0);

    private final int code;

    ActionType(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return this.code;
    }
}
