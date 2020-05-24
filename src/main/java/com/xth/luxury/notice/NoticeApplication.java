package com.xth.luxury.notice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableScheduling
@EnableAsync
@MapperScan("com.xth.luxury.notice.mapper")
// 扫描cn.hutool.extra.spring包下所有类并注册之
@ComponentScan(basePackages={"com.xth.luxury.notice","cn.hutool.extra.spring"})
@Import(cn.hutool.extra.spring.SpringUtil.class)
public class NoticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoticeApplication.class, args);
    }

}
