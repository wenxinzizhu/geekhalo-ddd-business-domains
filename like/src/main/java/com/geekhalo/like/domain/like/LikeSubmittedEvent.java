package com.geekhalo.like.domain.like;

import com.geekhalo.ddd.lite.domain.support.AbstractAggregateEvent;
import lombok.Value;

@Value
public class LikeSubmittedEvent extends AbstractAggregateEvent<Long, Like> {
    public LikeSubmittedEvent(Like source) {
        super(source);
    }

    public LikeSubmittedEvent(String id, Like source) {
        super(id, source);
    }
}
