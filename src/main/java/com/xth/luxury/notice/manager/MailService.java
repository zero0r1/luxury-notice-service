package com.xth.luxury.notice.manager;

import cn.hutool.log.StaticLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 功能描述:
 *
 * @author shawn
 * @date 2020/4/13 3:59 下午
 */
@Service
public class MailService {

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    private JavaMailSender mailSender;

    /**
     * 发送简单文本邮件
     *
     * @param to
     * @param subject
     * @param content
     */
    public void sendSimpleTextMail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            message.setFrom(from);
            mailSender.send(message);
            StaticLog.info("【文本邮件】成功发送！to={}", to);
        } catch (Exception e) {
            StaticLog.error(e.getCause(), "sendSimpleTextMail");
        }
    }
}
