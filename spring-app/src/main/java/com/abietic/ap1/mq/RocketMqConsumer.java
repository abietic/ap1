package com.abietic.ap1.mq;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.abietic.ap1.mapper.ItemStockMapper;
import com.abietic.ap1.mapper.SequenceMapper;
import com.alibaba.fastjson.JSON;

@Component
public class RocketMqConsumer {

    private final static Logger logger = LoggerFactory.getLogger(RocketMqConsumer.class);

    private DefaultMQPushConsumer consumer; // 使用push模式获得消息的consumer

    @Value("${rocketmq.name-server}")
    private String namesrvAddr;

    @Value("${rocketmq.topic}")
    private String topicName;

    @Autowired
    private ItemStockMapper itemStockMapper;

    @Autowired
    private SequenceMapper sequenceMapper;

    @PostConstruct
    public void init() throws MQClientException {
        logger.info("MQ consumer init start");
        consumer = new DefaultMQPushConsumer("stock_consumer_group");
        consumer.setNamesrvAddr(namesrvAddr);
        logger.info("Set name server address {}.", namesrvAddr);
        consumer.subscribe(topicName, "*");
        logger.info("Set subscribe topic {}.", topicName);

        logger.info("MQ consumer regist listener start.");
        consumer.registerMessageListener(new MessageListenerConcurrently() {

            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                logger.info("MQ consumer working.");
                try {
                    Message msg = msgs.get(0);
                    String jsonString = new String(msg.getBody());
                    Map<String, Object> bodyMap = JSON.parseObject(jsonString, Map.class);
                    Integer itemId = (Integer) bodyMap.get("itemId");
                    Integer amount = (Integer) bodyMap.get("amount");
                    // 异步更新数据库内容
                    // 扣减数据库库存
                    itemStockMapper.decreaseStock(itemId, amount);
                    sequenceMapper.updateSequenceByName("order_info");
                    logger.info("MQ consumer finish working.");
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } catch (Exception e) {
                    logger.error("Exception occured when consuming message.", e);
                }
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }

        });
        logger.info("MQ consumer regist listener completed.");
        consumer.start();
        logger.info("MQ consumer init completed");
    }
}
