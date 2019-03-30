package com.whkj.classin.application.atomic;

import lombok.Value;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class AtomicReferenceTest {
    @Test
    public void test() throws Exception{
        AtomicReference<User> atomicReference = new AtomicReference<>(new User("name", 0));

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i=0;i<100;i ++){
            executorService.submit(()->{
                for (int j=0; j<100; j++) {
                    User expect = null;
                    User updater = null;
                    do {
                        expect = atomicReference.get();
                        updater = new User(expect.getName() + ".", expect.getCounter() + 1);
                    }while (!atomicReference.compareAndSet(expect, updater));

                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.SECONDS);

        Assert.assertEquals("name".length() + 100 * 100, atomicReference.get().getName().length());
        Assert.assertEquals(100 * 100, atomicReference.get().getCounter());
    }

    @Value
    class User{
        private String name;
        private int counter;
    }
}
