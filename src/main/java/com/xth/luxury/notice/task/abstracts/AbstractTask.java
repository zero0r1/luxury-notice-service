package com.xth.luxury.notice.task.abstracts;

import cn.hutool.core.collection.CollectionUtil;
import com.xth.luxury.notice.manager.MailService;
import com.xth.luxury.notice.redis.InetSocketAddressRedis;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.List;

@Component
public abstract class AbstractTask {
    @Resource
    public MailService mailService;
    @Resource
    public InetSocketAddressRedis inetSocketAddressRedis;
    public String SKU = "";
    public String URL = "";
    public String HOME_PAGE = "";
    public Integer NO_STOCK = 0;
    public String COOKIE = "";
    public Integer TIME_OUT = 2000;
    public String TO = "thassange@163.com";
    public String REDIS_KEY = "";

    /**
     * 获取地址
     *
     * @param key redis key
     * @return inet 地址
     */
    public InetSocketAddress getInetSocketAddress(String key) {
        List<InetSocketAddress> socketAddressList = inetSocketAddressRedis.sPop(key, 1);
        return !CollectionUtil.isEmpty(socketAddressList) ? socketAddressList.get(0) : null;
    }

    /**
     * 发送邮件
     *
     * @param content 内容
     * @param title   标题
     */
    public void sendEmail(String content, String title) {
        if (StringUtils.isNotEmpty(content)) {
            mailService.sendSimpleTextMail(this.TO, title, content);
        }
    }

    public abstract void run();

    public abstract void getCookies();
}
