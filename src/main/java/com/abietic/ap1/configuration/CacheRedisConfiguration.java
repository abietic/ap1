package com.abietic.ap1.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class CacheRedisConfiguration {
    @Autowired
    private CacheRedisProperties cacheRedisProperty;


    @Bean(name = "cacheRedisConnectionFactory")
    public RedisConnectionFactory userRedisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(cacheRedisProperty.getHost(), cacheRedisProperty.getPort());
        configuration.setPassword(cacheRedisProperty.getPassword());
        LettuceConnectionFactory redisConnectionFactory = new LettuceConnectionFactory(configuration);
        // redisConnectionFactory.setHostName(redis1Property.getHost());
        // redisConnectionFactory.setPort(redis1Property.getPort());
        // redisConnectionFactory.setDatabase(redis1Property.getDatabase());
        // redisConnectionFactory.setPoolConfig(getPoolConfig());
        return redisConnectionFactory;
    }

    // private JedisPoolConfig getPoolConfig() {
    //     JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
    //     jedisPoolConfig.setMaxIdle(8);
    //     jedisPoolConfig.setMinIdle(1);
    //     jedisPoolConfig.setMaxTotal(8);
    //     return jedisPoolConfig;
    // }

    @Bean(name = "cacheRedisStringRedisTemplate")
    public StringRedisTemplate userStringRedisTemplate(@Qualifier("cacheRedisConnectionFactory") RedisConnectionFactory cf) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(cf);
        return stringRedisTemplate;
    }

    @Bean(name = "cacheRedisRedisTemplate")
    public RedisTemplate userRedisTemplate(@Qualifier("cacheRedisConnectionFactory") RedisConnectionFactory cf) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(cf);
        //setSerializer(stringRedisTemplate);
        return stringRedisTemplate;
    }
}
