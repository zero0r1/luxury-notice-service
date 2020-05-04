package com.xth.luxury.notice.domain;

import lombok.Builder;
import lombok.Data;

import java.net.InetSocketAddress;

@Data
@Builder
public class SelfInetSocketAddress {
    private InetSocketAddress inetSocketAddress;
    private Integer id;
}
