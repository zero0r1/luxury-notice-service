package com.xth.luxury.notice.task;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.StaticLog;
import com.google.common.collect.Maps;
import com.xth.luxury.notice.domain.LvAddGoodsRespDTO;
import com.xth.luxury.notice.domain.SelfInetSocketAddress;
import com.xth.luxury.notice.mapper.IpPoolMapper;
import com.xth.luxury.notice.mapper.LvSkuMapper;
import com.xth.luxury.notice.redis.InetSocketAddressRedis;
import com.xth.luxury.notice.task.abstracts.AbstractTask;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.Proxy;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author shawn
 */
@Component
public class LvArrivedNoticeTask extends AbstractTask {
    @Resource
    private IpPoolMapper ipPoolMapper;
    @Resource
    private LvSkuMapper lvSkuMapper;
    String result = "";
    public static boolean hasAddedNewGoods = false;
    private static List<LvAddGoodsRespDTO> goods = null;
    private static Iterator<LvAddGoodsRespDTO> iterator;
    private static final Map<String, Integer> goodsAlertCountMap = Maps.newHashMap();

    @PostConstruct
    public void init() {
        super.SKU = "M40712";
        super.URL = StrUtil.format("{}{}"
                , "https://secure.louisvuitton.cn/ajaxsecure/getStockLevel.jsp?storeLang=zhs-cn&pageType=storelocator_section&skuIdList="
                , super.SKU);
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
        StaticLog.info("{} - {}", "任务开始", "10s 一次");
        try {
            this.setSkuInfo();
            this.getCookies();
            this.getSkuStock();
            this.checkedInStockSendMail();
        } catch (Exception e) {
            StaticLog.error("LvArrivedNoticeTask run" + ExceptionUtil.getMessage(e));
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
            if (socketAddressItem == null) {
                return;
            } else {
                StaticLog.info("{} - {} - {}", "getCookies", socketAddressItem, "开始执行...");
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
            }
        }
    }

    @Override
    public void setSkuInfo() {
        if (LvArrivedNoticeTask.hasAddedNewGoods) {
            this.initGoods();
            LvArrivedNoticeTask.hasAddedNewGoods = false;
        } else {
            if (Validator.isNull(goods)) {
                this.initGoods();
            }
        }

        if (Validator.isNotNull(goods)) {
            this.resetIterator();
            LvAddGoodsRespDTO currentGoods = iterator.next();
            super.TITLE = currentGoods.getTitle();
            super.SKU = currentGoods.getSku();
            super.URL = StrUtil.format("{}{}"
                    , currentGoods.getUrl()
                    , SKU);
        }
    }

    private void resetIterator() {
        if (!iterator.hasNext()) {
            iterator = goods.iterator();
        }
    }

    private void initGoods() {
        goods = lvSkuMapper.getGoods();
        iterator = goods.iterator();
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
                StaticLog.info("商品: {} sku: {} - {} ", super.TITLE, super.SKU, "开始执行...");
                StaticLog.info("{} - {} - {}", "getSkuStock", socketAddressItem, "开始执行...");
                proxy = new Proxy(Proxy.Type.HTTP, socketAddressItem.getInetSocketAddress());
                try {
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
        StaticLog.info("{} - {}", "checkedInStockSendMail", "开始执行...");
        try {
            if (JSONUtil.isJson(result)) {
                Object skuObj = JSONUtil.parseObj(result).get(SKU);
                String inStock = "inStock";
                inStockObj = JSONUtil.parseObj(skuObj).get(inStock);
            }
            if (Validator.equal(inStockObj, true)) {
                goodsAlertCountMap.put(super.SKU, 0);
                StaticLog.info("商品: {} sku: {} - {}", super.TITLE, super.SKU, "lv 到货啦!");
                for (int i = 0; i < 3; i++) {
                    super.sendNoticeMsg(result
                            , StrUtil.format("\r        商品: {}\r        sku: {} {}", super.TITLE, super.SKU, "lv 到货啦!"), "lv 到货啦!"
                            , super.SKU
                            , null);
                }
            } else {
                Integer notStockLimit = 20;
                Integer alertCount = goodsAlertCountMap.get(super.SKU);
                alertCount = alertCount == null ? 0 : alertCount;
                if (alertCount.equals(notStockLimit)) {
                    goodsAlertCountMap.put(super.SKU, 0);
                    super.sendNoticeMsg(result
                            , StrUtil.format("\r        商品: {}\r        sku: {} {}", super.TITLE, super.SKU, "还没货..."), "还没货..."
                            , super.SKU
                            , null);
                }
                Integer calcAlertCount = goodsAlertCountMap.get(super.SKU);
                calcAlertCount = calcAlertCount == null ? 0 : calcAlertCount;
                goodsAlertCountMap.put(super.SKU, ++calcAlertCount);
                StaticLog.info("{} - {}", "lv 定时提醒.\r\n", result);
                StaticLog.info("{} - {}次", "当前已经执行到", calcAlertCount);
            }
        } catch (Exception e) {
            StaticLog.error("checkedInStockSendMail" + ExceptionUtil.getMessage(e));
        }
    }
}
