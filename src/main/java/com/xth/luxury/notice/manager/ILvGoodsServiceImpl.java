package com.xth.luxury.notice.manager;

import com.xth.luxury.notice.domain.GetStocksReqDTO;
import com.xth.luxury.notice.domain.LvAddGoodsReqDTO;
import com.xth.luxury.notice.services.ILvGoodsService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class ILvGoodsServiceImpl implements ILvGoodsService {
    @Resource
    private LvNoticeManager lvNoticeManager;

    /**
     * lv到货通知
     *
     * @param request null
     * @return true
     */
    @Override
    public String doGetStockStatus(@RequestBody GetStocksReqDTO request) {
        return lvNoticeManager.runLvNoticeByManual();
    }

    /**
     * lv 添加新的商品
     *
     * @param request null
     * @return true
     */
    @Override
    public Boolean lvAddGoods(LvAddGoodsReqDTO request) {
        return lvNoticeManager.lvAddGoods(request);
    }
}
