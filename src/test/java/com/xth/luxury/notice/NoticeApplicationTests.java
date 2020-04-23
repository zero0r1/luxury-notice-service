package com.xth.luxury.notice;

import cn.hutool.core.lang.Console;
import cn.hutool.http.HttpUtil;
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
        String post = HttpUtil.post("https://oapi.dingtalk.com/chat/send?access_token=22cfa2724ed53660b2ea62524563ed0c"
                , "{\"chatid\":\"chat47a302dce354eded2a3592a4f8efe72a\",\"msg\":{\"msgtype\":\"text\",\"text\":{\"content\":\"text\"}}}");
        Console.log(post);
    }


    @Test
    void testRedis() {
        boolean set = redisUtil.set("rest_redis", "test_value");
        Object test_redis = redisUtil.get("rest_redis");
        Console.log(test_redis);
    }
}
