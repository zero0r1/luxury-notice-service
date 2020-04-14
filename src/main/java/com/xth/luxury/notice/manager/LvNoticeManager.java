package com.xth.luxury.notice.manager;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.Validator;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@Component
public class LvNoticeManager {

    @Resource
    private MailService mailService;
    @Resource
    private RestTemplate restTemplate;
    private final String sku = "N41207";
    public final String url = "https://secure.louisvuitton.cn/ajaxsecure/getStockLevel.jsp?storeLang=zhs-cn&pageType=storelocator_section&skuIdList=" + sku + "&null&_=1586758087289";
    public String cookie = "";
    private final Integer notStock = 20;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void aTask() throws InterruptedException {

        int noStock = 0;
        try {
            if (Validator.isEmpty(cookie) || !cookie.contains("Secure")) {
                HttpResponse execute = HttpRequest.get("https://www.louisvuitton.cn/zhs-cn/products/pochette-accessoires-damier-azur-005868")
                        .timeout(1000)
                        .execute();
                cookie = execute.getCookieStr();
            }
        } catch (Exception e) {
            this.sendEmail(e);
            Console.log(ExceptionUtil.getMessage(e));
        }
        String result = "";
        try {
            result = HttpRequest.post(url)
                    .cookie(cookie)
                    //超时，毫秒
                    .timeout(1000)
                    .execute()
                    .body();
        } catch (HttpException e) {
            cookie = null;
            this.sendEmail(e);
            Console.log(ExceptionUtil.getMessage(e));
        }
        Object inStock = null;
        try {
            JSONObject jsonObject = JSONUtil.parseObj(result);
            Object skuObj = jsonObject.get(sku);
            inStock = JSONUtil.parseObj(skuObj).get("inStock");

            if (inStock.equals(true)) {
                this.sendEmail("有货啦~~");
                noStock = 0;
            } else {
                noStock++;
                if (noStock == this.notStock) {
                    this.sendEmail(result);
                    noStock = 0;
                }
            }
        } catch (Exception e) {
            this.sendEmail(e);
            Console.log(ExceptionUtil.getMessage(e));
        }
        this.sendEmail(result);
    }

    private void sendEmail(Exception e) {
        String to = "thassange@163.com";
        String subject = "lv 到货提醒";
        String content = "";
        if (Validator.isNotNull(e)) {
            content = ExceptionUtil.getMessage(e);
        }
        mailService.sendSimpleTextMail(to, subject, content);
    }

    private void sendEmail(String content) {
        String to = "thassange@163.com";
        String subject = "lv 到货提醒";
        mailService.sendSimpleTextMail(to, subject, content);
    }
}
