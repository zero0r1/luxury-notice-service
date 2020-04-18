package com.xth.luxury.notice.redis;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;

@Configurable
public class RedisAtomicIntegerUtil {

    @Bean
    public RedisAtomicInteger init(RedisConnectionFactory factory) {
        return new RedisAtomicInteger("myCounter", factory);
    }
}
