package com.tradebeyond.backend.mapper;

import com.tradebeyond.backend.bo.TaxRateSnapshotBo;
import com.tradebeyond.backend.domain.TaxRateSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Mapper
public interface TaxRateSnapshotDao {
    int deleteByPrimaryKey(Long id);

    int insert(TaxRateSnapshotBo record);

    int insertSelective(TaxRateSnapshot record);

    TaxRateSnapshot selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TaxRateSnapshot record);

    int updateByPrimaryKey(TaxRateSnapshot record);

    int insertIfVersionMatch(@Param("eventId") String eventId, @Param("version") Integer version, @Param("createdAt") Date createdAt);
}