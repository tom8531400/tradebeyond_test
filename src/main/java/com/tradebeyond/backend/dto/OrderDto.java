package com.tradebeyond.backend.dto;

import com.tradebeyond.backend.bo.OrderBo;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long productId;

    private Integer orderAmount;


    public OrderBo toBo(){
        OrderBo bo = new OrderBo();
        bo.setUserId(userId);
        bo.setProductId(productId);
        bo.setOrderAmount(orderAmount);
        return bo;
    }


}
