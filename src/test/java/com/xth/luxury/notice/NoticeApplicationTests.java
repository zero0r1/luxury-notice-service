package com.xth.luxury.notice;

import cn.hutool.core.lang.Console;
import com.xth.luxury.notice.manager.LvNoticeManager;
import com.xth.luxury.notice.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import javax.annotation.Resources;
import java.net.InetSocketAddress;
import java.util.List;

@SpringBootTest
class NoticeApplicationTests {

    @Resource
    private LvNoticeManager lvNoticeManager;
    @Resource
    private RedisUtil redisUtil;

    @Test
    void contextLoads() {
        List<InetSocketAddress> inetSocketAddressByXiCi = lvNoticeManager.getInetSocketAddressByXiCi();
        int a = 1;
    }


    @Test
    void testRedis() {
        boolean set = redisUtil.set("rest_redis", "test_value");
        Object test_redis = redisUtil.get("rest_redis");
        Console.log(test_redis);
    }
}
