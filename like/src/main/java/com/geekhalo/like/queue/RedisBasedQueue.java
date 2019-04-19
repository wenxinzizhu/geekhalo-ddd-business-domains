package com.geekhalo.like.queue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.geekhalo.like.application.OwnerAndTarget;
import com.geekhalo.like.event.AbstractLikeEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisBasedQueue {
    private static final String COMMAND_CLICK = "queue.command.click";
    private static final String EVENT_LIKE = "queue.event.like";
    @Autowired
    public RedisTemplate<String, String> redisTemplate;

    private FastJsonConfig fastJsonConfig;

    private RedisBasedQueue(){
        this.fastJsonConfig = new FastJsonConfig();
        ParserConfig parserConfig = new ParserConfig(true);
        parserConfig.setAutoTypeSupport(true);
        fastJsonConfig.setParserConfig(parserConfig);
        fastJsonConfig.setSerializeConfig(new SerializeConfig(true));
        fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteClassName);
    }

    public void pushClickCommand(OwnerAndTarget ownerAndTarget){
        redisTemplate.boundListOps(COMMAND_CLICK).leftPush(JSON.toJSONString(ownerAndTarget, this.fastJsonConfig.getSerializerFeatures()));
    }

    public OwnerAndTarget popClickCommand(){
        String json = redisTemplate.boundListOps(COMMAND_CLICK).rightPop(1, TimeUnit.SECONDS);
        if (StringUtils.isEmpty(json)){
            return null;
        }
        return JSON.parseObject(json, OwnerAndTarget.class, this.fastJsonConfig.getParserConfig());
    }

    public void pushLikeEvent(AbstractLikeEvent event){
        redisTemplate.boundListOps(EVENT_LIKE).leftPush(JSON.toJSONString(event, this.fastJsonConfig.getSerializerFeatures()));
    }

    public AbstractLikeEvent popLikeEvent(){
        String json = redisTemplate.boundListOps(EVENT_LIKE).rightPop(1, TimeUnit.SECONDS);
        if (StringUtils.isEmpty(json)){
            return null;
        }
        return (AbstractLikeEvent) JSON.parse(json, this.fastJsonConfig.getParserConfig());
    }
}
