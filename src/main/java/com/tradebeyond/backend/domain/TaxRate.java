package com.tradebeyond.backend.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 即時稅率表
 * tax_rate
 */
@Data
public class TaxRate implements Serializable {
    /**
     * 主鍵
     */
    private Long id;

    /**
     * 事件唯一ID（幂等）
     */
    private String eventId;

    /**
     * 地區代碼（如 TW, US）
     */
    private String region;

    /**
     * 稅率，例如 0.050000
     */
    private BigDecimal rate;

    /**
     * 版本號，用於防亂序
     */
    private Integer version;

    /**
     * 建立時間
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}