package com.geekhalo.tinyurl.service.generator;

import com.geekhalo.tinyurl.domain.NumberType;
import com.geekhalo.tinyurl.service.impl.RedisBasedBatchNumberGenerator;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisBasedBatchNumberGeneratorTest extends AbstractNumberGeneratorTest {
    @Autowired
    private RedisBasedBatchNumberGenerator batchIdGenerator;

    @Override
    Long nextNumber() {
        return batchIdGenerator.nextNumber(NumberType.TINY_URL);
    }

    @Override
    String getName() {
        return "RedisBasedBatchNumberGenerator";
    }
}