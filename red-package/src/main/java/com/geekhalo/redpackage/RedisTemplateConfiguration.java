package com.geekhalo.redpackage;


import com.geekhalo.redpackage.domain.RedPackage;
import com.geekhalo.redpackage.domain.UserRedPackage;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.util.List;

@Configuration
public class RedisTemplateConfiguration {

    @Bean
    public RedisTemplate<String, RedPackage> redPackageRedisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String, RedPackage> userRedisTemplate = new RedisTemplate<>();
        // 设置 connectionFactory
        userRedisTemplate.setConnectionFactory(connectionFactory);
        // 设置 keySerializer
        userRedisTemplate.setKeySerializer(new GenericToStringSerializer<String>(String.class));
        // 设置 valueSerializer
        userRedisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<RedPackage>(RedPackage.class));

        return userRedisTemplate;
    }

    @Bean
    public RedisTemplate<String, UserRedPackage> userRedPackageRedisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String, UserRedPackage> userRedisTemplate = new RedisTemplate<>();
        // 设置 connectionFactory
        userRedisTemplate.setConnectionFactory(connectionFactory);
        // 设置 keySerializer
        userRedisTemplate.setKeySerializer(new GenericToStringSerializer<String>(String.class));
        // 设置 valueSerializer
        userRedisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<UserRedPackage>(UserRedPackage.class));

        return userRedisTemplate;
    }

    @Bean
    public RedisTemplate<String, List<RedPackage>> redPackagesRedisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String, List<RedPackage>> redisTemplate = new RedisTemplate<>();
        // 设置 connectionFactory
        redisTemplate.setConnectionFactory(connectionFactory);
        // 设置 keySerializer
        redisTemplate.setKeySerializer(new GenericToStringSerializer<String>(String.class));
        TypeFactory typeFactory = TypeFactory.defaultInstance();
        // 设置 valueSerializer
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<List<RedPackage>>(typeFactory.constructCollectionType(List.class, RedPackage.class)));

        return redisTemplate;
    }

}
