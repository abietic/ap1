package com.abietic.ap1.configuration;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClusterConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration.LettuceClientConfigurationBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.abietic.ap1.serializer.JodaDateTimeJsonDeserializer;
import com.abietic.ap1.serializer.JodaDateTimeJsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.lettuce.core.SocketOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;

import java.time.Duration;
import java.util.List;

@Configuration
public class ClusterRedisConfiguration {
    @Autowired
    private ClusterRedisProperties clusterRedisProperties;

    @Primary
    @Bean(name = "clusterCacheRedisConnectionFactory")
    @Scope("prototype")
    public RedisConnectionFactory userRedisConnectionFactory(LettuceClientConfiguration clientConfig) {
        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration();
        clusterConfiguration.setPassword(clusterRedisProperties.getPassword());
        List<String> hosts = clusterRedisProperties.getHosts();
        List<Integer> ports = clusterRedisProperties.getPorts();
        int nodeCount = hosts.size();
        for (int i = 0; i < nodeCount; ++i) {
            clusterConfiguration.clusterNode(hosts.get(i), ports.get(i));
        }
        clusterConfiguration.setMaxRedirects(clusterRedisProperties.getMaxRedirects());
        LettuceConnectionFactory clusterFactory = new LettuceConnectionFactory(clusterConfiguration, clientConfig);
        return clusterFactory;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.lettuce.pool")
    public GenericObjectPoolConfig redisPool() {
        return new GenericObjectPoolConfig();
    }

    @Bean
    public LettuceClientConfiguration getPoolingClientConfig(GenericObjectPoolConfig poolConfig) {
        // 配置用于开启自适应刷新和定时刷新。如自适应刷新不开启，Redis集群变更时将会导致连接异常
        ClusterTopologyRefreshOptions clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enablePeriodicRefresh(Duration.ofSeconds(60))// 开启周期刷新(默认60秒)
                .enableAdaptiveRefreshTrigger(ClusterTopologyRefreshOptions.RefreshTrigger.ASK_REDIRECT)// 开启自适应刷新
                // .enableAdaptiveRefreshTriggers(RefreshTrigger.ASK_REDIRECT,RefreshTrigger.UNKNOWN_NODE)//
                // 开启自适应刷新
                .build();
        ClusterClientOptions clusterClientOptions = ClusterClientOptions.builder()
                .topologyRefreshOptions(clusterTopologyRefreshOptions)// 拓扑刷新
                .disconnectedBehavior(ClusterClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .autoReconnect(true)
                .socketOptions(SocketOptions.builder().keepAlive(true).build())
                .validateClusterNodeMembership(false)// 取消校验集群节点的成员关系
                .build();
        LettucePoolingClientConfiguration configuration = LettucePoolingClientConfiguration.builder()
                .clientOptions(clusterClientOptions).poolConfig(poolConfig).build();
        return configuration;
    }

    @Bean("checkSeqInitializedScript")
    public RedisScript<Boolean> loadCheckInitializedScript() {
        RedisScript<Boolean> script =  RedisScript.of(new ClassPathResource("check_seq_init.lua"), Boolean.class);
        return script;
    }

    // 莫名其妙的问题,脚本直接在redis-cli上运行都是正常的,到了spring上就会有问题
    // 下面两个方法应该都是返回值的转换上有问题
    // org.springframework.data.redis.connection.ReturnType中能支持的java类转换应该是Long,Boolean和List
    // 但是在执行初始化的时候如果使用Long而非Integer执行的返回值会变成null
    // 已知使用Integer对应的redis的returntype会变成byte[]应该是文本格式然后进入serializer中
    @Bean("seqInitializeScript")
    public RedisScript<Integer> loadSeqInitializeScript() {
        RedisScript<Integer> script =  RedisScript.of(new ClassPathResource("seq_init.lua"), Integer.class);
        return script;
    }

    // 这个之前使用Integer不行,换成Long后就又可以了
    // Long对应的redis returntype是INTGER应该是要求redis返回的是一个整数
    // 但是上下两个脚本除了这个脚本如果在键不存在时会直接返回一个-1外都是将返回值用tonumber包裹起来的应该没有什么区别才对
    @Bean("getSeqScript")
    public RedisScript<Long> loadGetSeqScript() {
        RedisScript<Long> script =  RedisScript.of(new ClassPathResource("get_seq.lua"), Long.class);
        return script;
    }

}
