package com.monitor;

import com.monitor.config.SpringDocFixConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 移动端应用性能监控平台启动类
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@MapperScan({"com.monitor.dao", "com.monitor.repository"})
@Import(SpringDocFixConfig.class)
public class MonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitorApplication.class, args);
    }


}


