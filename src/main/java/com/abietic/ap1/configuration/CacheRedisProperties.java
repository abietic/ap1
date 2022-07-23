package com.abietic.ap1.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.cache-redis")
public class CacheRedisProperties extends RedisCommonProperty {

}
