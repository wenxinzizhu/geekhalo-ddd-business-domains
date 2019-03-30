package com.geekhalo.redpackage.mq;

import com.geekhalo.redpackage.Const;
import com.geekhalo.redpackage.domain.RedPackage;
import com.geekhalo.redpackage.domain.UserRedPackage;
import com.geekhalo.redpackage.repository.RedPackageRepository;
import com.geekhalo.redpackage.repository.UserRedPackageRepository;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//@Service
public class RedPackageDumpConsumer implements SmartLifecycle {
    @Autowired
    private RedisTemplate<String, UserRedPackage> userRedPackageRedisTemplate;

    @Autowired
    private UserRedPackageRepository userRedPackageRepository;

    @Autowired
    private RedPackageRepository redPackageRepository;

    private ExecutorService executorService;
    
    private int threadCount = 5;

    private boolean running = false;

    @Override
    public void start() {
        BasicThreadFactory threadFactory = new BasicThreadFactory.Builder()
                .namingPattern("user-red-package-save-thread-%d")
                .daemon(true)
                .build();
        executorService = Executors.newFixedThreadPool(threadCount, threadFactory);
        for (int i=0;i<threadCount;i++){
            executorService.submit(new DumpTask(userRedPackageRedisTemplate.boundListOps(Const.USER_RED_PACKAGE_DUMP_QUEUE)));
        }
        this.running = true;
    }

    @Override
    public void stop() {
        this.executorService.shutdown();
        try {
            this.executorService.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    class DumpTask implements Runnable{
        private final BoundListOperations<String, UserRedPackage> listOperations;

        DumpTask(BoundListOperations<String, UserRedPackage> listOperations) {
            this.listOperations = listOperations;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()){
                UserRedPackage userRedPackage = listOperations.rightPop();
                if (userRedPackage != null){
                    RedPackage redPackage = redPackageRepository.getById(userRedPackage.getActivityId());
                    redPackage.disble();
                    redPackageRepository.update(redPackage);
                    userRedPackageRepository.save(userRedPackage);
                }else {
                    try {
                        TimeUnit.MILLISECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
