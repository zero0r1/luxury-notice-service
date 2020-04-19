package com.xth.luxury.notice.manager;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import com.xth.luxury.notice.domain.GetStocksReqDTO;
import com.xth.luxury.notice.redis.InetSocketAddressRedis;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.validation.GroupSequence;
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
    private int timeOut = 6000;

    //    @Scheduled(cron = "0 0/10 * * * ?")
    @Scheduled(cron = "0/15 * * * * ?")
    public void aTask() {
        String result = "";

        try {
//            socketAddressList = this.getInetSocketAddressByXiCi();
            this.getLouisVuittonCookies(null);
            result = this.getSkuStock(result);
            this.checkedInStockSendMail(result);
//            this.sendEmail(result);
        } catch (Exception e) {
            throw e;
        }
    }

    //    @Scheduled(cron = "0/3 * * * * ?")
    public void getIpTask() {
        getInetSocketAddressByXiCi();
    }

    @Scheduled(cron = "0/3 * * * * ?")
    public void getIpTask89() {
        getInetSocketAddressBy89();
    }

    @Scheduled(cron = "0/3 * * * * ?")
    public void getIpTaskXila() {
        getInetSocketAddressByXila();
    }

    public String aTask2(GetStocksReqDTO request) {
        String result = "";
        try {
//            socketAddressList = this.getInetSocketAddressByXiCi();
            this.getLouisVuittonCookies(request);
            result = this.getSkuStock(result);
            this.checkedInStockSendMail(result);
//            this.sendEmail(result);
            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    private void checkedInStockSendMail(String result) {
        Object inStockObj = null;

        try {
            if (JSONUtil.isJson(result)) {
                Object skuObj = JSONUtil.parseObj(result).get(sku);
                inStockObj = JSONUtil.parseObj(skuObj).get(this.inStock);
            }
            if (Validator.equal(inStockObj, true)) {
                this.sendEmail("有货啦~~");
                noStock = 0;
            } else {
                if (noStock == this.notStockLimit) {
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

    /**
     * 西刺代理
     *
     * @return ip 地址
     */
    public List<InetSocketAddress> getInetSocketAddressByXiCi() {
        String httpsIps = "";
        String xiCiUrl = "https://www.xicidaili.com/wn/";

        InetSocketAddress socketAddressItem = this.getInetSocketAddress();
        if (socketAddressItem == null) {
            httpsIps = HttpUtil.get(xiCiUrl);
        } else {
            String pageNum = !this.ipPageNum.equals(0) ? this.ipPageNum.toString() : "";
            Proxy proxy = new Proxy(Proxy.Type.HTTP, socketAddressItem);

            do {
                try {
                    socketAddressItem = this.getInetSocketAddress();
                    if (socketAddressItem == null) {
                        return Lists.newArrayList();
                    }
                    httpsIps = HttpRequest.get(xiCiUrl + pageNum)
                            .setProxy(proxy)
                            .timeout(timeOut)
                            .execute()
                            .body();
                } catch (Exception e) {
                    inetSocketAddressRedis.sRemove(InetSocketAddressRedis.ip, socketAddressItem);
                }
            } while (StringUtils.isEmpty(httpsIps));
        }

        List<InetSocketAddress> result = Lists.newArrayList();
        //使用正则获取所有标题
        List<String> ips = ReUtil.findAll("((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}", httpsIps, 0);
        List<String> ports = ReUtil.findAll("<td>(\\d+)</td>", httpsIps, 1);

        int index = 0;
        String port = "";
        InetSocketAddress inetSocketAddress;
        for (String ip : ips) {
            port = ports.get(index);
            int portInt = NumberUtils.isDigits(port) ? Integer.parseInt(port) : 0;

            if (portInt > 0) {
                inetSocketAddress = new InetSocketAddress(ip, portInt);
                result.add(inetSocketAddress);
                inetSocketAddressRedis.sAdd(InetSocketAddressRedis.ip, inetSocketAddress);
            }
            index++;
        }

        this.ipPageNum++;
        return result;
    }

    /**
     * 89免费代理
     *
     * @return
     */
    public List<InetSocketAddress> getInetSocketAddressBy89() {
        String httpsIps = "";
        //http://www.89ip.cn/
        String url = "http://www.89ip.cn/tqdl.html?num=600&address=&kill_address=&port=&kill_port=&isp=";

        InetSocketAddress socketAddressItem = this.getInetSocketAddress();
        if (socketAddressItem == null) {
            httpsIps = HttpUtil.get(url);
        } else {
            String pageNum = !this.ipPageNum.equals(0) ? this.ipPageNum.toString() : "";
            Proxy proxy = new Proxy(Proxy.Type.HTTP, socketAddressItem);

            do {
                try {
                    socketAddressItem = this.getInetSocketAddress();
                    if (socketAddressItem == null) {
                        return Lists.newArrayList();
                    }
                    httpsIps = HttpRequest.get(url + pageNum)
                            .setProxy(proxy)
                            .timeout(timeOut)
                            .execute()
                            .body();
                } catch (Exception e) {
                    inetSocketAddressRedis.sRemove(InetSocketAddressRedis.ip, socketAddressItem);
                }
            } while (StringUtils.isEmpty(httpsIps));
        }

        List<InetSocketAddress> result = Lists.newArrayList();
        //使用正则获取所有标题
        List<String> ips = ReUtil.findAll("((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}:(\\d+)", httpsIps, 0);

        int index = 0;
        String port = "";
        String currentIp = "";
        InetSocketAddress inetSocketAddress;
        for (String ip : ips) {
            currentIp = ip.split(":")[0];
            port = ip.split(":")[1];
            int portInt = NumberUtils.isDigits(port) ? Integer.parseInt(port) : 0;

            inetSocketAddress = new InetSocketAddress(currentIp, portInt);
            result.add(inetSocketAddress);
            inetSocketAddressRedis.sAdd(InetSocketAddressRedis.ip, inetSocketAddress);
            index++;
        }

        this.ipPageNum++;
        return result;
    }


    /**
     * 西拉免费代理
     *
     * @return
     */
    public List<InetSocketAddress> getInetSocketAddressByXila() {
        String httpsIps = "";
        //http://www.xiladaili.com/interface/
        String url = "http://www.xiladaili.com/api/?uuid=b398cacf74744c479cd84b004c7e7dd8&num=500&place=%E4%B8%AD%E5%9B%BD&protocol=2&sortby=0&repeat=1&format=3&position=1";

        InetSocketAddress socketAddressItem = this.getInetSocketAddress();
        if (socketAddressItem == null) {
            httpsIps = HttpUtil.get(url);
        } else {
            String pageNum = !this.ipPageNum.equals(0) ? this.ipPageNum.toString() : "";
            Proxy proxy = new Proxy(Proxy.Type.HTTP, socketAddressItem);

            do {
                try {
                    socketAddressItem = this.getInetSocketAddress();
                    if (socketAddressItem == null) {
                        return Lists.newArrayList();
                    }
                    httpsIps = HttpRequest.get(url + pageNum)
                            .setProxy(proxy)
                            .timeout(timeOut)
                            .execute()
                            .body();
                } catch (Exception e) {
                    inetSocketAddressRedis.sRemove(InetSocketAddressRedis.ip, socketAddressItem);
                }
            } while (StringUtils.isEmpty(httpsIps));
        }

        List<InetSocketAddress> result = Lists.newArrayList();
        //使用正则获取所有标题
        List<String> ips = ReUtil.findAll("((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}:(\\d+)", httpsIps, 0);

        int index = 0;
        String port = "";
        String currentIp = "";
        InetSocketAddress inetSocketAddress;
        for (String ip : ips) {
            currentIp = ip.split(":")[0];
            port = ip.split(":")[1];
            int portInt = NumberUtils.isDigits(port) ? Integer.parseInt(port) : 0;

            inetSocketAddress = new InetSocketAddress(currentIp, portInt);
            result.add(inetSocketAddress);
            inetSocketAddressRedis.sAdd(InetSocketAddressRedis.ip, inetSocketAddress);
            index++;
        }

        this.ipPageNum++;
        return result;
    }

    private InetSocketAddress getInetSocketAddress() {
        List<InetSocketAddress> socketAddressList = inetSocketAddressRedis.sPop(InetSocketAddressRedis.ip, 1);
        return !CollectionUtil.isEmpty(socketAddressList) ? socketAddressList.get(0) : null;
    }
}
