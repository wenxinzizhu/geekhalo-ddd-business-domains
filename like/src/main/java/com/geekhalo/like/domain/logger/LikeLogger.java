package com.geekhalo.like.domain.logger;

import com.geekhalo.ddd.lite.codegen.springdatarepository.GenSpringDataRepository;
import com.geekhalo.ddd.lite.domain.support.jpa.JpaAggregate;
import com.geekhalo.like.domain.Owner;
import com.geekhalo.like.domain.Target;
import com.querydsl.core.annotations.QueryEntity;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

@GenSpringDataRepository

@QueryEntity
@Data
@Entity
@Table(name = "tb_like_logger")
public class LikeLogger extends JpaAggregate {
    @Setter(AccessLevel.PRIVATE)
    @Embedded
    private Owner owner;

    @Setter(AccessLevel.PRIVATE)
    @Embedded
    private Target target;

    @Convert(converter = CodeBasedActionTypeConverter.class)
    @Setter(AccessLevel.PRIVATE)
    private ActionType actionType;

    private LikeLogger(){

    }

    public static LikeLogger createLikeAction(Owner owner, Target target){
        LikeLogger likeLogger = new LikeLogger();
        likeLogger.setActionType(ActionType.LIKE);
        likeLogger.setOwner(owner);
        likeLogger.setTarget(target);
        return likeLogger;
    }

    public static LikeLogger createCancelAction(Owner owner, Target target){
        LikeLogger likeLogger = new LikeLogger();
        likeLogger.setActionType(ActionType.CANCEL);
        likeLogger.setOwner(owner);
        likeLogger.setTarget(target);
        return likeLogger;
    }
}
