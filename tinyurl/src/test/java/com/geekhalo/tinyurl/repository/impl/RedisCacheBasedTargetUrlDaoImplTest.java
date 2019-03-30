package com.geekhalo.tinyurl.repository.impl;

import com.geekhalo.tinyurl.domain.NumberType;
import com.geekhalo.tinyurl.domain.TargetUrl;
import com.geekhalo.tinyurl.service.impl.DBBasedBatchNumberGenerator;
import com.geekhalo.tinyurl.service.impl.DBBasedBatchNumberGenerator;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;


@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisCacheBasedTargetUrlDaoImplTest {
    @Autowired
    private RedisCacheBasedTargetUrlRepository cachedTinyUrlRepository;

    @Autowired
    private DBBasedBatchNumberGenerator idGenApplication;

    private List<TargetUrl> datas;

    @Before
    public void setUp() throws Exception {
        this.datas = Lists.newLinkedList();
        Random random = new Random();
        for (int i=0;i<100;i++){
            TargetUrl targetUrl = TargetUrl.builder()
                    .id(this.idGenApplication.nextNumber(NumberType.TINY_URL))
                    .url("http://tinyurl.com/" + Math.abs(random.nextLong()))
                    .build();
            this.cachedTinyUrlRepository.save(targetUrl);
            this.datas.add(targetUrl);
        }
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void save() {
    }

    @Test
    public void getById() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (int i=0;i<100000;i++){
            this.datas.forEach(tinyUrl -> {
                TargetUrl targetUrl1 = cachedTinyUrlRepository.getById(tinyUrl.getId());
                Assert.assertEquals(tinyUrl.getUrl(), targetUrl1.getUrl());
            });
        }
        System.out.println(String.format("%s get %s tiny url cost %s ms, TPS is %f/s",
                RedisCacheBasedTargetUrlRepository.class.getSimpleName(),
                100000 * datas.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS),
                100000 * datas.size() * 1f / stopwatch.elapsed(TimeUnit.MILLISECONDS)  * 1000));
    }
}