package com.whkj.classin.application.atomic;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class AtomicIntegerArrayTest {
    @Test
    public void test() throws Exception{
        int[] value = new int[10];
        AtomicIntegerArray ai = new AtomicIntegerArray(value);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i=0;i<100;i ++){
            executorService.submit(()->{
                for (int j=0; j<10; j++) {
                    ai.getAndAdd(j, 1);
                    Thread.yield();
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.SECONDS);

        for (int i=0;i<10;i++){
            Assert.assertEquals(100, ai.get(i));
        }
    }
}
