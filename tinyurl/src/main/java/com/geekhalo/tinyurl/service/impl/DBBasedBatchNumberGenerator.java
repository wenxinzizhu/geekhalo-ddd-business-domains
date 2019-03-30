package com.geekhalo.tinyurl.service.impl;

import com.geekhalo.tinyurl.domain.NumberGen;
import com.geekhalo.tinyurl.domain.NumberType;
import com.geekhalo.tinyurl.repository.NumberGenRepository;
import com.geekhalo.tinyurl.service.NumberGenerator;
import com.geekhalo.tinyurl.service.NumberGenerator;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class DBBasedBatchNumberGenerator implements NumberGenerator {
    private static final int BATCH_SIZE = 500;
    @Autowired
    private NumberGenRepository numberGenRepository;

    private List<Long> tmp = Lists.newArrayList();

    @Override
    public Long nextNumber(NumberType type) {
        synchronized (tmp){
            if (CollectionUtils.isEmpty(tmp)){
                do {
                    try {
                        List<Long> numbers = nextNumber(type, BATCH_SIZE);
                        tmp.addAll(numbers);
                        break;
                    }catch (ObjectOptimisticLockingFailureException e){
                    }
                }while (true);
            }
            return tmp.remove(0);
        }
    }

    private List<Long> nextNumber(NumberType type, int size){
        NumberGen numberGen = this.numberGenRepository.getByType(type);
        if (numberGen == null){
            numberGen = new NumberGen(type);
        }
        List<Long> ids = numberGen.nextNumber(size);

        this.numberGenRepository.save(numberGen);
        return ids;
    }
}
