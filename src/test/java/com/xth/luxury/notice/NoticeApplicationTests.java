package com.xth.luxury.notice;

import cn.hutool.core.lang.Console;
import com.xth.luxury.notice.manager.LvNoticeManager;
import com.xth.luxury.notice.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class NoticeApplicationTests {

    @Resource
    private LvNoticeManager lvNoticeManager;
    @Resource
    private RedisUtil redisUtil;

    @Test
    void contextLoads() {
    }


    @Test
    void testRedis() {
        boolean set = redisUtil.set("rest_redis", "test_value");
        Object test_redis = redisUtil.get("rest_redis");
        Console.log(test_redis);
    }
}
