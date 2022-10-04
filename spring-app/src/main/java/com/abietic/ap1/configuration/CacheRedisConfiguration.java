package com.abietic.ap1.configuration;

import org.joda.time.DateTime;
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
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.abietic.ap1.serializer.JodaDateTimeJsonDeserializer;
import com.abietic.ap1.serializer.JodaDateTimeJsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Configuration
public class CacheRedisConfiguration {
    @Autowired
    private CacheRedisProperties cacheRedisProperty;


    // @Bean(name = "cacheRedisConnectionFactory")
    // public RedisConnectionFactory userRedisConnectionFactory() {
    //     RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(cacheRedisProperty.getHost(), cacheRedisProperty.getPort());
    //     configuration.setPassword(cacheRedisProperty.getPassword());
    //     LettuceConnectionFactory redisConnectionFactory = new LettuceConnectionFactory(configuration);
    //     // redisConnectionFactory.setHostName(redis1Property.getHost());
    //     // redisConnectionFactory.setPort(redis1Property.getPort());
    //     // redisConnectionFactory.setDatabase(redis1Property.getDatabase());
    //     // redisConnectionFactory.setPoolConfig(getPoolConfig());
    //     return redisConnectionFactory;
    // }

    // private JedisPoolConfig getPoolConfig() {
    //     JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
    //     jedisPoolConfig.setMaxIdle(8);
    //     jedisPoolConfig.setMinIdle(1);
    //     jedisPoolConfig.setMaxTotal(8);
    //     return jedisPoolConfig;
    // }

    @Bean(name = "cacheRedisStringRedisTemplate")
    public StringRedisTemplate userStringRedisTemplate(@Qualifier("clusterCacheRedisConnectionFactory") RedisConnectionFactory cf) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(cf);
        return stringRedisTemplate;
    }

    @Bean(name = "cacheRedisRedisTemplate")
    public RedisTemplate<Object, Object> userRedisTemplate(@Qualifier("clusterCacheRedisConnectionFactory") RedisConnectionFactory cf) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(cf);
        // key的序列化直接使用toString
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // value的序列化使用Jackson的序列化把对象转化成json
        Jackson2JsonRedisSerializer<Object> valueSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        // 自定义相应的类型对象json映射
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(DateTime.class, new JodaDateTimeJsonSerializer());
        simpleModule.addDeserializer(DateTime.class, new JodaDateTimeJsonDeserializer());
        objectMapper.registerModule(simpleModule);
        // 这个设置会使对象在转化为json时保存类型信息，这样才能正常反序列化
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        valueSerializer.setObjectMapper(objectMapper);
        
        redisTemplate.setValueSerializer(valueSerializer);
        return redisTemplate;
    }
}
