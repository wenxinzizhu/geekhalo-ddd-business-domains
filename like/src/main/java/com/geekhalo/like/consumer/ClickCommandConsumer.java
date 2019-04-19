package com.geekhalo.like.consumer;

import com.geekhalo.like.application.LikeApplication;
import com.geekhalo.like.application.OwnerAndTarget;
import com.geekhalo.like.queue.RedisBasedQueue;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ClickCommandConsumer implements SmartLifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClickCommandConsumer.class);

    @Autowired
    private LikeApplication likeApplication;

    @Autowired
    private RedisBasedQueue redisBasedQueue;

    private ExecutorService executorService;

    @Override
    public void start() {
        BasicThreadFactory basicThreadFactory = new BasicThreadFactory.Builder()
                .namingPattern("ClickCommandConsumer-Thread-%d")
                .daemon(true)
                .build();
        this.executorService = Executors.newSingleThreadExecutor(basicThreadFactory);
        this.executorService.submit(new CommandRunner());
    }

    @Override
    public void stop() {
        this.executorService.shutdown();
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    private class CommandRunner implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()){
                    OwnerAndTarget ownerAndTarget = redisBasedQueue.popClickCommand();
                    if (ownerAndTarget != null){
                        try {
                            LOGGER.info("hand click command {}", ownerAndTarget);
                            likeApplication.click(ownerAndTarget.getOwner(), ownerAndTarget.getTarget());
                        }catch (Exception e){
                            LOGGER.error("failed to handle command {}", ownerAndTarget, e);
                        }

                    }
            }
        }
    }
}
