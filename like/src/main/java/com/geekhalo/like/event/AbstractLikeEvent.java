package com.geekhalo.like.event;

import com.geekhalo.like.domain.Owner;
import com.geekhalo.like.domain.Target;
import lombok.Data;

@Data
public abstract class AbstractLikeEvent {
    private Owner owner;
    private Target target;
}
