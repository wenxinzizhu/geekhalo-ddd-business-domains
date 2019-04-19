package com.geekhalo.like;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.runtime.DefaultIdStrategy;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.geekhalo.like.domain.Target;
import com.geekhalo.like.domain.count.TargetCount;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

@Configuration
public class RedisTemplateConfiguration {

    @Bean
    public RedisTemplate<Target, TargetCount> targetCountRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<Target, TargetCount> targetCountRedisTemplate = new RedisTemplate<>();
        targetCountRedisTemplate.setConnectionFactory(redisConnectionFactory);
        targetCountRedisTemplate.setKeySerializer(new RedisSerializer<Target>() {
            @Override
            public byte[] serialize(Target target) throws SerializationException {
                String key = new StringBuilder()
                        .append("target:").append(target.getType().getCode()).append(":")
                        .append(target.getId())
                        .toString();

                return key.getBytes();
            }

            @Override
            public Target deserialize(byte[] bytes) throws SerializationException {
                return null;
            }
        });
        targetCountRedisTemplate.setValueSerializer(new RedisSerializer<TargetCount>(){
            private final RuntimeSchema<TargetCount> schema =
                    RuntimeSchema.createFrom(TargetCount.class, new DefaultIdStrategy());

            @Override
            public byte[] serialize(TargetCount targetCount) throws SerializationException {
                if (targetCount == null) {
                    return new byte[0];
                }else {
                    return ProtobufIOUtil.toByteArray(targetCount,
                            this.schema,
                            LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                }
            }

            @Override
            public TargetCount deserialize(byte[] bytes) throws SerializationException {
                if (bytes == null || bytes.length == 0){
                    return null;
                }
                TargetCount t = schema.newMessage();
                ProtobufIOUtil.mergeFrom(bytes, t, schema);
                return t;
            }
        });
        return targetCountRedisTemplate;
    }
}
