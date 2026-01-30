package com.tradebeyond.backend.mapper;

import com.tradebeyond.backend.bo.ProductCategoryBo;
import com.tradebeyond.backend.domain.ProductCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
public interface ProductCategoryDao {
    int deleteByPrimaryKey(Long categoryId);

    int insert(ProductCategory record);

    int insertSelective(ProductCategory record);

    ProductCategoryBo selectByPrimaryKey(@Param("categoryId") Long categoryId);

    int updateByPrimaryKeySelective(ProductCategory record);

    int updateByPrimaryKey(ProductCategory record);
}