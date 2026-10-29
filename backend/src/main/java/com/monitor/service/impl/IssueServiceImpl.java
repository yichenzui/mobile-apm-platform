package com.monitor.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.monitor.common.BizException;
import com.monitor.dao.IssueMapper;
import com.monitor.dto.IssueQueryDTO;
import com.monitor.entity.Issue;
import com.monitor.service.IssueService;
import com.monitor.vo.IssueVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 问题服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

    private final IssueMapper issueMapper;

    @Override
    public List<IssueVO> getIssueList(IssueQueryDTO queryDTO) {
        try {
            // 如果appKey为空字符串，直接返回空列表
            if (queryDTO.getAppKey() == null || queryDTO.getAppKey().trim().isEmpty()) {
                log.warn("获取问题列表: appKey为空，返回空列表");
                return new java.util.ArrayList<>();
            }
            
            // 查询问题列表
            List<Issue> issues = issueMapper.selectList(queryDTO);

            // 转换为VO对象
            if (issues == null || issues.isEmpty()) {
                return new java.util.ArrayList<>();
            }
            return issues.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取问题列表失败(可能是表未初始化)，返回空列表", e);
            // 表不存在或其他异常时返回空列表而非500错误
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public int countIssues(IssueQueryDTO queryDTO) {
        try {
            if (queryDTO.getAppKey() == null || queryDTO.getAppKey().trim().isEmpty()) {
                return 0;
            }
            return issueMapper.selectCount(queryDTO);
        } catch (Exception e) {
            log.error("统计问题数量失败", e);
            return 0;
        }
    }

    /**
     * 转换为VO对象
     */
    private IssueVO convertToVO(Issue issue) {
        IssueVO vo = new IssueVO();
        vo.setId(issue.getId());
        vo.setAppKey(issue.getAppKey());
        vo.setTitle(issue.getTitle());
        vo.setDescription(issue.getDescription());
        vo.setType(issue.getType());
        vo.setSeverity(issue.getSeverity());
        vo.setStatus(issue.getStatus());
        vo.setStackTrace(issue.getStackTrace());
        vo.setDeviceInfo(issue.getDeviceInfo());
        vo.setOccurrenceCount(issue.getOccurrenceCount());
        vo.setAffectedUserCount(issue.getAffectedUserCount());
        vo.setFirstOccurrenceTime(issue.getFirstOccurrenceTime());
        vo.setLastOccurrenceTime(issue.getLastOccurrenceTime());
        vo.setAssignee(issue.getAssignee());
        vo.setProcessNote(issue.getProcessNote());
        vo.setCreateTime(issue.getCreateTime());
        vo.setUpdateTime(issue.getUpdateTime());

        return vo;
    }

    @Override
    public IssueVO getIssueDetail(Long id) {
        try {
            Issue issue = issueMapper.selectOneById(id);
            if (issue == null) {
                throw new BizException("问题不存在，ID: " + id);
            }

            return convertToVO(issue);
        } catch (Exception e) {
            log.error("获取问题详情失败", e);
            throw new BizException("获取问题详情失败", e);
        }
    }

    @Override
    public void processIssue(Long id, String assignee, String note) {
        try {
            Issue issue = issueMapper.selectOneById(id);
            if (issue == null) {
                throw new BizException("问题不存在，ID: " + id);
            }

            issue.setStatus(1); // 处理中
            issue.setAssignee(assignee);
            issue.setProcessNote(note);
            issue.setProcessTime(LocalDateTime.now());
            issue.setUpdateTime(LocalDateTime.now());

            issueMapper.update(issue, true);

            log.info("问题处理成功，ID：{}，处理人：{}", id, assignee);
        } catch (Exception e) {
            log.error("处理问题失败", e);
            throw new BizException("处理问题失败", e);
        }
    }

    @Override
    public void resolveIssue(Long id, String assignee, String note) {
        try {
            Issue issue = issueMapper.selectOneById(id);
            if (issue == null) {
                throw new BizException("问题不存在，ID: " + id);
            }

            issue.setStatus(2); // 已解决
            issue.setAssignee(assignee);
            issue.setProcessNote(note);
            issue.setProcessTime(LocalDateTime.now());
            issue.setUpdateTime(LocalDateTime.now());

            issueMapper.update(issue, true);

            log.info("问题解决成功，ID：{}，处理人：{}", id, assignee);
        } catch (Exception e) {
            log.error("解决问题失败", e);
            throw new BizException("解决问题失败", e);
        }
    }
}
