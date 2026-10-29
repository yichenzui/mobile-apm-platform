package com.monitor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis 连接健康检查
 * 启动时检查 Redis 是否可用，不可用时记录警告但不阻止启动
 */
@Component
@ConditionalOnProperty(name = "spring.data.redis.enabled", havingValue = "true")
public class RedisHealthCheck implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RedisHealthCheck.class);

    private final StringRedisTemplate redisTemplate;

    public RedisHealthCheck(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            log.info("✓ Redis 连接正常 (localhost:6379)");
        } catch (Exception e) {
            log.warn("⚠ Redis 连接失败 (localhost:6379): {} - 缓存功能将不可用", e.getMessage());
            log.warn("提示: 如果不需要缓存功能，可以忽略此警告。如需启用请先启动 Redis。");
        }
    }
}
