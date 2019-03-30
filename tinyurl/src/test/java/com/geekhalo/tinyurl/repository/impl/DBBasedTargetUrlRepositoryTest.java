package com.geekhalo.tinyurl.repository.impl;

import com.geekhalo.tinyurl.repository.TargetUrlRepository;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by taoli on 2019/2/17.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class DBBasedTargetUrlRepositoryTest extends AbstractTargetUrlRepositoryTest {
    @Autowired
    private DBBasedTargetUrlRepository tinyUrlRepository;

    @Override
    TargetUrlRepository getTinyUrlRepository() {
        return tinyUrlRepository;
    }
}
