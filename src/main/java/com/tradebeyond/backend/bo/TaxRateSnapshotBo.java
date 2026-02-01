package com.tradebeyond.backend.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxRateSnapshotBo implements Serializable {
    private static final long serialVersionUID = 1L;

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

}
