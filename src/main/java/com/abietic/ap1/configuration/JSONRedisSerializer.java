package com.abietic.ap1.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.SerializationUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JSONRedisSerializer implements RedisSerializer<Object> {
    @Autowired
    private ObjectMapper mapper;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final String separator = "=";
    private final String classPrefix = "<";
    private final String classSuffix = ">";
    private final String classSeparator = ","; 
    private Pattern pattern; 
    public JSONRedisSerializer() {
        pattern = Pattern.compile("<(.*)>");  
    }
 
    /**
     * 获取class，包含集合类的泛型
     * <p>暂只支持最多两个泛型，同时集合内数据必须为同一个实现类，不可将泛型声明成父类</p>
     *
     * @param obj 将要序列化的对象
     * @return 没有泛型，形式为java.lang.String<>
     * <p>一个泛型，形式为java.lang.String<java.lang.String></p>
     * <p>两个个泛型，形式为java.lang.String<java.lang.String,java.lang.String></p>
     */
    private String getBegin(Object obj) {
        StringBuilder builder = new StringBuilder(obj.getClass().toString().substring(6) + classPrefix);
        if (obj instanceof List) {
            List list = ((List) obj);
            if (!list.isEmpty()) {
                Object temp = list.get(0);
                builder.append(temp.getClass().toString().substring(6));
            }
        } else if (obj instanceof Map) {
            Map map = ((Map) obj);
            Iterator iterator = map.keySet().iterator();
            if (iterator.hasNext()) {
                Object key = iterator.next();
                Object value = map.get(key);
                builder.append(key.getClass().toString().substring(6)).append(classSeparator).append(value.getClass().toString().substring(6));
            }
        } else if (obj instanceof Set) {
            Set set = ((Set) obj);
            Iterator iterator = set.iterator();
 
            if (iterator.hasNext()) {
                Object value = iterator.next();
                builder.append(value.getClass().toString().substring(6));
            }
        }
        builder.append(classSuffix);
        return builder.toString();
    }
 
    @Override
    public byte[] serialize(Object o) throws SerializationException {
        if (o == null)
            return new byte[0];
        try {
            String builder = getBegin(o) +
                    separator +
                    mapper.writeValueAsString(o);
            return builder.getBytes("UTF-8");
        } catch (UnsupportedEncodingException | JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
 
    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) return null;//已被删除的session
        try {
            String temp = new String(bytes, "UTF-8"); 
            String cl[] = getClass(temp); 
            if (cl == null) {
                throw new RuntimeException("错误的序列化结果=" + temp);
            }
            if (cl.length == 1) {
                return mapper.readValue(temp.substring(temp.indexOf(separator) + 1), Class.forName(cl[0]));
            } else if (cl.length == 2) {
                TypeFactory factory = mapper.getTypeFactory();
                JavaType type = factory.constructParametricType(Class.forName(cl[0]), Class.forName(cl[1]));
                return mapper.readValue(temp.substring(temp.indexOf(separator) + 1), type);
            } else if (cl.length == 3) {
                TypeFactory factory = mapper.getTypeFactory();
                JavaType type = factory.constructParametricType(Class.forName(cl[0]), Class.forName(cl[1]), Class.forName(cl[2]));
                return mapper.readValue(temp.substring(temp.indexOf(separator) + 1), type);
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
 
    /**
     * 解析字符串，获取class
     * <p>一个类型,java.lang.String<>={}</p>
     * <p>两个类型，后面为泛型,java.lang.String<java.lang.String>={}</p>
     * <p>三个类型，后面为泛型,java.lang.String<java.lang.String,java.lang.String>={}</p>
     *
     * @param value 包含class的字符串
     * @return 返回所有类的数组
     */
    private String[] getClass(String value) {
        int index = value.indexOf(classPrefix);
        if (index != -1) {
            Matcher matcher = pattern.matcher(value.subSequence(index, value.indexOf(classSuffix) + 1));
            if (matcher.find()) {
                String temp = matcher.group(1);
                if (temp.isEmpty()) {//没有泛型
                    return new String[]{value.substring(0, index)};
                } else if (temp.contains(classSeparator)) {//两个泛型
                    int nextIndex = temp.indexOf(classSeparator);
                    return new String[]{
                            value.substring(0, index),
                            temp.substring(0, nextIndex),
                            temp.substring(nextIndex + 1)
                    };
                } else {//一个泛型
                    return new String[]{
                            value.substring(0, index),
                            temp
                    };
                }
            }
        }
        return null;
     }
}