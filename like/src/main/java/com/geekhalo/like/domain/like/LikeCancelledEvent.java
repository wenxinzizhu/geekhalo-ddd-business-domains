package com.geekhalo.like.domain.like;

import com.geekhalo.ddd.lite.domain.support.AbstractAggregateEvent;
import lombok.Value;

@Value
public class LikeCancelledEvent extends AbstractAggregateEvent<Long, Like> {
    public LikeCancelledEvent(Like source) {
        super(source);
    }
}
