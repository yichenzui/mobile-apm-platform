package com.monitor.service;

import com.monitor.dto.IssueQueryDTO;
import com.monitor.vo.IssueVO;

import java.util.List;

/**
 * 问题服务接口
 */
public interface IssueService {

    /**
     * 获取问题列表
     *
     * @param queryDTO 查询参数
     * @return 问题列表
     */
    List<IssueVO> getIssueList(IssueQueryDTO queryDTO);

    /**
     * 统计符合条件的总条数（用于分页）
     *
     * @param queryDTO 查询参数
     * @return 总条数
     */
    int countIssues(IssueQueryDTO queryDTO);

    /**
     * 获取问题详情
     *
     * @param id 问题ID
     * @return 问题详情
     */
    IssueVO getIssueDetail(Long id);

    /**
     * 处理问题
     *
     * @param id 问题ID
     * @param assignee 处理人
     * @param note 处理说明
     */
    void processIssue(Long id, String assignee, String note);

    /**
     * 解决问题
     *
     * @param id 问题ID
     * @param assignee 处理人
     * @param note 解决方案
     */
    void resolveIssue(Long id, String assignee, String note);
}
