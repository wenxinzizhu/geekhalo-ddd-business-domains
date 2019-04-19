package com.geekhalo.like.domain.count;

import com.geekhalo.like.domain.Target;

import java.util.List;

public interface TargetCountRepository extends BaseTargetCountRepository{
    List<TargetCount> getByTargetIn(List<Target> targets);
}
