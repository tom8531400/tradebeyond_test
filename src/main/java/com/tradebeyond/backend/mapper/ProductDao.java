package com.tradebeyond.backend.mapper;

import com.tradebeyond.backend.bo.ProductBo;
import com.tradebeyond.backend.domain.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Mapper
public interface ProductDao {
    int deleteByPrimaryKey(Long productId);

    int insert(Product record);

    int insertSelective(Product record);

    ProductBo selectByPrimaryKey(@Param("productId") Long productId);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    List<ProductBo> selectAllByProductId(@Param("list") Set<Long> productIds);
}