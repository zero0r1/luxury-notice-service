package com.xth.luxury.notice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LvAddGoodsReqDTO {
    private String sku;
    private String url;
    private String title;
}
