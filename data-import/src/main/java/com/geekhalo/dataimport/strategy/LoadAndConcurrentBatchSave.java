package com.geekhalo.dataimport.strategy;

import com.geekhalo.dataimport.service.UserParser;
import com.geekhalo.dataimport.repository.UserRepository;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LoadAndConcurrentBatchSave
        extends AbstractUserImportService
        implements UserImportService{

    private int concurrentSize = 10;

    private int queueSize = concurrentSize * 10;

    private int batchSize = 50000;

    protected LoadAndConcurrentBatchSave(UserParser userParser, UserRepository userRepository) {
        super(userParser, userRepository);
    }


    @Override
    public void importFromFile(File file) {
        // 构建线程池
        ExecutorService executorService = new ThreadPoolExecutor(concurrentSize, concurrentSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(queueSize),
                new ThreadPoolExecutor.CallerRunsPolicy());

        List<String> data = Lists.newLinkedList();
        loadData(file, line -> {
            data.add(line);
            if (data.size() >= batchSize){
                // 达到最大批次，向线程池中提交任务
                executorService.submit(new BatchSaveTask(data));
                data.clear();
            }
        });
        if (!data.isEmpty()){
            executorService.submit(new BatchSaveTask(data));
        }

        // 所有任务提交完成，关闭线程池
        executorService.shutdown();
        try {
            // 等待线程池中所有的任务执行完成
            executorService.awaitTermination(60, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
