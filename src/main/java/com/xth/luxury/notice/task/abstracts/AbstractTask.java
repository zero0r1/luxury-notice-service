package com.xth.luxury.notice.task.abstracts;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.StaticLog;
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
    public void sendNoticeMsg(String content, String title, String msgType) {
        if (StringUtils.isNotEmpty(content)) {
            if (Validator.isNull(msgType)) {

                String requestParams = "{\"chatid\":\"chat47a302dce354eded2a3592a4f8efe72a\",\"msg\":{\"msgtype\":\"text\",\"text\":{\"content\":\"{}\"}}}";
                String post = HttpUtil.post("https://oapi.dingtalk.com/chat/send?access_token=22cfa2724ed53660b2ea62524563ed0c"
                        , StrUtil.format(requestParams, StrUtil.replace(content, "\"", "\\\"")));
                StaticLog.info("【钉钉通知】成功发送！to={}", post);
            } else {
                mailService.sendSimpleTextMail(this.TO, title, content);
            }
        }
    }

    public abstract void run();

    public abstract void getCookies();
}
