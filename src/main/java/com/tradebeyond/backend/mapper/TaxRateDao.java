package com.tradebeyond.backend.mapper;

import com.tradebeyond.backend.bo.RateBo;
import com.tradebeyond.backend.bo.TaxRateBo;
import com.tradebeyond.backend.domain.TaxRate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface TaxRateDao {
    int deleteByPrimaryKey(Long id);

    int insert(TaxRateBo record);

    int insertSelective(TaxRate record);

    TaxRate selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TaxRate record);

    int updateByPrimaryKey(TaxRate record);

    int countByVersion(Integer version);

    int batchInsertTaxRate(@Param("list") List<TaxRate> list);
}