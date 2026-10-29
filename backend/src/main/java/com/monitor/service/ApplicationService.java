package com.monitor.service;

import com.monitor.entity.Application;
import java.util.List;

/**
 * 应用服务接口
 */
public interface ApplicationService {

    /**
     * 获取所有应用
     * @return 应用列表
     */
    List<Application> getAllApplications();

    /**
     * 根据ID获取应用
     * @param id 应用ID
     * @return 应用对象
     */
    Application getApplicationById(Long id);

    /**
     * 创建应用
     * @param application 应用对象
     * @return 创建的应用对象
     */
    Application createApplication(Application application);

    /**
     * 更新应用
     * @param id 应用ID
     * @param application 应用对象
     * @return 更新后的应用对象
     */
    Application updateApplication(Long id, Application application);

    /**
     * 删除应用
     * @param id 应用ID
     */
    void deleteApplication(Long id);
}
