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
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.abietic.ap1.error.BusinessException;
import com.abietic.ap1.mapper.StockLogMapper;
import com.abietic.ap1.model.StockLog;
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

    @Autowired
    private StockLogMapper stockLogMapper;

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
                // 根据是否扣减库存成功,来判断要返回COMMIT,ROLLBACK还是继续UNKNOW
                // 进入这个方法的情况
                // 是executeLocalTransaction超时
                // 那么就需要一种方法,能够记录库存是否操作成功的状态
                // 所以要记录操作流水记录(即log data中间状态记录,可以使用数据库记录这个状态,保证其持久性,分布式服务架构中提过)
                String jsonString =new String(msg.getBody());
                Map<String, Object> bodyMap = JSON.parseObject(jsonString, Map.class);
                Integer itemId = (Integer) bodyMap.get("itemId");
                Integer amount = (Integer) bodyMap.get("amount");
                String stockLogId = (String) bodyMap.get("stockLogId");
                StockLog stockLog = stockLogMapper.selectByPrimaryKey(stockLogId);
                if (stockLog == null) {
                    // 这个情况不应该发生, 但是这里还是处理了一下
                    return LocalTransactionState.UNKNOW;
                }
                switch (stockLog.getStatus().intValue()) {
                    case 1: return LocalTransactionState.UNKNOW;
                    case 2: return LocalTransactionState.COMMIT_MESSAGE;
                    default: return LocalTransactionState.ROLLBACK_MESSAGE;
                }
                // 随着返回unknown次数变多,进入这个方法的频率也会逐渐减少
            }

            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                // 返回值是LocalTransactionState,这是一个枚举类,有3种取值{COMMIT_MESSAGE,ROLLBACK_MESSAGE,UNKNOW,}
                // COMMIT_MESSAGE将之前的prepare消息转化成commit消息给对应的消费方消费
                // ROLLBACK_MESSAGE将之前的prepare消息撤回
                // UNKNOW当前还不知道怎么处理,消息队列先替我保存一下,过一会再问我这个消息是什么状态

                // 取出相应事务对应要执行操作所需的参数
                Map<String, Object> argsMap = (Map<String, Object>) arg;
                Integer itemId = (Integer) argsMap.get("itemId");
                Integer userId = (Integer) argsMap.get("userId");
                Integer promoId = (Integer) argsMap.get("promoId");
                Integer amount = (Integer) argsMap.get("amount");
                String stockLogId = (String) argsMap.get("stockLogId");
                // 这正要进行的事 创建订单,即之前在orderService中的createOrder
                try {
                    // 和itemstock这种在item量级进行行锁的操作比起来
                    // 操作流水表上的数据库操作并没有并发冲突,对于数据库的压力要小很多
                    // 所以可以有这样的改造
                    orderService.createOrder(userId, itemId, promoId, amount, stockLogId);
                } catch (BusinessException e) {
                    e.printStackTrace();
                    // 设置对应的stocklog为回滚状态
                    StockLog stockLog = stockLogMapper.selectByPrimaryKey(stockLogId);
                    stockLog.setStatus(3);
                    stockLogMapper.updateByPrimaryKeySelective(stockLog);
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
    // 数据状态未知消息必须是pending(即处理中,等待最后的commit或是rollback操作), 即方法checkLocalTransaction
    public boolean transactionAsyncDecreaseStock(Integer itemId, Integer amount, Integer promoId, Integer userId, String stockLogId) {
        final Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);
        bodyMap.put("stockLogId", stockLogId);

        final Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("itemId", itemId);
        argsMap.put("amount", amount);
        argsMap.put("userId", userId);
        argsMap.put("stockLogId", stockLogId);
        argsMap.put("promoId", promoId);
        final Message message = new Message(topicName, "increase",
                JSON.toJSONString(bodyMap).toString().getBytes(Charset.forName("UTF-8")));
        TransactionSendResult transactionSendResult;
        try {
            transactionSendResult = transactionMQProducer.sendMessageInTransaction(message, argsMap);
            // 使用二阶段提交,与设置的TransactionListener一起保证事务性
            // 向message broker发送prepare消息并由其中间件管理
            // 接下来会在本地执行方法executeLocalTransaction
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        }
        switch (transactionSendResult.getLocalTransactionState()) {
            case COMMIT_MESSAGE: return true;
            case ROLLBACK_MESSAGE : return false;
            case UNKNOW: return false;
            default : return false;
        }
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
