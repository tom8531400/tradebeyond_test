package com.tradebeyond.backend.domain;

import java.io.Serializable;
import lombok.Data;

/**
 * orders
 */
@Data
public class Orders implements Serializable {
    private Long orderId;

    private Long userId;

    private Long productId;

    private Integer orderAmount;

    private static final long serialVersionUID = 1L;
}