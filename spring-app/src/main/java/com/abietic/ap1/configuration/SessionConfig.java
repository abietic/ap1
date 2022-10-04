/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.abietic.ap1.configuration;

import java.time.Duration;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.session.RedisSessionProperties;
import org.springframework.boot.autoconfigure.session.SessionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
// import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.data.redis.RedisSessionRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RedisSessionProperties.class)
@EnableSpringHttpSession
public class SessionConfig {

	private final SessionProperties sessionProperties;

	private final RedisSessionProperties redisSessionProperties;

	@Autowired
	@Qualifier("clusterCacheRedisConnectionFactory")
	private RedisConnectionFactory redisConnectionFactory;

	@Autowired
	private JSONRedisSerializer sessionSerializer;

	// public SessionConfig(SessionProperties sessionProperties,
	// RedisSessionProperties redisSessionProperties,
	// ObjectProvider<RedisConnectionFactory> redisConnectionFactory) {
	public SessionConfig(SessionProperties sessionProperties, RedisSessionProperties redisSessionProperties) {
		this.sessionProperties = sessionProperties;
		this.redisSessionProperties = redisSessionProperties;
		// this.redisConnectionFactory = redisConnectionFactory.getObject();
	}

	@Bean
	public RedisOperations<String, Object> sessionRedisOperations() {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(this.redisConnectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		// 为了保证session存储的对象能够序列化，使用json进行序列化
		// redisTemplate.setDefaultSerializer(this.sessionSerializer);
		return redisTemplate;
	}

	@Bean
	public RedisSessionRepository sessionRepository(RedisOperations<String, Object> sessionRedisOperations) {
		RedisSessionRepository sessionRepository = new RedisSessionRepository(sessionRedisOperations);
		Duration timeout = this.sessionProperties.getTimeout();
		if (timeout != null) {
			sessionRepository.setDefaultMaxInactiveInterval(timeout);
		}
		sessionRepository.setKeyNamespace(this.redisSessionProperties.getNamespace());
		sessionRepository.setFlushMode(this.redisSessionProperties.getFlushMode());
		sessionRepository.setSaveMode(this.redisSessionProperties.getSaveMode());
		return sessionRepository;
	}

	// @Bean
	// RedisSerializer<Object> springSessionDefaultRedisSerializer() {
	// return this.sessionSerializer;
	// }
}
