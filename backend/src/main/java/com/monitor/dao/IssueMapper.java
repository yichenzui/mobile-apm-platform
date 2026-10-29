package com.monitor.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.monitor.dto.IssueQueryDTO;
import com.monitor.entity.Issue;
import com.mybatisflex.core.BaseMapper;

/**
 * 问题 Mapper 接口
 */
@Mapper
public interface IssueMapper extends BaseMapper<Issue> {
    // 这里可以添加自定义查询方法

    /**
     * 根据查询条件查询问题列表
     *
     * @param queryDTO 查询条件
     * @return 问题列表
     */
    List<Issue> selectList(IssueQueryDTO queryDTO);

    /**
     * 根据查询条件统计问题数量（与 selectList 条件保持一致）
     *
     * @param queryDTO 查询条件
     * @return 数量
     */
    int selectCount(IssueQueryDTO queryDTO);
}
