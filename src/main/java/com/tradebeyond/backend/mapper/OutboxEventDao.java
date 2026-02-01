package com.tradebeyond.backend.mapper;

import com.tradebeyond.backend.bo.OutboxEventBo;
import com.tradebeyond.backend.bo.SendMqOrderBo;
import com.tradebeyond.backend.domain.OutboxEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface OutboxEventDao {
    int deleteByPrimaryKey(Long id);

    int insert(OutboxEventBo record);

    int insertSelective(OutboxEvent record);

    OutboxEvent selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(OutboxEventBo record);

    int updateByPrimaryKey(OutboxEvent record);

    List<OutboxEventBo> selectRetryEvents(@Param("bo") SendMqOrderBo bo);

    List<OutboxEventBo> selectDeadEvents(@Param("bo") SendMqOrderBo bo);

    List<OutboxEventBo> selectByIdsAndStatus(@Param("status") Integer status, @Param("list") List<OutboxEventBo> outboxEventBos);

    int updateStatusByEventId(@Param("status") Integer status, @Param("eventId") String eventId, @Param("now") Date now, @Param("outBoxStatus") Integer outBoxStatus);

    int updateRetryByEventId(@Param("status") Integer status, @Param("retryAt") Date retryAt, @Param("eventId") String eventId, @Param("now") Date now);

    int batchUpdateStatus(@Param("list") List<OutboxEventBo> outboxEventBos,
                          @Param("status") Integer status,
                          @Param("now") Date now);

    int batchUpdateDeadStatus(@Param("list") List<OutboxEventBo> outboxEventBos,
                              @Param("status") Integer status,
                              @Param("now") Date now);

    int updateOrderEventStatus(@Param("eventType") String eventType,@Param("now") Date now,@Param("eventId") String eventId, @Param("oldEventType") String oldEventType);
}