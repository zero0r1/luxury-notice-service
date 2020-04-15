package com.xth.luxury.notice.services;

import com.xth.luxury.notice.domain.GetStocksReqDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.net.UnknownHostException;

@RequestMapping("/luxury")
@Api(tags = "奢侈品到货通知")
public interface ILvNoticeService {

    /**
     * lv到货通知
     *
     * @param request null
     * @return true
     */
    @ApiOperation(value = "lv到货通知", response = Boolean.class)
    @RequestMapping(value = "/lv_notice", method = RequestMethod.POST)
    Boolean doGetStockStatus(@RequestBody GetStocksReqDTO request) throws InterruptedException, UnknownHostException;
}