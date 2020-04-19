package com.xth.luxury.notice.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.google.common.collect.Lists;
import com.xth.luxury.notice.redis.InetSocketAddressRedis;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

@Component
public class XiLaIpTask {
    @Resource
    private com.xth.luxury.notice.redis.InetSocketAddressRedis inetSocketAddressRedis;
    private Integer ipPageNum = 0;
    private int timeOut = 6000;

    @Scheduled(cron = "0/3 * * * * ?")
    public void getIpTaskXila() {
        getInetSocketAddressByXila();
    }

    /**
     * 西拉免费代理
     */
    public void getInetSocketAddressByXila() {
        String httpsIps = "";
        //http://www.xiladaili.com/interface/
        String url = "http://www.xiladaili.com/https/";

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
                        return;
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
            inetSocketAddressRedis.sAdd(InetSocketAddressRedis.ip, inetSocketAddress);
            index++;
        }

        this.ipPageNum++;
    }

    private InetSocketAddress getInetSocketAddress() {
        List<InetSocketAddress> socketAddressList = inetSocketAddressRedis.sPop(InetSocketAddressRedis.ip, 1);
        return !CollectionUtil.isEmpty(socketAddressList) ? socketAddressList.get(0) : null;
    }
}
