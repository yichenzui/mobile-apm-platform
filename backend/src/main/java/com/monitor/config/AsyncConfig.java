package com.monitor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步配置类
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 性能数据处理线程池
     */
    @Bean("performanceDataExecutor")
    public Executor performanceDataExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(10);
        // 最大线程数
        executor.setMaxPoolSize(20);
        // 队列容量
        executor.setQueueCapacity(200);
        // 线程名前缀
        executor.setThreadNamePrefix("performance-data-");
        // 拒绝策略：由调用线程处理该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 线程空闲时间
        executor.setKeepAliveSeconds(60);
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
