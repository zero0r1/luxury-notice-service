package com.xth.luxury.notice.task;

import cn.hutool.core.lang.Validator;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.StaticLog;
import com.xth.luxury.notice.redis.InetSocketAddressRedis;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;

/**
 * @author shawn
 */
@Component
public class MiMvpTask {

    @Resource
    private InetSocketAddressRedis inetSocketAddressRedis;


    @Scheduled(cron = "0/30 * * * * ?")
    private void getInetSocketAddressByApi() {
        boolean hasException = false;
        do {
            try {
                StaticLog.info("{}{}", "getInetSocketAddressByApi", "开始获取...");
                String mpUrl = "https://proxyapi.mimvp.com/api/fetchopen?orderid=861176314039185103&country_group=1&http_type=2&result_fields=1,2&result_format=json";
                String httpsIps = HttpUtil.get(mpUrl);
                Object result = "";
                InetSocketAddress inetSocketAddress;

                if (StringUtils.isNotEmpty(httpsIps)) {
                    if (JSONUtil.isJson(httpsIps)) {
                        JSONObject jsonObject = JSONUtil.parseObj(httpsIps);
                        JSONArray objects = JSONUtil.parseArray(jsonObject.get("result"));
                        for (Object ipPortObj : objects.toArray()) {
                            result = JSONUtil.parseObj(ipPortObj).get("ip:port");
                            if (Validator.isNotEmpty(result)) {
                                inetSocketAddress = new InetSocketAddress(result.toString().split(":")[0], Integer.parseInt(result.toString().split(":")[1]));
                                inetSocketAddressRedis.sAdd(InetSocketAddressRedis.ip, inetSocketAddress);
                            }
                        }
                    }
                }
                hasException = false;
            } catch (Exception e) {
                hasException = true;
            }
        } while (hasException);
    }
}
