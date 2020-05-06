package com.xth.luxury.notice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LvAddGoodsRespDTO {
    private Integer id;
    private String sku;
    private String url;
    private String title;
    private String alertCount;
}
