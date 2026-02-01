package com.tradebeyond.backend.factory.product;

import com.tradebeyond.backend.bo.OrderBo;
import com.tradebeyond.backend.bo.ProductBo;

import java.math.BigDecimal;

public interface ProductFactory {

    BigDecimal calculateTotalCost(ProductBo product, OrderBo orderBo);

}
