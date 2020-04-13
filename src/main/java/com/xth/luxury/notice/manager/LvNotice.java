package com.xth.luxury.notice.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class LvNotice {

    @Resource
    private MailService mailService;
    @Resource
    private RestTemplate restTemplate;

    public final String url = "https://secure.louisvuitton.cn/ajaxsecure/getStockLevel.jsp?storeLang=zhs-cn&pageType=storelocator_section&skuIdList=N58010&null&_=1586758087289";
    public final String cookie = "lvbmwe2=7B55B743BD8E9A18181427304683B571; lvbmwe1=4F7BF4EBE010104ACB93BBAC41BCAC2D; _qubitTracker=398zsnekute-0k8y032dk-8d6f874; qb_generic=:XFx4XbG:.louisvuitton.cn; ak_bmsc=1F9EEF3014FF223111680089C26D34303ADE1E3D73360000FCEF935E087ED265~pl+FufVSBbKwEyr/eFyXHRrm8Qahv/z/5KJOhLPsB2DVjRsWEZvKhNma5l+gmGiTgYdb82qPeeg9Uiein2v4gSSesp6T7anpHO+MRS89FEOg+s3s93io7EHtJTo/kfJQ2IEryJh46cTE1XNE4m8RXSDzuBdHkaMcCBSE0GnO4fU4RmEEtti/AHq72DZMkeUqV2/OoYW3VXl3wDh8wBkI4rdI6VqBWS0frqBUq9puBisS9e6ebIsZ4OESKvaGbObw7F; _ga=GA1.2.62421431.1586753538; _gid=GA1.2.1801886594.1586753538; Qs_lvt_187854=1586753539; _gcl_au=1.1.1525261611.1586753540; s_cc=true; AMCV_A69F5C6655799DC57F000101%40AdobeOrg=-2017484664%7CMCMID%7C81236362444033999403268747437226398815%7CMCAID%7CNONE%7CMCAAMLH-1587358339%7C11%7CMCAAMB-1587358339%7Cj8Odv6LonN4r3an7LhD3WZrU1bUpAkFkkiY1ncBR96t2PTI; Hm_lvt_70ef235947285936a07191ef669a6813=1586753540; _cs_c=1; kameleoonVisitorCode=_js_hp6gfrofeu3pyg1s; Hm_lvt_dde6ba2851f3db0ddc415ce0f895822e=1586755502; Hm_lpvt_dde6ba2851f3db0ddc415ce0f895822e=1586755528; lvbmwe1=4F7BF4EBE010104ACB93BBAC41BCAC2D; bm_sz=C38332B01D305DF8AC2C1A41C1CCE66C~YAAQZx7eOkNHNF1xAQAAwG8mcgc+8wbtBvousB+DVxcTvzN2wHiXF8pzzwXVaS/zajdM5KIRkpJcQuFHArJriDouX+sy3lphRIpgQK3gOD0vqt5fG3dqEQ5E1BkxBD1f2CvedgbJ06d6xXdrBqDhJ2gE6CLcjPMBm1hmQkaLILL1UmqzIzitTD6tSBwk3ocT6VAGWB0=; JSESSIONID=MXJF2sFvCDq5TOpz0BRTBAkv.front21-prc; ATG_SESSION_ID=MXJF2sFvCDq5TOpz0BRTBAkv.front21-prc; anonymous_session=true; s_sq=%5B%5BB%5D%5D; OPTOUTMULTI=0:0%7Cc1:0%7Cc2:0%7Cc4:0%7Cc3:0; Qs_pv_187854=2990730398490531300%2C4228689083148360700%2C4352845594649111600%2C3944324527735301000%2C4157104837489458700; Hm_lpvt_70ef235947285936a07191ef669a6813=1586758065; _cs_id=8374f30d-fe99-a99c-d683-15470f590d33.1586753539.2.1586758065.1586758058.1.1620917539906.Lax.0; _cs_s=3.1; qb_session=3:1:16:EYai=D:0:XFyJnpW:0:0:0:0:.louisvuitton.cn; _abck=6E6735898DAD9CBD643AE266E93D2D00~0~YAAQZx7eOkZMNF1xAQAAo6YmcgMeAzd2TGvP2N/qFBP4AtuytJvZ/ESIImDCG4PDhjdRxiq9NOU6XUlLbP4qJ69fgh4YSJpz2SnCkp7AGbiLEfwcR0/niiYLnSlyLJ/4OoNvqszMps4ryU69etWvA2j6b2YNaLuiXRzA1fxHfqvn+ggCeeO1TbV/CsDS7oMgat2hj9LqtyS+r+oqce7zaA6T9cJXdGoBJuMi4Pt3eA2TMhMGJaJr01KI/PA4DEHB6LqFA/OIq5ltMOHPucg76HA5OtfgGGs1BYbLFrxJkKr04Vm0VgugqDgdhJkLAOpvdm3yTOtCSroINs1/~-1~1-GqXuzDNmeX-10000-100-3000-1||1-mHsvYwXOPl-2250-100-3000-2||~-1; lvbmwe2=CFB3A5D0E04B04F2F6A5B8146CDD07AD; bm_sv=CC960A25E3E8F87D9EE3439856524412~GZRsgWxHid2p+HRPeL6/yrL8X9eQFwMxQtIfOvDIXvagyi8OFTqEBrROSPIUrGAHPGDgLNvQnICKnmTUXjbeKrYccAGpx8YUqAMAo1d0Vb8Ep6FRhwqU0rFk6o/7AInJXskjwrxDxoJoS7HklEpXb45y8QoUJwWxvfdDqYc8mEE=; qb_permanent=398zsnekute-0k8y032dk-8d6f874:19:19:1:1:0::0:1:0:Bek/AC:BelAGw:A::::58.40.83.146:shanghai:7391:china:CN:31.24:121.48:shanghai:156073:shanghai%20shi:35611:migrated|1586758056620:EYai==T=CCFu=Cl::XFyJvOv:XFx4YkY:0:0:0::0:0:.louisvuitton.cn:0; utag_main=v_id:017171e1850300344318dd96a32404079005807101274$_sn:2$_se:10$_ss:0$_st:1586759887942$dc_visit:2$ses_id:1586758055835%3Bexp-session$_pn:4%3Bexp-session$dc_event:9%3Bexp-session$dc_region:eu-central-1%3Bexp-session";

    @Scheduled(cron = "0/1 * * * * ?")
    public void aTask() {


        try {
            String result = HttpRequest.post(url)
                    .cookie(cookie)
                    //超时，毫秒
                    .timeout(20000)
                    .execute()
                    .body();

//            this.sendEmail();
            int a = 1;
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(DateUtil.now() + "*********A任务每1秒执行一次进入测试");
    }

    private void sendEmail() {
        String to = "thassange@163.com";
        String subject = "Springboot 发送简单文本邮件";
        String content = "<p>第一封 Springboot 简单文本邮件</p>";
        mailService.sendSimpleTextMail(to, subject, content);
    }
}
