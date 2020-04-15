package com.xth.luxury.notice.manager;

import com.xth.luxury.notice.domain.GetStocksReqDTO;
import com.xth.luxury.notice.services.ILvNoticeService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.net.UnknownHostException;

@RestController
public class ILvNoticeServiceImpl implements ILvNoticeService {
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
        return lvNoticeManager.aTask2(request);
    }
}
