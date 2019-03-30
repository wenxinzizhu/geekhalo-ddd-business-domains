package com.geekhalo.redpackage.application.impl;

import com.geekhalo.redpackage.application.RedPackageApplication;
import com.geekhalo.redpackage.domain.RedPackageActivity;
import com.geekhalo.redpackage.domain.UserRedPackage;
import com.geekhalo.redpackage.exception.ObjectOptimisticLockingFailureException;
import com.geekhalo.redpackage.util.IDUtils;
import com.google.common.base.Stopwatch;
import org.flywaydb.core.Flyway;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

abstract class AbstractRedPackageApplicationTest {

    // 待测试的线程并发数
    private static final int[] concurrentCount = new int[]{1, 3, 5, 10, 20, 50};

    @Autowired
    private Flyway flyway;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 测试用户 id 列表
    private List<String> userIds;

    // 测试红包活动
    private RedPackageActivity redPackageActivity;

    /**
     * 回调函数，用于获取待测试的 RedPackageApplication 实现
     * @return
     */
    protected abstract RedPackageApplication getRedPackageApplication();

    /**
     * 每轮测试前回调
     */
    protected void preTest(){

    }

    /**
     * 每轮测试后回调
     */
    protected void postTest(){

    }

    @Test
    public void draw() throws Exception{
        // 针对不同的线程数进行性能测试
        for (int cCount : concurrentCount){
            // 测试前回调
            preTest();
            // 清理数据
            cleanData();
            // 准备测试数据
            prepareData(cCount);
            // 进行抢红包测试
            doDraw(cCount);
            // 测试后回调
            postTest();
        }

    }

    /**
     * 清理数据，以避免测试间的相互影响
     */
    private void cleanData(){
        // 删除 redis 中所有数据
        stringRedisTemplate.delete(stringRedisTemplate.keys("*"));

        // 清理数据表
        flyway.clean();
        // 重建数据库表
        flyway.migrate();
    }

    /**
     * 准备测试数据
     * @param cCount 并发线程数
     */
    private void prepareData(int cCount){
        Stopwatch stopwatch = Stopwatch.createStarted();
        int total = 50 * 10000;// 总金额
        int size = 10 * 10000;// 总红包数
        // 创建红包活动
        this.redPackageActivity = this.getRedPackageApplication().createActivity(total, size);
        stopwatch.stop();
//        System.out.println(String.format("create Activity and save %s red package cost %s s, TPS is %s/s",
//                size,
//                stopwatch.elapsed(TimeUnit.SECONDS),
//                size * 1f / stopwatch.elapsed(TimeUnit.MILLISECONDS)  * 1000));
        // 创建用户 id 列表
        this.userIds = new ArrayList<>(10000);
        for (int i=0;i<10000;i++){
            this.userIds.add(IDUtils.genId());
        }
    }

    /**
     * 抢红包测试
     * @param cCount 并发线程数
     * @throws InterruptedException
     */
    private void doDraw(int cCount) throws InterruptedException {
        int threadCount = cCount;
        Stopwatch stopwatch = Stopwatch.createStarted();
        // 通过线程池，构建测试线程
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger atomicInteger = new AtomicInteger(0);
        for (int i=0;i<threadCount;i++) {
            executorService.submit(()->{
                // 每个线程不停的抢红包，直到没有红包为止
                while (true) {
                    try {
                        // 抢红包
                        UserRedPackage userRedPackage = this.getRedPackageApplication().draw(this.redPackageActivity.getId(), randomUser());
                        if (userRedPackage == null) {
                            // 已经没有红包，终止循环，线程退出
                            break;
                        }
                        // 成功抢到红包，增加红包总数
                        atomicInteger.incrementAndGet();
                    }catch (ObjectOptimisticLockingFailureException e){

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });
        }
        // 关闭线程池，等待所有线程的退出
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.MINUTES);
        stopwatch.stop();

        // 打印测试结果
        System.out.println(String.format("concurrent %s draw %s red package cost %s s, TPS is %s/s",
                cCount,
                atomicInteger.get(),
                stopwatch.elapsed(TimeUnit.SECONDS),
                atomicInteger.get() * 1f / stopwatch.elapsed(TimeUnit.MILLISECONDS)  * 1000));
    }

    private String randomUser() {
        return this.userIds.get(Math.abs(new Random().nextInt() % this.userIds.size()));
    }
}
