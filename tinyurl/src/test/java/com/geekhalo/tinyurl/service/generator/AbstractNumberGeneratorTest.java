package com.geekhalo.tinyurl.service.generator;

import com.google.common.base.Stopwatch;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

abstract class AbstractNumberGeneratorTest {
    private static final int[] CONCURRENT_SIZE = new int[]{
            1, 2, 5, 10, 20
    };

    /**
     * 生成 Number
     * @return
     */
    abstract Long nextNumber();

    /**
     * 获取生成器名称
     * @return
     */
    abstract String getName();

    /**
     * 测试生成器所生成的数据是否唯一
     */
    @Test
    public void next() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Set<Long> ids = Sets.newTreeSet();
        for (int i=0;i <10000;i++){
            ids.add(nextNumber());
        }
        System.out.println(String.format("%s seq generate %s id cost %s ms, TPS is %f/s",
                getName().getClass().getSimpleName(),
                ids.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS),
                ids.size() * 1f / stopwatch.elapsed(TimeUnit.MILLISECONDS)  * 1000));

        // 判断是否出现重复数据
        Assert.assertEquals(10000, ids.size());
    }

    /**
     * 使用不同数量的线程对生成器进行简单压测
     * @throws Exception
     */
    @Test
    public void concurrentTest() throws Exception{
        List<String> result = Lists.newArrayList();
        for (int cSize : CONCURRENT_SIZE) {
            try {
                Stopwatch stopwatch = Stopwatch.createStarted();
                int concurrentCount = cSize;
                int preThreadCount = 10000;
                ExecutorService executorService = Executors.newFixedThreadPool(concurrentCount);
                List<Future<Set<Long>>> futures = Lists.newArrayList();
                for (int i = 0; i < concurrentCount; i++) {
                    futures.add(executorService.submit(new Task(preThreadCount)));
                }
                Set<Long> all = Sets.newHashSet();
                for (Future<Set<Long>> future : futures) {
                    all.addAll(future.get());
                }
                String line =String.format("concurrent %s generate %s id cost %s ms, TPS is %f/s",
                        cSize,
                        all.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS),
                        all.size() * 1f / stopwatch.elapsed(TimeUnit.MILLISECONDS) * 1000);
                result.add(line);
                Assert.assertEquals(concurrentCount * preThreadCount, all.size());
            }catch (Exception e){

            }
        }
        result.forEach(line->System.out.println(line));
    }

    /**
     * 生成结果集合
     */
    private class Task implements Callable<Set<Long>> {
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
