package com.monitor.dao;

import org.apache.ibatis.annotations.Mapper;

import com.monitor.dto.AlertQueryDTO;
import com.monitor.entity.AlertInstance;
import com.mybatisflex.core.BaseMapper;

import java.util.List;

/**
 * 告警实例 Mapper 接口
 */
@Mapper
public interface AlertInstanceMapper extends BaseMapper<AlertInstance> {
    
    /**
     * 根据查询条件查询告警实例列表
     *
     * @param queryDTO 查询条件
     * @return 告警实例列表
     */
    List<AlertInstance> selectList(AlertQueryDTO queryDTO);

    /**
     * 根据查询条件统计告警数量（与 selectList 条件保持一致）
     *
     * @param queryDTO 查询条件
     * @return 数量
     */
    int selectCount(AlertQueryDTO queryDTO);
}
