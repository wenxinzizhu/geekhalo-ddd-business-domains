package com.whkj.classin.application.atomic;

import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class AtomicIntegerFieldUpdaterTest {

    private static final AtomicIntegerFieldUpdater<User> COUNTER_UPDATER = AtomicIntegerFieldUpdater
            .newUpdater(User.class, "counter");

    @Test
    public void test() throws Exception{
        User user = new User("test", 0);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i=0;i<100;i ++){
            executorService.submit(()->{
                for (int j=0; j<100; j++) {
                    COUNTER_UPDATER.getAndAdd(user, 1);
                    Thread.yield();
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.SECONDS);


        Assert.assertEquals(100 * 100, user.getCounter());
    }


    @Data
    class User {
        private String name;
        public volatile int counter;

        public User(String name, int counter) {
            this.name = name;
            this.counter = counter;
        }
    }
}
