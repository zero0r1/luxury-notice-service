package com.xth.luxury.notice.manager;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.Validator;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import com.xth.luxury.notice.domain.GetStocksReqDTO;
import com.xth.luxury.notice.domain.MimvpProxyJava;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.*;
import java.util.ArrayList;
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
    private final Integer notStock = 20;
    List<InetSocketAddress> socketAddressList = Lists.newArrayList();

    //    @Scheduled(cron = "0 0/10 * * * ?")
    @Scheduled(cron = "0/15 * * * * ?")
    public void aTask() {
        String result = "";

        try {
            socketAddressList = this.getInetSocketAddress();
            this.getLouisVuittonCookies(null);
            result = this.getSkuStock(result);
            this.checkedInStockSendMail(result);
            this.sendEmail(result);
        } catch (Exception e) {
            throw e;
        }
    }

    public String aTask2(GetStocksReqDTO request) {
        String result = "";
        try {
            socketAddressList = this.getInetSocketAddress();
            this.getLouisVuittonCookies(request);
            result = this.getSkuStock(result);
            this.checkedInStockSendMail(result);
            this.sendEmail(result);
            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    private void checkedInStockSendMail(String result) {
        Object inStockObj = null;
        int noStock = 0;
        try {
            if (JSONUtil.isJson(result)) {
                Object skuObj = JSONUtil.parseObj(result).get(sku);
                inStockObj = JSONUtil.parseObj(skuObj).get(this.inStock);
            }
            if (Validator.equal(inStockObj, true)) {
                this.sendEmail("有货啦~~");
                noStock = 0;
            } else {
                if (noStock == this.notStock) {
                    this.sendEmail(result);
                    noStock = 0;
                }
                noStock++;
            }
        } catch (Exception e) {
            this.sendEmail(e);
            Console.log(ExceptionUtil.getMessage(e));
            throw e;
        }
    }

    /**
     * @param result
     * @return
     */
    private String getSkuStock(String result) {
        try {
            for (InetSocketAddress inetSocketAddress : socketAddressList) {
                try {
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, inetSocketAddress);
                    result = HttpRequest.post(url)
                            .setProxy(proxy)
                            .cookie(cookie)
                            //超时，毫秒
                            .timeout(2000)
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
            this.sendEmail(e);
            Console.log(ExceptionUtil.getMessage(e));
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
            if (CollectionUtil.isEmpty(socketAddressList)) {
                return;
            }

            for (InetSocketAddress inetSocketAddress : socketAddressList) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, inetSocketAddress);
                HttpResponse execute = null;
                try {
                    execute = HttpRequest.get(homePage)
                            .setProxy(proxy)
                            .timeout(2000)
                            .cookie(cookie)
                            .execute();
                } catch (Exception ignored) {
                }
                if (execute != null) {
                    cookie = execute.getCookieStr();
                    return;
                }
            }
        } catch (
                Exception e) {
            this.sendEmail(e);
            Console.log(ExceptionUtil.getMessage(e));
            throw e;
        }
    }

    /**
     * @param request
     */
    private void getLouisVuittonCookies2(GetStocksReqDTO request) throws UnknownHostException {
        try {

            // proxy protocol 只支持 http、socks5
            String data = MimvpProxyJava.proxy_protocol("http", MimvpProxyJava.proxyMap.get("http"), homePage);        // http
            System.out.println("http : " + MimvpProxyJava.proxyMap.get("http") + " --> " + data.length());
        } catch (
                Exception e) {
//            this.sendEmail(e);
            Console.log(ExceptionUtil.getMessage(e));
            throw e;
        }
    }

    /**
     * @param e
     */
    private void sendEmail(Exception e) {
        String to = "thassange@163.com";
        String subject = "lv 到货提醒";
        String content = "";
        if (Validator.isNotNull(e)) {
            content = ExceptionUtil.getMessage(e);
        }

        if (StringUtils.isNotEmpty(content)) {
            mailService.sendSimpleTextMail(to, subject, content);
        }
    }

    /**
     * @param content
     */
    private void sendEmail(String content) {
        String to = "thassange@163.com";
        String subject = "lv 到货提醒";
        if (StringUtils.isNotEmpty(content)) {
            mailService.sendSimpleTextMail(to, subject, content);
        }
    }

    private List<InetSocketAddress> getInetSocketAddress() {
        String mpUrl = "https://proxyapi.mimvp.com/api/fetchopen?orderid=868010510091252409&num=20&country_group=1&http_type=2&result_fields=1,2&result_format=json";
        String httpsIps = HttpUtil.get(mpUrl);
        Object result = "";
        List<InetSocketAddress> inetSocketAddressList = Lists.newArrayList();
        if (StringUtils.isEmpty(httpsIps)) {
            return inetSocketAddressList;
        }
        if (JSONUtil.isJson(httpsIps)) {
            JSONObject jsonObject = JSONUtil.parseObj(httpsIps);
            if (jsonObject.get("result") == null) {
                return inetSocketAddressList;
            }
            List<Object> objectArrayList = Lists.newArrayList(JSONUtil.parseArray(jsonObject.get("result")).toArray());
            if (CollectionUtil.isEmpty(objectArrayList)) {
                return inetSocketAddressList;
            }
            for (Object ipPortObj : objectArrayList) {
                result = JSONUtil.parseObj(ipPortObj).get("ip:port");
                if (Validator.isNotEmpty(result)) {
                    inetSocketAddressList.add(new InetSocketAddress(result.toString().split(":")[0], Integer.parseInt(result.toString().split(":")[1])));
                }
            }
        }
        return inetSocketAddressList;
    }
}
