package com.tradebeyond.backend.domain;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 稅率快照事件表
 * tax_rate_snapshot
 */
@Data
public class TaxRateSnapshot implements Serializable {
    /**
     * 主鍵
     */
    private Long id;

    /**
     * Webhook 事件唯一ID（幂等）
     */
    private String eventId;

    /**
     * 全域快照版本號
     */
    private Integer version;

    /**
     * 接收並成功處理時間
     */
    private Date createdAt;

    private static final long serialVersionUID = 1L;
}