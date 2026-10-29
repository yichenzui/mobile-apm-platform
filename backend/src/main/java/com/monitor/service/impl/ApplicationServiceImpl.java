package com.monitor.service.impl;

import com.monitor.entity.Application;
import com.monitor.repository.ApplicationRepository;
import com.monitor.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 应用服务实现类
 */
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;

    @Override
    public List<Application> getAllApplications() {
        return applicationRepository.selectAll();
    }

    @Override
    public Application getApplicationById(Long id) {
        return applicationRepository.selectOneById(id);
    }

    @Override
    public Application createApplication(Application application) {
        applicationRepository.insert(application);
        return application;
    }

    @Override
    public Application updateApplication(Long id, Application application) {
        application.setId(id);
        applicationRepository.update(application);
        return application;
    }

    @Override
    public void deleteApplication(Long id) {
        applicationRepository.deleteById(id);
    }
}
