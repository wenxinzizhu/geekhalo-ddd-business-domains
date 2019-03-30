package com.geekhalo.dataimport.strategy;

import com.geekhalo.dataimport.service.UserParser;
import com.geekhalo.dataimport.repository.UserRepository;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LoadAndConcurrentSingleSave
        extends AbstractUserImportService
        implements UserImportService{

    private int concurrentSize = 20;

    private int queueSize = concurrentSize * 10;

    protected LoadAndConcurrentSingleSave(UserParser userParser, UserRepository userRepository) {
        super(userParser, userRepository);
    }


    @Override
    public void importFromFile(File file) {
        ExecutorService executorService = new ThreadPoolExecutor(concurrentSize, concurrentSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(queueSize),
                new ThreadPoolExecutor.CallerRunsPolicy());

        loadData(file, line -> {
            executorService.submit(new SingleSaveTask(line));
        });

        executorService.shutdown();
        try {
            executorService.awaitTermination(60, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
