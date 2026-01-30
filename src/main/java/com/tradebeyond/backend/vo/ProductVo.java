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
public class ProductVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long productId;

    private Long productCategoryId;

    private BigDecimal unitPrice;
}
