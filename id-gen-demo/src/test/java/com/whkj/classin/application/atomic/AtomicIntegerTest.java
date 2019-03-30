package com.whkj.classin.application.atomic;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIntegerTest {

    @Test
    public void test() throws Exception{
        AtomicInteger atomicInteger = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i=0;i<100;i ++){
            executorService.submit(()->{
                for (int j=0; j<100; j++) {
                    atomicInteger.incrementAndGet();
                    Thread.yield();
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.SECONDS);

        Assert.assertEquals(100 * 100, atomicInteger.get());
    }
}
