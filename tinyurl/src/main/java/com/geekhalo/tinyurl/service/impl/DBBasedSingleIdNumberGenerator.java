package com.geekhalo.tinyurl.service.impl;

import com.geekhalo.tinyurl.domain.NumberGen;
import com.geekhalo.tinyurl.domain.NumberType;
import com.geekhalo.tinyurl.repository.NumberGenRepository;
import com.geekhalo.tinyurl.service.NumberGenerator;
import com.geekhalo.tinyurl.domain.NumberGen;
import com.geekhalo.tinyurl.domain.NumberType;
import com.geekhalo.tinyurl.repository.NumberGenRepository;
import com.geekhalo.tinyurl.service.NumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
public class DBBasedSingleIdNumberGenerator implements NumberGenerator {

    @Autowired
    private NumberGenRepository numberGenRepository;

    @Override
    public Long nextNumber(NumberType type) {
        do {
            try {
                // 尝试获取nextNumber
                Long number = doNextNumber(type);
                // 保存成功，说明未发生冲突
                if (number != null){
                    return number;
                }
            }catch (ObjectOptimisticLockingFailureException e){
                // 更新失败，进行重试
//                LOGGER.error("opt lock failure to generate number, retry ...");
            }
        }while (true);
    }

    private Long doNextNumber(NumberType type){
        NumberGen numberGen = this.numberGenRepository.getByType(type);
        if (numberGen == null){
            numberGen = new NumberGen(type);
        }
        Long id = numberGen.nextNumber();
        this.numberGenRepository.save(numberGen);
        return id;
    }
}
