package com.xth.luxury.notice.manager;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.Validator;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import org.apache.commons.lang3.StringUtils;
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
    private final String inStock = "inStock";

    public final String url = "https://secure.louisvuitton.cn/ajaxsecure/getStockLevel.jsp?storeLang=zhs-cn&pageType=storelocator_section&skuIdList=" + sku + "&null&_=1586758087289";
    public String cookie = "";
    private final Integer notStock = 20;

    //    @Scheduled(cron = "0 0/10 * * * ?")
//    @Scheduled(cron = "0/60 * * * * ?")
    public void aTask() throws InterruptedException {
        String result = "";


        try {
            this.getLouisVuittonCookies();
            result = this.getSkuStock(result);
            this.checkedInStockSendMail(result);
            this.sendEmail(result);
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
            throw e;
        }

        if (StringUtils.isNotEmpty(result)) {
            result = result.trim();
        }
        return JSONUtil.formatJsonStr(result);
    }

    /**
     *
     */
    private void getLouisVuittonCookies() {
        try {
            HttpResponse execute = HttpRequest.get("https://www.louisvuitton.cn/zhs-cn/homepage")
                    .header("User-Agent", " Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv\", \"74.0) Gecko/20100101 Firefox/74.0")
                    .header("Accept", " text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", " en-US,en;q=0.5' --compresse")
                    .header("DNT", " 1")
                    .header("Connection", " keep-alive")
                    .header("Upgrade-Insecure-Requests", " 1")
                    .timeout(1000)
                    .cookie(cookie)
                    .execute();
            cookie = execute.getCookieStr();
        } catch (
                Exception e) {
            this.sendEmail(e);
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
}
