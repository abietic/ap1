package com.abietic.ap1.configuration;

import com.abietic.ap1.serializer.JodaDateTimeJsonDeserializer;
import com.abietic.ap1.serializer.JodaDateTimeJsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.DefaultBaseTypeLimitingValidator;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.jackson2.WebServletJackson2Module;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

// 这是一个给sessionRepository做序列化的bean
@Component
public class JSONRedisSerializer extends Jackson2JsonRedisSerializer<Object> {

    public JSONRedisSerializer() {
        super(Object.class);
        // 自定义相应的类型对象json映射
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        
        // 时间类的序列化与反序列化
        simpleModule.addSerializer(DateTime.class, new JodaDateTimeJsonSerializer());
        simpleModule.addDeserializer(DateTime.class, new JodaDateTimeJsonDeserializer());
        objectMapper.registerModule(simpleModule);

        // 用来为savedRequest进行json的序列化与反序列化
        // 但是不知道为什么还是会引起序列化与反序列化的异常
        objectMapper.registerModule(new WebServletJackson2Module());

        // 这个设置会使对象在转化为json时保存类型信息，这样才能正常反序列化
        // objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance);
        setObjectMapper(objectMapper);
    }

}