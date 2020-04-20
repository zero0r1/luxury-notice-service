package com.xth.luxury.notice.manager;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.StaticLog;
import com.xth.luxury.notice.domain.GetStocksReqDTO;
import com.xth.luxury.notice.redis.InetSocketAddressRedis;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

@Component
public class LvNoticeManager {

    @Resource
    private MailService mailService;
    @Resource
    private RestTemplate restTemplate;
    private final String sku = "N41207";
    private final String inStock = "inStock";

    public final String url = "https://secure.louisvuitton.cn/ajaxsecure/getStockLevel.jsp?storeLang=zhs-cn&pageType=storelocator_section&skuIdList=" + sku + "&null&_=1586758087289";
    public final String homePage = "https://www.louisvuitton.cn/zhs-cn/homepage";
    public String cookie = "";
    private final Integer notStockLimit = 20;
    private int noStock = 0;
    @Resource
    private com.xth.luxury.notice.redis.InetSocketAddressRedis inetSocketAddressRedis;
    private Integer ipPageNum = 0;
    private int timeOut = 2000;
    String to = "thassange@163.com";

    @Scheduled(cron = "0/15 * * * * ?")
    public void aTask() {
        String result = "";
        try {
            StaticLog.info("{}{}", "任务开始", "15s 一次");
            this.getLouisVuittonCookies(null);
            result = this.getSkuStock(result);
            this.checkedInStockSendMail(result);
        } catch (Exception e) {
            StaticLog.error("aTask Scheduled" + ExceptionUtil.getMessage(e));
        }
    }

    public String aTask2(GetStocksReqDTO request) {
        String result = "";
        try {
            this.getLouisVuittonCookies(request);
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
                inStockObj = JSONUtil.parseObj(skuObj).get(this.inStock);
            }
            if (Validator.equal(inStockObj, true)) {
                this.sendEmail("有货啦~~", "lv 到货啦!");
                noStock = 0;
            } else {
                if (noStock == this.notStockLimit) {
                    this.sendEmail(result, "lv 定时提醒.");
                    noStock = 0;
                }
                StaticLog.info("{}\r\n,{}", "lv 定时提醒.", result);
                noStock++;
                StaticLog.info("{}\r\n,{}", "当前已经执行到", noStock);
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

    @Scheduled(cron = "0/30 * * * * ?")
    private void getInetSocketAddressByApi() {
        String mpUrl = "https://proxyapi.mimvp.com/api/fetchopen?orderid=861176314039185103&country_group=1&http_type=2&result_fields=1,2&result_format=json";
        String httpsIps = HttpUtil.get(mpUrl);
        Object result = "";
        if (StringUtils.isNotEmpty(httpsIps)) {
            if (JSONUtil.isJson(httpsIps)) {
                JSONObject jsonObject = JSONUtil.parseObj(httpsIps);
                JSONArray objects = JSONUtil.parseArray(jsonObject.get("result"));
                for (Object ipPortObj : objects.toArray()) {
                    result = JSONUtil.parseObj(ipPortObj).get("ip:port");
                    if (Validator.isNotEmpty(result)) {
                        InetSocketAddress inetSocketAddress = new InetSocketAddress(result.toString().split(":")[0], Integer.parseInt(result.toString().split(":")[1]));
                        inetSocketAddressRedis.sAdd(InetSocketAddressRedis.ip, inetSocketAddress);
                    }
                }
            }
        }
    }

    private InetSocketAddress getInetSocketAddress() {
        List<InetSocketAddress> socketAddressList = inetSocketAddressRedis.sPop(InetSocketAddressRedis.ip, 1);
        return !CollectionUtil.isEmpty(socketAddressList) ? socketAddressList.get(0) : null;
    }
}
