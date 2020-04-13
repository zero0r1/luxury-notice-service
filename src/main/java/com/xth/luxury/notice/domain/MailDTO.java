package com.xth.luxury.notice.domain;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MailDTO {
    private String from;
    private String replyTo;
    private String[] to;
    private String[] cc;
    private String[] bcc;
    private LocalDate sentDate;
    private String subject;
    private String text;
    private String[] filenames;
}
