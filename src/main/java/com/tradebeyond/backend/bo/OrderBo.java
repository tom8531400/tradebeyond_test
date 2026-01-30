package com.tradebeyond.backend.bo;

import com.sun.istack.internal.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderBo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long orderId;

    @NotNull
    private Long userId;

    @NotNull
    private Long productId;

    @NotNull
    private Integer orderAmount;
}
