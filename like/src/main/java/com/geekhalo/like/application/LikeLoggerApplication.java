package com.geekhalo.like.application;

import com.geekhalo.ddd.lite.domain.support.AbstractApplication;
import com.geekhalo.like.domain.Owner;
import com.geekhalo.like.domain.Target;
import com.geekhalo.like.domain.logger.LikeLogger;
import com.geekhalo.like.domain.logger.LikeLoggerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LikeLoggerApplication extends AbstractApplication {

    @Autowired
    private LikeLoggerRepository repository;


    public void createLikeAction(Owner owner, Target target) {
        creatorFor(this.repository)
                .instance(()-> LikeLogger.createLikeAction(owner, target))
                .call();
    }

    public void createCancelAction(Owner owner, Target target) {
        creatorFor(this.repository)
                .instance(()-> LikeLogger.createCancelAction(owner, target))
                .call();
    }
}
