package com.xth.luxury.notice.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@Component
public class InetSocketAddressRedis extends AbstractRedis<InetSocketAddress> {
    public static String ip = "ip";

    InetSocketAddressRedis() {
        super(ip);
    }
}
