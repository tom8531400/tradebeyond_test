package com.tradebeyond.backend.mapper;

import com.tradebeyond.backend.bo.OrderBo;
import com.tradebeyond.backend.domain.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface OrdersDao {
    int deleteByPrimaryKey(@Param("orderId") Long orderId);

    int insert(OrderBo record);

    int insertSelective(Orders record);

    OrderBo selectByPrimaryKey(@Param("orderId") Long orderId);

    int updateByPrimaryKeySelective(@Param("record") OrderBo record);

    int updateByPrimaryKey(OrderBo record);

    List<Long> selectUserOrderIds(@Param("userId") Long userId);

    int deleteByOrderIds(@Param("list") List<Long> list);

    List<OrderBo> selectByUserId(@Param("userId") Long userId);
}