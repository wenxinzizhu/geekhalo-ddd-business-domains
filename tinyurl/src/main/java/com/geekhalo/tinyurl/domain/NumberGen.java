package com.geekhalo.tinyurl.domain;


import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "tb_number_gen")
public class NumberGen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 生成器类型
     */
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private NumberType type;

    /**
     * version 字段，用于乐观锁控制
     */
    @Version
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private long version;

    /**
     * 当前 number
     */
    @Setter(AccessLevel.PRIVATE)
    @Column(name = "current_number")
    private Long currentNumber;


    public NumberGen(){

    }

    public NumberGen(NumberType type){
        this.setType(type);
        setCurrentNumber(0L);
    }

    public Long nextNumber(){
        return ++currentNumber;
    }

    public List<Long> nextNumber(int size){
        List<Long> numbers = new ArrayList<>(size);
        for (int i=0;i<size;i++){
            numbers.add(nextNumber());
        }
        return numbers;
    }

}