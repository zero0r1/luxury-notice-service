package com.xth.luxury.notice.task.abstracts;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.StaticLog;
import com.xth.luxury.notice.domain.IpPoolDO;
import com.xth.luxury.notice.domain.SelfInetSocketAddress;
import com.xth.luxury.notice.manager.MailService;
import com.xth.luxury.notice.mapper.IpPoolMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;

/**
 * 功能描述: 任务
 *
 * @author shawn
 * @date 2020/5/7 8:59 上午
 */
@Component
public abstract class AbstractTask {
    @Resource
    public MailService mailService;
    @Resource
    private IpPoolMapper ipPoolMapper;
    public static String SKU = "";
    public static String TITLE = "";
    public static String URL = "";
    public static String HOME_PAGE = "";
    public static Integer NO_STOCK = 0;
    public static String COOKIE = "";
    public static Integer TIME_OUT = 2000;
    public static String TO = "thassange@163.com";
    public static String REDIS_KEY = "";
    public static Integer NOT_STOCK_LIMIT = 200;

    /**
     * 获取地址
     *
     * @return inet 地址
     */
    public SelfInetSocketAddress getInetSocketAddress() {
        IpPoolDO one = ipPoolMapper.getOne();
        if (Validator.isNotNull(one)) {
            return SelfInetSocketAddress.builder()
                    .id(one.getId())
                    .inetSocketAddress(new InetSocketAddress(one.getIp(), one.getPort()))
                    .build();
        }
        return null;
    }

    /**
     * 发送邮件
     *
     * @param content 内容
     * @param title   标题
     */
    public void sendNoticeMsg(String content, String summary, String title, String goodsNo, String msgType) {
        String contentTemple = StrUtil.format("标题: {}\r货号: {}\r摘要: {}\r内容: {}", title, goodsNo, summary, content);
        if (StringUtils.isNotEmpty(content)) {
            if (Validator.isNull(msgType)) {
                String token = HttpUtil.get("https://oapi.dingtalk.com/gettoken?appkey=dingzmdnhqspdfjbst1l&appsecret=2l4MDbWhm-XIeyCnwaO6D3r8vrJIiKvyuSmspqNl-OL38Xb63zLuVPuegXawCMZr");
                if (JSONUtil.isJson(token)) {

                    Object access_token = JSONUtil.parseObj(token).get("access_token");
                    String requestParams = "{\"chatid\":\"chat47a302dce354eded2a3592a4f8efe72a\",\"msg\":{\"msgtype\":\"text\",\"text\":{\"content\":\"{}\"}}}";
                    String post = HttpUtil.post(StrUtil.format("https://oapi.dingtalk.com/chat/send?access_token={}", access_token)
                            , StrUtil.format(requestParams, StrUtil.replace(contentTemple, "\"", "\\\"")));
                    StaticLog.info("【钉钉通知】成功发送！to={}", post);
                }
            } else {
                mailService.sendSimpleTextMail(this.TO, title, content);
            }
        }
    }

    public abstract void run();

    public abstract void getCookies();

    public abstract void setSkuInfo();
}
