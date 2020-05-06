package com.xth.luxury.notice.task;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.StaticLog;
import com.xth.luxury.notice.domain.SelfInetSocketAddress;
import com.xth.luxury.notice.mapper.IpPoolMapper;
import com.xth.luxury.notice.redis.InetSocketAddressRedis;
import com.xth.luxury.notice.task.abstracts.AbstractTask;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.Proxy;

/**
 * @author shawn
 */
@Component
public class LvArrivedNoticeTask extends AbstractTask {
    @Resource
    private IpPoolMapper ipPoolMapper;

    String result = "";

    @PostConstruct
    public void init() {
        super.SKU = "M40712";
        super.URL = "https://secure.louisvuitton.cn/ajaxsecure/getStockLevel.jsp?storeLang=zhs-cn&pageType=storelocator_section&skuIdList=" + SKU + "&null&_=1586758087289";
        super.HOME_PAGE = "https://www.louisvuitton.cn/zhs-cn/homepage";
        super.NO_STOCK = 0;
        super.COOKIE = "";
        super.TIME_OUT = 2000;
        super.TO = "thassange@163.com";
        super.REDIS_KEY = InetSocketAddressRedis.ip;
    }

    @Async
    @Scheduled(fixedDelay = 10 * 1000, initialDelay = 0L)
    @Override
    public void run() {
        try {
            StaticLog.info("{}{}", "任务开始", "10s 一次");
            this.getCookies();
            this.getSkuStock();
            this.checkedInStockSendMail();
        } catch (Exception e) {
            StaticLog.error("aTask Scheduled" + ExceptionUtil.getMessage(e));
        }
    }

    /**
     * 获取 cookies
     */
    @Override
    public void getCookies() {
        Proxy proxy;
        HttpResponse execute;
        SelfInetSocketAddress socketAddressItem;
        while (true) {
            socketAddressItem = super.getInetSocketAddress();
            if (socketAddressItem != null) {
                StaticLog.info("{}{}{}", "getCookies", socketAddressItem, "开始执行...");
                proxy = new Proxy(Proxy.Type.HTTP, socketAddressItem.getInetSocketAddress());
                try {
                    execute = HttpRequest.get(super.HOME_PAGE)
                            .setProxy(proxy)
                            .timeout(super.TIME_OUT)
                            .cookie(super.COOKIE)
                            .execute();

                    super.COOKIE = execute.getCookieStr();
                    return;
                } catch (Exception ignored) {
                    ipPoolMapper.deleteById(socketAddressItem.getId());
                }
            } else {
                return;
            }
        }
    }

    /**
     * 获取库存
     */
    private void getSkuStock() {
        Proxy proxy;
        SelfInetSocketAddress socketAddressItem;
        do {
            socketAddressItem = super.getInetSocketAddress();
            if (socketAddressItem != null) {
                StaticLog.info("{}{}{}", "getSkuStock", socketAddressItem, "开始执行...");
                try {
                    proxy = new Proxy(Proxy.Type.HTTP, socketAddressItem.getInetSocketAddress());
                    result = HttpRequest.post(super.URL)
                            .setProxy(proxy)
                            .cookie(super.COOKIE)
                            //超时，毫秒
                            .timeout(super.TIME_OUT)
                            .execute()
                            .body();
                } catch (Exception ignored) {
                    ipPoolMapper.deleteById(socketAddressItem.getId());
                }
            } else {
                return;
            }
        } while (Validator.isEmpty(result) || !JSONUtil.isJson(result));

        result = StringUtils.deleteWhitespace(result);
        result = JSONUtil.formatJsonStr(result);
    }

    /**
     * 检查库存并发送邮件
     */
    private void checkedInStockSendMail() {
        Object inStockObj = null;
        try {
            StaticLog.info("{}{}", "checkedInStockSendMail", "开始执行...");
            if (JSONUtil.isJson(result)) {
                Object skuObj = JSONUtil.parseObj(result).get(SKU);
                String inStock = "inStock";
                inStockObj = JSONUtil.parseObj(skuObj).get(inStock);
            }
            if (Validator.equal(inStockObj, true)) {
                super.NO_STOCK = 0;
                StaticLog.info("{}", "lv 到货啦!");
                StaticLog.info("{}", "lv 到货啦!");
                StaticLog.info("{}", "lv 到货啦!");
                for (int i = 0; i < 3; i++) {
                    super.sendNoticeMsg(result, "有货啦~~", "lv 到货啦!", super.SKU, null);
                }
            } else {
                Integer notStockLimit = 20;
                super.NO_STOCK = 20;
                if (super.NO_STOCK.equals(notStockLimit)) {
                    super.NO_STOCK = 0;
                    super.sendNoticeMsg(result, "还没货...", "lv 定时提醒.", super.SKU, null);
                }
                super.NO_STOCK++;
                StaticLog.info("{}{}", "lv 定时提醒.\\r\\n", result);
                StaticLog.info("{}{}次", "当前已经执行到", super.NO_STOCK);
            }
        } catch (Exception e) {
            StaticLog.error("checkedInStockSendMail" + ExceptionUtil.getMessage(e));
        }
    }
}
