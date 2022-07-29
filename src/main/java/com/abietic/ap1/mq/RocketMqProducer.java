package com.abietic.ap1.mq;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

@Component
public class RocketMqProducer {
    private DefaultMQProducer producer;

    @Value("${rocketmq.name-server}")
    private String namesrvAddr;

    @Value("${rocketmq.topic}")
    private String topicName;

    @PostConstruct
    public void init() throws MQClientException {
        // 做mq的初始化
        producer = new DefaultMQProducer("producer_group");
        producer.setNamesrvAddr(namesrvAddr);
        producer.start();
    }

    // 同步扣库存消息
    public SendResult asyncDecreaseStock(Integer itemId, Integer amount) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);
        Message message = new Message(topicName, "increase", JSON.toJSONString(bodyMap).toString().getBytes(Charset.forName("UTF-8")));
        return producer.send(message);
    }
}
