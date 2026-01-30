package com.tradebeyond.backend.mapper;

import com.tradebeyond.backend.bo.UsersBo;
import com.tradebeyond.backend.domain.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UsersDao {
    int deleteByPrimaryKey(@Param("userId") Long userId);

    int insert(Users record);

    int insertSelective(Users record);

    UsersBo selectByPrimaryKey(@Param("userId") Long userId);

    int updateByPrimaryKeySelective(Users record);

    int updateByPrimaryKey(Users record);
}