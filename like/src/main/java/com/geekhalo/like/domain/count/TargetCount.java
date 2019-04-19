package com.geekhalo.like.domain.count;

import com.geekhalo.ddd.lite.codegen.repository.Index;
import com.geekhalo.ddd.lite.codegen.springdatarepository.GenSpringDataRepository;
import com.geekhalo.ddd.lite.domain.support.jpa.JpaAggregate;
import com.geekhalo.like.domain.Target;
import com.querydsl.core.annotations.QueryEntity;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

@GenSpringDataRepository
@Index(value = "target", unique = true)

@QueryEntity
@Data
@Entity
@Table(name = "tb_target_count")
public class TargetCount extends JpaAggregate {
    @Setter(AccessLevel.PRIVATE)
    @Embedded
    private Target target;

    @Setter(AccessLevel.PRIVATE)
    private Long count;

    private TargetCount(){

    }

    public static TargetCount create(Target target){
        TargetCount count = new TargetCount();
        count.setTarget(target);
        count.init();
        return count;
    }

    private void init(){
        setCount(0L);
    }

    public void incr(int by){
        setCount(getCount() + by);
    }

    public void decr(int by){
        setCount(getCount() - by);
    }

    private void setCount(Long count){
        if (count >= 0){
            this.count = count;
        }else {
            this.count = 0L;
        }
    }
}
