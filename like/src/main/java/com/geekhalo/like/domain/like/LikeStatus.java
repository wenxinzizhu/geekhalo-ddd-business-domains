package com.geekhalo.like.domain.like;

import com.geekhalo.ddd.lite.codegen.converter.GenCodeBasedEnumConverter;
import com.geekhalo.ddd.lite.domain.support.CodeBasedEnum;

/**
 * @author litao
 */

@GenCodeBasedEnumConverter
public enum LikeStatus implements CodeBasedEnum<LikeStatus> {
    SUBMITTED(1){
        @Override
        public void click(Like like) {
            like.cancel();
        }
    },
    CANCELLED(0){
        @Override
        public void click(Like like) {
            like.submit();
        }
    };

    private final int code;

    LikeStatus(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }

    public abstract void click(Like like);
}
