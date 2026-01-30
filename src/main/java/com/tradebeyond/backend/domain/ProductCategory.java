package com.tradebeyond.backend.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * product_category
 */
@Data
public class ProductCategory implements Serializable {
    private Long categoryId;

    private String categoryName;

    private BigDecimal taxRate;

    private static final long serialVersionUID = 1L;
}