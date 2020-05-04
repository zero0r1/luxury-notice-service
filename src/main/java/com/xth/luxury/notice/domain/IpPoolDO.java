package com.xth.luxury.notice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IpPoolDO {
    /**
     * 自增id
     */
    private Integer id;
    /**
     * ip
     */
    private String ip;
    /**
     * 端口
     */
    private Integer port;
}
