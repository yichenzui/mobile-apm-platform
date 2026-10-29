package com.monitor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;

/**
 * Provide a bean expected by springdoc to avoid missing-bean errors during startup.
 */
@Configuration
public class SpringDocFixConfig {

    @Bean
    public StandardReflectionParameterNameDiscoverer standardReflectionParameterNameDiscoverer() {
        return new StandardReflectionParameterNameDiscoverer();
    }
}
