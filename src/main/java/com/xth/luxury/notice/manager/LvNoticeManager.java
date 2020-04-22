package com.xth.luxury.notice.manager;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.StaticLog;
import com.xth.luxury.notice.domain.GetStocksReqDTO;
import com.xth.luxury.notice.redis.InetSocketAddressRedis;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

@Component
public class LvNoticeManager {
    @Resource
    private MailService mailService;

    private final String sku = "N41207";
    public final String url = "https://secure.louisvuitton.cn/ajaxsecure/getStockLevel.jsp?storeLang=zhs-cn&pageType=storelocator_section&skuIdList=" + sku + "&null&_=1586758087289";
    public final String homePage = "https://www.louisvuitton.cn/zhs-cn/homepage";
    private int noStock = 0;
    public String cookie = "";
    @Resource
    private com.xth.luxury.notice.redis.InetSocketAddressRedis inetSocketAddressRedis;
    private int timeOut = 2000;
    String to = "thassange@163.com";

    public String runLvNoticeByManual() {
        String result = "";
        try {
            this.getLouisVuittonCookies(null);
            result = this.getSkuStock(result);
            this.checkedInStockSendMail(result);
            return result;
        } catch (Exception e) {
            StaticLog.error("aTask" + ExceptionUtil.getMessage(e));
        }
        return result;
    }

    private void checkedInStockSendMail(String result) {
        Object inStockObj = null;
        try {
            if (JSONUtil.isJson(result)) {
                Object skuObj = JSONUtil.parseObj(result).get(sku);
                String inStock = "inStock";
                inStockObj = JSONUtil.parseObj(skuObj).get(inStock);
            }
            if (Validator.equal(inStockObj, true)) {
                noStock = 0;
                StaticLog.info("{}", "lv 到货啦!");
                StaticLog.info("{}", "lv 到货啦!");
                StaticLog.info("{}", "lv 到货啦!");
                this.sendEmail("有货啦~~", "lv 到货啦!");
            } else {
                Integer notStockLimit = 20;
                if (noStock == notStockLimit) {
                    noStock = 0;
                    this.sendEmail(result, "lv 定时提醒.");
                }
                noStock++;
                StaticLog.info("{}{}", "lv 定时提醒.\\r\\n", result);
                StaticLog.info("{}{}次", "当前已经执行到", noStock);
            }
        } catch (Exception e) {
            StaticLog.error("checkedInStockSendMail" + ExceptionUtil.getMessage(e));
            throw e;
        }
    }

    /**
     * @param result
     * @return
     */
    private String getSkuStock(String result) {
        try {

            while (true) {
                try {
                    InetSocketAddress socketAddressItem = this.getInetSocketAddress();
                    if (socketAddressItem == null) {
                        return "";
                    }
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, socketAddressItem);
                    result = HttpRequest.post(url)
                            .setProxy(proxy)
                            .cookie(cookie)
                            //超时，毫秒
                            .timeout(timeOut)
                            .execute()
                            .body();
                } catch (Exception ignored) {
                }

                if (StringUtils.isNotEmpty(result) && JSONUtil.isJson(result)) {
                    break;
                }
            }
        } catch (Exception e) {
            cookie = null;
            StaticLog.error("getSkuStock" + ExceptionUtil.getMessage(e));
            throw e;
        }

        if (StringUtils.isNotEmpty(result)) {
            result = result.trim();
        }
        return JSONUtil.formatJsonStr(result);
    }

    /**
     * @param request
     */
    private void getLouisVuittonCookies(GetStocksReqDTO request) {
        try {

            while (true) {
                InetSocketAddress socketAddressItem = this.getInetSocketAddress();
                if (socketAddressItem == null) {
                    return;
                }
                Proxy proxy = new Proxy(Proxy.Type.HTTP, socketAddressItem);
                HttpResponse execute = null;
                try {
                    execute = HttpRequest.get(homePage)
                            .setProxy(proxy)
                            .timeout(timeOut)
                            .cookie(cookie)
                            .execute();
                } catch (Exception ignored) {
                    inetSocketAddressRedis.sRemove(InetSocketAddressRedis.ip, socketAddressItem);
                }
                if (execute != null) {
                    cookie = execute.getCookieStr();
                    return;
                }
            }
        } catch (Exception e) {
            StaticLog.error("aTask Scheduled" + ExceptionUtil.getMessage(e));
            throw e;
        }
    }


    /**
     * @param e
     */
    private void sendEmail(Exception e) {
        String subject = "lv 错误提示";
        String content = "";
        if (Validator.isNotNull(e)) {
            content = ExceptionUtil.getMessage(e);
        }

        if (StringUtils.isNotEmpty(content)) {
            mailService.sendSimpleTextMail(this.to, subject, content);
        }
    }

    /**
     * @param content
     */
    private void sendEmail(String content, String title) {
        if (StringUtils.isNotEmpty(content)) {
            mailService.sendSimpleTextMail(this.to, title, content);
        }
    }

    private InetSocketAddress getInetSocketAddress() {
        List<InetSocketAddress> socketAddressList = inetSocketAddressRedis.sPop(InetSocketAddressRedis.ip, 1);
        return !CollectionUtil.isEmpty(socketAddressList) ? socketAddressList.get(0) : null;
    }
}
