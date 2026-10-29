package com.monitor.dao;

import com.monitor.entity.Application;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 应用数据访问接口
 */
@Mapper
public interface ApplicationMapper {

    /**
     * 根据应用Key查询应用
     * 
     * @param appKey 应用Key
     * @return 应用信息
     */
    Application selectOneByAppKey(@Param("appKey") String appKey);
}
