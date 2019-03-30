package com.geekhalo.tinyurl.service;

import com.geekhalo.tinyurl.domain.NumberType;
import com.geekhalo.tinyurl.service.impl.DBBasedSingleIdNumberGenerator;
import com.google.common.base.Stopwatch;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.flywaydb.core.Flyway;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SingleIdNumberGeneratorTest1 {

    @Autowired
    private Flyway flyway;

    @Autowired
    private DBBasedSingleIdNumberGenerator application;

    @Before
    public void setUp() throws Exception {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void next() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Set<Long> ids = Sets.newTreeSet();
        for (int i=0;i <1000;i++){
            ids.add(this.application.nextNumber(NumberType.TINY_URL));
        }
        System.out.println(String.format("%s seq generate %s id cost %s ms, TPS is %f/s",
                application.getClass().getSimpleName(),
                ids.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS),
                ids.size() * 1f / stopwatch.elapsed(TimeUnit.MILLISECONDS)  * 1000));

        Assert.assertEquals(1000, ids.size());
    }

    @Test
    public void concurrentTest() throws Exception{
        Stopwatch stopwatch = Stopwatch.createStarted();
        int concurrentCount = 10;
        int preThreadCount = 10000;
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentCount);
        List<Future<Set<Long>>> futures = Lists.newArrayList();
        for (int i=0;i<concurrentCount;i++){
            futures.add(executorService.submit(new Task(preThreadCount)));
        }
        Set<Long> all = Sets.newHashSet();
        for (Future<Set<Long>> future : futures){
            all.addAll(future.get());
        }
        System.out.println(String.format("%s concurrent generate %s id cost %s ms, TPS is %f/s",
                application.getClass().getSimpleName(),
                all.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS),
                all.size() * 1f / stopwatch.elapsed(TimeUnit.MILLISECONDS)  * 1000));
        Assert.assertEquals(concurrentCount * preThreadCount, all.size());

    }

    private Long nextNumber(){
        return this.application.nextNumber(NumberType.TINY_URL);
    }

    private class Task implements Callable<Set<Long>>{
        private final int count;

        private Task(int count) {
            this.count = count;
        }

        @Override
        public Set<Long> call() throws Exception {
            Set<Long> result = Sets.newTreeSet();
            for (int i=0; i< this.count; i++){
                result.add(nextNumber());
            }
            return result;
        }
    }
}