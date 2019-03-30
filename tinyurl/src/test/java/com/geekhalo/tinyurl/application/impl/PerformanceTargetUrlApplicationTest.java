package com.geekhalo.tinyurl.application.impl;

import com.geekhalo.tinyurl.application.TinyUrlApplication;
import com.google.common.base.Stopwatch;
import org.assertj.core.util.Lists;
import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PerformanceTargetUrlApplicationTest {
    @Autowired
    private Flyway flyway;

    @Autowired
    private TinyUrlApplication tinyUrlApplication;

    @Before
    public void setUp() throws Exception {
//        flyway.clean();
//        flyway.migrate();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shortUrl() throws Exception{
        int batch = 50;
        int preBatch = 10000;

        List<String> urls = Lists.newArrayList();
        Random random = new Random();
        for (int i=0;i<preBatch;i++){
            urls.add("http://tinyurl.com/" + Math.abs(random.nextInt()));
        }


//        for (int j = 0;j < 10;j++) {
//            String url = "http://tinyurl.com/" + Math.abs(random.nextInt());
//            String code = this.tinyUrlApplication.shortUrl(url);
//            String url2 = this.tinyUrlApplication.getTargetUrl(code);
//            Assert.assertEquals(url, url2);
//        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        ExecutorService executorService = Executors.newFixedThreadPool(batch);

        for (int i=0;i<batch;i++) {
            executorService.submit(()->{
                for (String url : urls){
                    this.tinyUrlApplication.shortUrl("l", url);
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.MINUTES);

        System.out.println(String.format("write %s tiny url cost %s ms, TPS is %f/s",
                preBatch * batch, stopwatch.elapsed(TimeUnit.MILLISECONDS),
                preBatch * batch * 1f / stopwatch.elapsed(TimeUnit.MILLISECONDS)  * 1000));

        TimeUnit.MINUTES.sleep(1);
    }

    @Test
    public void getByCode() throws Exception{
        int batch = 50;
        int preBatch = 1000;

        List<String> codes = Lists.newArrayList();
        for (int i=0;i< preBatch;i++){
            Random random = new Random();
            String url = "http://tinyurl.com/" + Math.abs(random.nextInt());
            String code = this.tinyUrlApplication.shortUrl("l", url);
            codes.add(code);
        }
        codes.forEach(code->this.tinyUrlApplication.getTargetUrl(code));


        Stopwatch stopwatch = Stopwatch.createStarted();
        ExecutorService executorService = Executors.newFixedThreadPool(batch);
        for (int i=0;i<batch;i++) {
            executorService.submit(()->{
                for (String code : codes){
                    this.tinyUrlApplication.getTargetUrl(code);
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
        System.out.println(String.format("get by code %s tiny url cost %s ms, TPS is %f/s",
                preBatch * batch, stopwatch.elapsed(TimeUnit.MILLISECONDS),
                preBatch * batch * 1f / stopwatch.elapsed(TimeUnit.MILLISECONDS)  * 1000));
    }

}
