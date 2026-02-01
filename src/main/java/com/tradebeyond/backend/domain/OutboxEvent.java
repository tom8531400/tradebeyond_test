package com.tradebeyond.backend.domain;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * Outbox Event Table
 * outbox_event
 */
@Data
public class OutboxEvent implements Serializable {
    /**
     * PK
     */
    private Long id;

    /**
     * 全域唯一事件ID
     */
    private String eventId;

    /**
     * 事件類型，如 ORDER_CREATED
     */
    private String eventType;

    /**
     * 訂單ID
     */
    private Long orderId;

    /**
     * 使用者ID
     */
    private Long userId;

    /**
     * 事件內容（payload）
     */
    private Object message;

    /**
     * 狀態：0=NEW, 1=SENT, 2=RETRY, 3=DEAD
     */
    private Integer status;

    /**
     * 重試次數
     */
    private Integer retryCount;

    /**
     * 下次可重試時間
     */
    private Date nextRetryAt;

    /**
     * 建立時間
     */
    private Date createTime;

    /**
     * 更新時間
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}