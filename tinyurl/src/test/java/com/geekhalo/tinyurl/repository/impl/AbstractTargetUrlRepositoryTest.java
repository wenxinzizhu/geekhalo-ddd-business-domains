package com.geekhalo.tinyurl.repository.impl;

import com.geekhalo.tinyurl.domain.NumberType;
import com.geekhalo.tinyurl.domain.TargetUrl;
import com.geekhalo.tinyurl.repository.TargetUrlRepository;
import com.geekhalo.tinyurl.service.NumberGenerator;
import com.geekhalo.tinyurl.service.NumberGenerator;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import org.assertj.core.util.Lists;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

abstract class AbstractTargetUrlRepositoryTest {
    private static final int[] CON_SIZE = new int[]{
            1,2,5,10,20
    };

    @Autowired
    @Qualifier("redisBasedBatchNumberGenerator")
    private NumberGenerator numberGenerator;

    /**
     * 抽象方法，获取待测试对象
     * @return
     */
    abstract TargetUrlRepository getTinyUrlRepository();


    private NumberGenerator getNumberGenerator(){
        return this.numberGenerator;
    }

    @Test
    public void getById() throws Exception{
        TargetUrlRepository targetUrlRepository = getTinyUrlRepository();
        for (int batch : CON_SIZE) {
            int preBatch = 1000;

            // 创建测试数据
            Map<Long, TargetUrl> urls = Maps.newHashMap();
            Random random = new Random();
            for (int i = 0; i < preBatch; i++) {
                String url = "http://geekhalo.com/" + Math.abs(random.nextInt());
                TargetUrl targetUrl = TargetUrl.builder()
                        .url(url)
                        .id(this.numberGenerator.nextNumber(NumberType.TINY_URL))
                        .build();
                urls.put(targetUrl.getId(), targetUrl);
            }

            for (TargetUrl targetUrl : urls.values()) {
                targetUrlRepository.save(targetUrl);
                TargetUrl targetUrl1 = targetUrlRepository.getById(targetUrl.getId());
                Assert.assertEquals(targetUrl.getUrl(), targetUrl1.getUrl());
            }


            // 使用多线程对 getById 进行简单也成
            Stopwatch stopwatch = Stopwatch.createStarted();
            ExecutorService executorService = Executors.newFixedThreadPool(batch);
            for (int i = 0; i < batch; i++) {
                executorService.submit(() -> {
                    for (Map.Entry<Long, TargetUrl> entry : urls.entrySet()) {
                        TargetUrl targetUrl =  targetUrlRepository.getById(entry.getKey());
                        Assert.assertNotNull(targetUrl);
                        Assert.assertEquals(entry.getKey(), targetUrl.getId());
                        Assert.assertNotNull(entry.getValue().getUrl(), targetUrl.getUrl());
                    }
                });
            }
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.MINUTES);
            System.out.println(String.format("concurrent %s get by code %s tiny url cost %s ms, TPS is %f/s",
                    batch,
                    preBatch * batch, stopwatch.elapsed(TimeUnit.MILLISECONDS),
                    preBatch * batch * 1f / stopwatch.elapsed(TimeUnit.MILLISECONDS) * 1000));
        }
    }

}
