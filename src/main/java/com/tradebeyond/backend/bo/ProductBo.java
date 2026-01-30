package com.tradebeyond.backend.bo;

import com.tradebeyond.backend.vo.ProductVo;
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
public class ProductBo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long productId;

    private Long productCategoryId;

    private BigDecimal unitPrice;

    public ProductVo toVo(){
        ProductVo vo = new ProductVo();
        vo.setProductId(productId);
        vo.setProductCategoryId(productCategoryId);
        vo.setUnitPrice(unitPrice);
        return vo;
    }
}
