package com.xth.luxury.notice;

import com.xth.luxury.notice.manager.LvNoticeManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.List;

@SpringBootTest
class NoticeApplicationTests {

    @Resource
    private LvNoticeManager lvNoticeManager;

    @Test
    void contextLoads() {
        List<InetSocketAddress> inetSocketAddressByXiCi = lvNoticeManager.getInetSocketAddressByXiCi();
        int a = 1;
    }

}
