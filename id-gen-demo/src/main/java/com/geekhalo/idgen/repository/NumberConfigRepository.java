package com.geekhalo.idgen.repository;


import com.geekhalo.ddd.lite.domain.AggregateRepository;
import com.geekhalo.idgen.domain.NumberConfig;
import com.geekhalo.idgen.domain.NumberConfigStatus;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/**
 * Created by Administrator on 2018/1/24 0024.
 */
public interface NumberConfigRepository extends AggregateRepository<Long, NumberConfig>, Repository<NumberConfig, Long> {

    Optional<NumberConfig> getFirstByStatus(NumberConfigStatus status);
}
