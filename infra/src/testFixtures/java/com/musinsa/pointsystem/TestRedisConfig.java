package com.musinsa.pointsystem;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import redis.embedded.RedisServer;

import java.io.IOException;

/**
 * 테스트용 Redis 설정
 * - Embedded Redis 시작/종료
 * - Redisson 클라이언트 생성
 */
@TestConfiguration
public class TestRedisConfig {

    @Value("${redis.embedded.port:6371}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException {
        redisServer = new RedisServer(redisPort);
        try {
            redisServer.start();
        } catch (Exception e) {
            // Redis server already running
        }
    }

    @PreDestroy
    public void stopRedis() throws IOException {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
        }
    }

    @Bean
    @Primary
    public RedissonClient testRedissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://localhost:" + redisPort)
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(2);
        return Redisson.create(config);
    }
}
