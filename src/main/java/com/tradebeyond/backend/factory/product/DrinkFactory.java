package com.tradebeyond.backend.factory.product;

import com.tradebeyond.backend.bo.OrderBo;
import com.tradebeyond.backend.bo.ProductBo;
import com.tradebeyond.backend.bo.ProductCategoryBo;
import com.tradebeyond.backend.enums.StatusCode;
import com.tradebeyond.backend.exception.BusinessException;
import com.tradebeyond.backend.mapper.ProductCategoryDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class DrinkFactory implements ProductFactory{
    @Autowired
    private ProductCategoryDao productCategoryDao;

    @Override
    public BigDecimal calculateTotalCost(ProductBo product, OrderBo orderBo) {
        Integer orderAmount = orderBo.getOrderAmount();
        BigDecimal unitPrice = product.getUnitPrice();

        Long productCategoryId = product.getProductCategoryId();
        ProductCategoryBo productCategoryBo = productCategoryDao.selectByPrimaryKey(productCategoryId);
        if(productCategoryBo == null) {
            log.error("productCategoryBo is null");
            throw new BusinessException(StatusCode.PARAMS_INVALID);
        }
        BigDecimal taxRate = productCategoryBo.getTaxRate();

        return BigDecimal.valueOf(orderAmount).multiply(unitPrice).multiply(BigDecimal.ONE.add(taxRate));

    }
}
