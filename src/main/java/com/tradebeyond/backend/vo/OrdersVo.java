package com.tradebeyond.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdersVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long orderId;

    private Long userId;

    private Long productId;

    private Integer orderAmount;

    private BigDecimal orderPrice;
}
