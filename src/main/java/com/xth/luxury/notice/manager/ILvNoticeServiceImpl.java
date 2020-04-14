package com.xth.luxury.notice.manager;

import com.xth.luxury.notice.services.ILvNoticeService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
    public Boolean doGetStockStatus(@RequestBody String request) throws InterruptedException {
        lvNoticeManager.aTask();
        return true;
    }
}
