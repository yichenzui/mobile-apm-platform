package com.monitor.repository;

import com.monitor.entity.Application;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 应用数据访问接口
 */
@Repository
@Mapper
public interface ApplicationRepository extends BaseMapper<Application> {
    // 可以在这里添加自定义查询方法
}
