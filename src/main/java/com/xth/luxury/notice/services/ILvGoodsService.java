package com.xth.luxury.notice.services;

import com.xth.luxury.notice.domain.GetStocksReqDTO;
import com.xth.luxury.notice.domain.LvAddGoodsReqDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping("/luxury")
@Api(tags = "奢侈品到货通知")
public interface ILvGoodsService {

    /**
     * lv 到货通知
     *
     * @param request null
     * @return true
     */
    @ApiOperation(value = "lv 到货通知", response = String.class)
    @RequestMapping(value = "/lv_notice", method = RequestMethod.POST)
    String doGetStockStatus(@RequestBody GetStocksReqDTO request);

    /**
     * lv 添加新的商品
     *
     * @param request null
     * @return true
     */
    @ApiOperation(value = "lv 添加新的商品", response = Boolean.class)
    @RequestMapping(value = "/lv_add_goods", method = RequestMethod.POST)
    Boolean lvAddGoods(@RequestBody LvAddGoodsReqDTO request);
}
