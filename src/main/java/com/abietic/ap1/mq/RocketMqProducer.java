package com.abietic.ap1.mq;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.abietic.ap1.error.BusinessException;
import com.abietic.ap1.service.OrderService;
import com.alibaba.fastjson.JSON;

@Component
public class RocketMqProducer {
    private DefaultMQProducer producer;

    private TransactionMQProducer transactionMQProducer;

    @Value("${rocketmq.name-server}")
    private String namesrvAddr;

    @Value("${rocketmq.topic}")
    private String topicName;

    @Autowired
    private OrderService orderService;

    @PostConstruct
    public void init() throws MQClientException {
        // 做mq的初始化
        producer = new DefaultMQProducer("producer_group");
        producer.setNamesrvAddr(namesrvAddr);
        producer.start();

        transactionMQProducer = new TransactionMQProducer("_trancproducer_group");
        transactionMQProducer.setNamesrvAddr(namesrvAddr);
        transactionMQProducer.setTransactionListener(new TransactionListener() {

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                // 返回值是LocalTransactionState,这是一个枚举类,有3种取值{COMMIT_MESSAGE,ROLLBACK_MESSAGE,UNKNOW,}
                // COMMIT_MESSAGE将之前的prepare消息转化成commit消息给对应的消费方消费
                // ROLLBACK_MESSAGE将之前的prepare消息撤回
                // UNKNOW当前还不知道怎么处理,消息队列先替我保存一下,过一会再问我这个消息是什么状态

                // 取出相应事务对应要执行操作所需的参数
                Map<String, Integer> argsMap = (Map<String, Integer>) arg;
                Integer itemId = argsMap.get("itemId");
                Integer userId = argsMap.get("userId");
                Integer promoId = argsMap.get("promoId");
                Integer amount = argsMap.get("amount");
                // 这正要进行的事 创建订单,即之前在orderService中的createOrder
                try {
                    orderService.createOrder(userId, itemId, promoId, amount);
                } catch (BusinessException e) {
                    e.printStackTrace();
                    // 发生异常代表订单创建失败,不能让消费者消费消息
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }

                return LocalTransactionState.COMMIT_MESSAGE;
            }

        });
        transactionMQProducer.start();
    }

    // 事务型同步库存扣减消息
    // 保证数据库事务提交了消息必定发送成功
    // 数据库事务回滚了消息必定不发送
    // 数据状态未知消息必须是pending(即处理中,等待最后的commit或是rollback操作)
    public boolean transactionAsyncDecreaseStock(Integer itemId, Integer amount, Integer promoId, Integer userId) {
        final Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);

        final Map<String, Integer> argsMap = new HashMap<>();
        argsMap.put("itemId", itemId);
        argsMap.put("amount", amount);
        argsMap.put("userId", userId);
        argsMap.put("promoId", promoId);
        final Message message = new Message(topicName, "increase",
                JSON.toJSONString(bodyMap).toString().getBytes(Charset.forName("UTF-8")));
        try {
            transactionMQProducer.sendMessageInTransaction(message, argsMap);
            // 使用二阶段提交,与设置的TransactionListener一起保证事务性
            // 向message broker发送prepare消息并由其中间件管理
            // 接下来会在本地执行方法executeLocalTransaction
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // 同步扣库存消息
    public SendResult asyncDecreaseStock(final Integer itemId, final Integer amount)
            throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        final Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);
        final Message message = new Message(topicName, "increase",
                JSON.toJSONString(bodyMap).toString().getBytes(Charset.forName("UTF-8")));
        return producer.send(message);
    }
}
