package com.whkj.classin.application;


import com.geekhalo.idgen.IdGenConfiguration;
import com.geekhalo.idgen.application.IdGenApplication;
import com.geekhalo.idgen.domain.NumberConfig;
import com.geekhalo.idgen.domain.NumberConfigStatus;
import com.geekhalo.idgen.repository.NumberConfigRepository;
import org.assertj.core.util.Sets;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toSet;

@SpringBootTest(classes = {IdGenConfiguration.class, IdGenApplicationTest.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootApplication
public class IdGenApplicationTest {

    @Autowired
    private IdGenApplication idGenApplication;

    @Autowired
    private NumberConfigRepository numberConfigRepository;

    private List<NumberConfig> numberConfigs;


    @Before
    public void setUp() throws Exception {
        // 初始化数据
        this.numberConfigs = Arrays.asList(
                createVirtualNumber( 100L, 105L, 100L),
                createVirtualNumber( 200L, 205L, 200L),
                createVirtualNumber( 500L, 1000L, 500L)
        );

        this.numberConfigs.forEach(virtualNumber -> {
            virtualNumber.prePersist();
            this.numberConfigRepository.save(virtualNumber);
        });
    }

    @After
    public void tearDown() throws Exception {
        // 清理配置
        this.numberConfigs.forEach(virtualNumber -> this.numberConfigRepository.deleteById(virtualNumber.getId()));
    }

    @Test
    public void getNextNumber() throws ExecutionException, InterruptedException {


        {   // 测试多配置支持（当前配置项配额用完后，自动切换到下个配置项）
            Set<Long> numbers = IntStream.range(0, 10)
                    .mapToObj(index -> this.idGenApplication.getNextNumber())
                    .filter(number -> number != null)
                    .collect(toSet());

            Set<Long> data = Sets.newHashSet();
            // 第一个配置项所产生的ID
            data.addAll(LongStream.rangeClosed(101, 105).mapToObj(index -> Long.valueOf(index)).collect(toSet()));
            data.addAll(LongStream.rangeClosed(201, 205).mapToObj(index -> Long.valueOf(index)).collect(toSet()));
            // 第二个配置项所产生的ID
            Assert.assertEquals(data, numbers);

        }

        {
            // 使用多线程并发访问，以验证分布式有效性
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            // 启用5个线程，并行处理，每个线程调用10次getNextNumber方法，
            List<Future<Set<Long>>> futures = IntStream.range(0, 5)
                    .mapToObj(index -> executorService.submit((Callable<Set<Long>>) () -> {
                        Set<Long> datas = Sets.newHashSet();
                        for (int i = 0; i < 10; i++) {
                            datas.add(this.idGenApplication.getNextNumber());
                            TimeUnit.MILLISECONDS.sleep(200);
                        }
                        return datas;
                    })).collect(Collectors.toList());
            Set<Long> numbers = Sets.newHashSet();
            for (Future<Set<Long>> future : futures) {
                numbers.addAll(future.get());
            }


            // 验证并发情况下产生的ID是个正确
            Assert.assertEquals(LongStream.rangeClosed(501, 550).mapToObj(index -> Long.valueOf(index)).collect(toSet()), numbers);

            // 所有配额已经用完，无法生成新的ID
            Assert.assertNotNull(idGenApplication.getNextNumber());

            // 检测是否存在可以配置
            Optional<NumberConfig> virtualNumberOptional = this.numberConfigRepository.getFirstByStatus(NumberConfigStatus.NORMAL);
            Assert.assertTrue(virtualNumberOptional.isPresent());
        }
    }

    private NumberConfig createVirtualNumber(Long min, Long max, Long current) {
        return new NumberConfig(min, max, current, NumberConfigStatus.NORMAL);
    }

}