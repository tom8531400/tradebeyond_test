package com.tradebeyond.backend.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * product
 */
@Data
public class Product implements Serializable {
    private Long productId;

    private Long productCategoryId;

    private BigDecimal unitPrice;

    private static final long serialVersionUID = 1L;
}