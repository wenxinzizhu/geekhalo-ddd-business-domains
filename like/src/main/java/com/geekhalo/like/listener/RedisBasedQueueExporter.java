package com.geekhalo.like.listener;

import com.geekhalo.like.domain.like.LikeCancelledEvent;
import com.geekhalo.like.domain.like.LikeSubmittedEvent;
import com.geekhalo.like.event.CanceledEvent;
import com.geekhalo.like.event.SubmittedEvent;
import com.geekhalo.like.queue.RedisBasedQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RedisBasedQueueExporter {
    @Autowired
    private RedisBasedQueue redisBasedQueue;

    @EventListener
    public void handle(LikeSubmittedEvent likeSubmittedEvent){
        SubmittedEvent submittedEvent = new SubmittedEvent();
        submittedEvent.setOwner(likeSubmittedEvent.getSource().getOwner());
        submittedEvent.setTarget(likeSubmittedEvent.getSource().getTarget());
        this.redisBasedQueue.pushLikeEvent(submittedEvent);
    }


    @EventListener
    public void handle(LikeCancelledEvent likeCancelledEvent){
        CanceledEvent canceledEvent = new CanceledEvent();
        canceledEvent.setOwner(likeCancelledEvent.getSource().getOwner());
        canceledEvent.setTarget(likeCancelledEvent.getSource().getTarget());
        this.redisBasedQueue.pushLikeEvent(canceledEvent);
    }

}
