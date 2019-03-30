package com.geekhalo.idgen.domain;


import com.geekhalo.ddd.lite.domain.support.jpa.JpaAggregate;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.*;

@Data
@Entity
@Table(name = "tb_number_config")
public class NumberConfig extends JpaAggregate {

    @Setter(AccessLevel.PRIVATE)
    @Column(name = "min_number", updatable = false)
    private Long mimNumber;

    @Setter(AccessLevel.PRIVATE)
    @Column(name = "max_number", updatable = false)
    private Long maxNumber;

    @Setter(AccessLevel.PRIVATE)
    @Column(name = "current_number")
    private Long currentNumber;

    @Setter(AccessLevel.PRIVATE)
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private NumberConfigStatus status;

    @Setter(AccessLevel.PRIVATE)
    @Transient
    private Long nextNumber;

    public NumberConfig(){

    }

    public NumberConfig(Long mimNumber, Long maxNumber, Long currentNumber, NumberConfigStatus status){
        setMimNumber(mimNumber);
        setMaxNumber(maxNumber);
        setCurrentNumber(currentNumber);
        setStatus(status);
    }

    public void tryNextNumber(){
        Long nextNumber = getCurrentNumber() + 1;

        if (nextNumber > getMaxNumber()){
            setStatus(NumberConfigStatus.NO_QUOTA);
        }else {
            setNextNumber(nextNumber);
            setCurrentNumber(nextNumber);
        }
    }



}