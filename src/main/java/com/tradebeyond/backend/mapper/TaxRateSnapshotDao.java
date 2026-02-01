package com.tradebeyond.backend.mapper;

import com.tradebeyond.backend.bo.TaxRateSnapshotBo;
import com.tradebeyond.backend.domain.TaxRateSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
public interface TaxRateSnapshotDao {
    int deleteByPrimaryKey(Long id);

    int insert(TaxRateSnapshotBo record);

    int insertSelective(TaxRateSnapshot record);

    TaxRateSnapshot selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TaxRateSnapshot record);

    int updateByPrimaryKey(TaxRateSnapshot record);
}