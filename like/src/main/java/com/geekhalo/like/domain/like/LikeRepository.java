package com.geekhalo.like.domain.like;

import com.geekhalo.like.domain.Owner;
import com.geekhalo.like.domain.Target;

import java.util.List;

public interface LikeRepository extends BaseLikeRepository{
    List<Like> getByOwnerAndTargetIn(Owner owner, List<Target> targets);

}
