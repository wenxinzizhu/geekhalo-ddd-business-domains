package com.geekhalo.idgen.application.impl;

import com.geekhalo.ddd.lite.domain.support.AbstractApplication;
import com.geekhalo.idgen.repository.NumberConfigRepository;
import com.geekhalo.idgen.application.IdGenApplication;
import com.geekhalo.idgen.domain.NumberConfig;
import com.geekhalo.idgen.domain.NumberConfigStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import java.util.Optional;

/**
 * Created by saicyc on 2018/1/6.
 */
@Service
public class IdGenApplicationImpl extends AbstractApplication implements IdGenApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(IdGenApplicationImpl.class);
    private static final int MAX_TRY_COUNT = 20;

    @Resource
    private NumberConfigRepository numberConfigRepository;

    protected IdGenApplicationImpl() {
        super(LOGGER);
    }

    @Override
    public Long getNextNumber() {
        int tryCount = 0;
        do {
            try {
                // 尝试获取nextNumber
                Long number = generateNextNumber();
                if (number != null){
                    return number;
                }
            }catch (ObjectOptimisticLockingFailureException e){
                // 乐观锁更新失败，进行重试
                LOGGER.error("opt lock failure to generate number, retry ...");
            }
        }while (tryCount++ < MAX_TRY_COUNT);
        return null;
    }

    private Long generateNextNumber(){
        /**
         *
         * 查找可用的NumberConfig
         *
         *     select
         *         numberconf0_.id as id1_0_,
         *         numberconf0_.create_time as create_t2_0_,
         *         numberconf0_.update_time as update_t3_0_,
         *         numberconf0_.version as version4_0_,
         *         numberconf0_.current_number as current_5_0_,
         *         numberconf0_.max_number as max_numb6_0_,
         *         numberconf0_.min_number as min_numb7_0_,
         *         numberconf0_.status as status8_0_
         *     from
         *         tb_number_config numberconf0_
         *     where
         *         numberconf0_.status=?
         *     order by
         *         numberconf0_.id asc limit ?
         */
        Optional<NumberConfig> virtualNumberOptional = this.numberConfigRepository.getFirstByStatus(NumberConfigStatus.NORMAL);
        if (!virtualNumberOptional.isPresent()){
            LOGGER.error("failed to get number configuration");
            return null;
        }

        NumberConfig numberConfig = virtualNumberOptional.get();

        /**
         * 调用业务方法
         */
        numberConfig.tryNextNumber();

        /**
         * 根据旧的version作为条件对记录进行更新，同时更新version的值<br />
         * 如果数据库记录没有改变，过滤条件命中，更新成功，返回nextNumber<br />
         * 如果数据库记录被更新，过滤条件未命中，更新失败，JPA抛出ObjectOptimisticLockingFailureException异常
         *      update
         *         tb_number_config
         *     set
         *         update_time=?,
         *         version=?,
         *         current_number=?,
         *         status=?
         *     where
         *         id=?
         *         and version=?
         */
        numberConfig.preUpdate();
        this.numberConfigRepository.save(numberConfig);
        return numberConfig.getNextNumber();
    }

}
