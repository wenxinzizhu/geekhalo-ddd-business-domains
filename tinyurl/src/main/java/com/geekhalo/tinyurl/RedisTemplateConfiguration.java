package com.geekhalo.tinyurl;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.runtime.DefaultIdStrategy;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.geekhalo.tinyurl.domain.TargetUrl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

@Configuration
public class RedisTemplateConfiguration {
    @Bean
    public RedisTemplate<Long, TargetUrl> tinyUrlRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<Long, TargetUrl> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new RedisSerializer<Long>() {
            @Override
            public byte[] serialize(Long s) throws SerializationException {
                return String.valueOf(s).getBytes();
            }

            @Override
            public Long deserialize(byte[] bytes) throws SerializationException {
                return Long.valueOf(new String(bytes));
            }
        });
        redisTemplate.setValueSerializer(new RedisSerializer<TargetUrl>(){
            private final RuntimeSchema<TargetUrl> schema =
                    RuntimeSchema.createFrom(TargetUrl.class, new DefaultIdStrategy());

            @Override
            public byte[] serialize(TargetUrl targetUrl) throws SerializationException {
                if (targetUrl == null) {
                    return new byte[0];
                }else {
                    return ProtobufIOUtil.toByteArray(targetUrl,
                            this.schema,
                            LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                }
            }

            @Override
            public TargetUrl deserialize(byte[] bytes) throws SerializationException {
                if (bytes == null || bytes.length == 0){
                    return null;
                }
                TargetUrl t = schema.newMessage();
                ProtobufIOUtil.mergeFrom(bytes, t, schema);
                return t;
            }
        });
        return redisTemplate;
    }
}
