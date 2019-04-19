package com.geekhalo.like.consumer;

import com.geekhalo.like.application.LikeLoggerApplication;
import com.geekhalo.like.application.TargetCountApplication;
import com.geekhalo.like.domain.logger.LikeLogger;
import com.geekhalo.like.event.AbstractLikeEvent;
import com.geekhalo.like.event.CanceledEvent;
import com.geekhalo.like.event.SubmittedEvent;
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
public class LikeEventConsumer implements SmartLifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(LikeEventConsumer.class);

    @Autowired
    private LikeLoggerApplication likeLoggerApplication;

    @Autowired
    private TargetCountApplication targetCountApplication;

    @Autowired
    private RedisBasedQueue redisBasedQueue;

    private ExecutorService executorService;

    @Override
    public void start() {
        BasicThreadFactory basicThreadFactory = new BasicThreadFactory.Builder()
                .namingPattern("LiekEventConsumer-Thread-%d")
                .daemon(true)
                .build();
        executorService = Executors.newSingleThreadExecutor(basicThreadFactory);
        executorService.submit(new DispatcherTask());
    }

    @Override
    public void stop() {
        executorService.shutdown();
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    private class DispatcherTask implements Runnable{

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()){
                AbstractLikeEvent abstractEvent = redisBasedQueue.popLikeEvent();
                if (abstractEvent != null){
                    try {
                        LOGGER.info("handle like event {}", abstractEvent);
                        if (abstractEvent instanceof SubmittedEvent){
                            handle((SubmittedEvent) abstractEvent);
                        }
                        if (abstractEvent instanceof CanceledEvent){
                            handle((CanceledEvent) abstractEvent);
                        }
                    }catch (Exception e){
                        LOGGER.error("failed to handle event {}", abstractEvent, e);
                    }
                }
            }
        }
    }

    private void handle(SubmittedEvent submittedEvent){
        saveLogger(submittedEvent);
        updateCount(submittedEvent);
    }

    private void handle(CanceledEvent canceledEvent){
        saveLogger(canceledEvent);
        updateCount(canceledEvent);
    }

    private void saveLogger(SubmittedEvent submittedEvent){
        this.likeLoggerApplication.createLikeAction(submittedEvent.getOwner(), submittedEvent.getTarget());
    }


    private void saveLogger(CanceledEvent canceledEvent){
        this.likeLoggerApplication.createCancelAction(canceledEvent.getOwner(), canceledEvent.getTarget());
    }

    private void updateCount(SubmittedEvent submittedEvent){
        this.targetCountApplication.incr(submittedEvent.getTarget(), 1);
    }



    private void updateCount(CanceledEvent canceledEvent){
        this.targetCountApplication.decr(canceledEvent.getTarget(), 1);
    }
}
