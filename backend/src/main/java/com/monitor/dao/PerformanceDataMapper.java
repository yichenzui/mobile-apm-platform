package com.monitor.dao;

import com.mybatisflex.core.BaseMapper;
import com.monitor.entity.PerformanceDataNew;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 性能数据 Mapper 接口
 */
@Mapper
public interface PerformanceDataMapper extends BaseMapper<PerformanceDataNew> {

    /**
     * 批量插入性能数据
     *
     * @param list 性能数据列表
     * @return 插入的记录数
     */
    int insertBatch(@Param("list") List<PerformanceDataNew> list);
}
