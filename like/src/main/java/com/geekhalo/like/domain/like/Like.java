package com.geekhalo.like.domain.like;

import com.geekhalo.ddd.lite.codegen.springdatarepository.GenSpringDataRepository;
import com.geekhalo.ddd.lite.domain.support.jpa.JpaAggregate;
import com.geekhalo.like.domain.Owner;
import com.geekhalo.like.domain.Target;
import com.google.common.base.Preconditions;
import com.querydsl.core.annotations.QueryEntity;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.*;

@GenSpringDataRepository
@com.geekhalo.ddd.lite.codegen.repository.Index(value = {"owner", "target"}, unique = true)

@QueryEntity
@Data
@Entity
@Table(name = "tb_like")
public class Like extends JpaAggregate {
    @Setter(AccessLevel.PRIVATE)
    @Embedded
    private Owner owner;

    @Embedded
    @Setter(AccessLevel.PRIVATE)
    private Target target;

    @Setter(AccessLevel.PRIVATE)
    @Convert(converter = CodeBasedLikeStatusConverter.class)
    private LikeStatus status;


    private Like(){

    }

    public static Like create(Owner owner, Target target){
        Preconditions.checkArgument(owner != null);
        Preconditions.checkArgument(target != null);

        Like like = new Like();
        like.setOwner(owner);
        like.setTarget(target);
        // 进行初始化操作，以构建完整的对象实例
        like.init();
        return like;
    }

    private void init(){
        setStatus(LikeStatus.CANCELLED);
    }

    public void click(){
        getStatus().click(this);
    }

    void cancel(){
        setStatus(LikeStatus.CANCELLED);
        LikeCancelledEvent likeCancelledEvent = new LikeCancelledEvent(this);
        registerEvent(likeCancelledEvent);
    }

    void submit(){
        setStatus(LikeStatus.SUBMITTED);
        LikeSubmittedEvent likeSubmittedEvent = new LikeSubmittedEvent(this);
        registerEvent(()-> likeSubmittedEvent);
    }

    public boolean isLiked() {
        return getStatus() == LikeStatus.SUBMITTED;
    }
}
