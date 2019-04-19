package com.geekhalo.like.application;

import com.geekhalo.ddd.lite.domain.DomainEventBus;
import com.geekhalo.ddd.lite.domain.support.AbstractApplication;
import com.geekhalo.like.domain.Owner;
import com.geekhalo.like.domain.Target;
import com.geekhalo.like.domain.like.Like;
import com.geekhalo.like.domain.like.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LikeApplication extends AbstractApplication {
    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private DomainEventBus domainEventBus;

    public void click(Owner owner, Target target){
        syncerFor(this.likeRepository)
                .publishBy(domainEventBus)
                .loadBy(() -> this.likeRepository.getByOwnerAndTarget(owner, target))
                .instance(()-> Like.create(owner, target))
                .update(like -> like.click())
                .call();
    }

    public List<Like> getByOwnerAndTargets(Owner owner, List<Target> targets){
        return this.likeRepository.getByOwnerAndTargetIn(owner, targets);
    }
}
