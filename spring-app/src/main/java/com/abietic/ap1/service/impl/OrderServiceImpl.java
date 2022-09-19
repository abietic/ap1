package com.abietic.ap1.service.impl;

import com.abietic.ap1.mapper.OrderMapper;
import com.abietic.ap1.mapper.SequenceMapper;
import com.abietic.ap1.mapper.StockLogMapper;
import com.abietic.ap1.model.Order;
import com.abietic.ap1.model.Sequence;
import com.abietic.ap1.model.StockLog;
import com.abietic.ap1.error.BusinessException;
import com.abietic.ap1.error.EmBusinessError;
import com.abietic.ap1.service.ItemService;
import com.abietic.ap1.service.OrderService;
import com.abietic.ap1.service.UserService;
import com.abietic.ap1.service.model.ItemModel;
import com.abietic.ap1.service.model.OrderModel;
import com.abietic.ap1.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author manster
 * @Date 2021/5/26
 **/
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private SequenceMapper sequenceMapper;

    @Autowired
    private StockLogMapper stockLogMapper;

    @Autowired
    @Qualifier("cacheRedisRedisTemplate")
    private RedisTemplate<Object, Object> jsonEnhancedRedisTemplate;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId)
            throws BusinessException {
        // 1.校验下单状态，商品是否存在，用户是否合法，购买数量是否正确
        // ItemModel itemModel = itemService.getItemById(itemId); // 第一次mysql查询
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if (itemModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商品信息不存在");
        }

        // UserModel userModel = userService.getUserById(userId); // 第二次mysql查询
        UserModel userModel = userService.getUserByIdInCache(userId);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "用户信息不存在");
        }

        if (amount <= 0 || amount > 99) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "数量信息不正确");
        }

        // 校验活动信息
        if (promoId != null) {
            // 校验对应活动是否存在这个适用商品
            if (!promoId.equals(itemModel.getPromoModel().getId())) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动信息不正确");
            } else if (itemModel.getPromoModel().getStatus() != 2) {
                // 活动是否正在进行中
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动还未开始");
            }
        }

        // 2.落单减库存，支付减库存
        boolean result = itemService.decreaseStock(itemId, amount); // 一次数据更新，涉及行锁,读多写也多 // 现在的代码中这里不涉及数据库了
        if (!result) {
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        // 3.订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setItemId(itemId);
        orderModel.setUserId(userId);
        orderModel.setAmount(amount);
        if (promoId != null) {
            // 商品价格取特价
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        } else {
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setPromoId(promoId);
        orderModel.setOrderAmount(orderModel.getItemPrice().multiply(new BigDecimal(amount)));

        // 生成交易流水号 一次sql涉及行锁 // 现在的代码改为异步更新
        orderModel.setId(generateOrderNo());

        Order order = convertFromOrderModel(orderModel);
        orderMapper.insertSelective(order); // 一次sql添加操作

        // 加上商品的销量
        itemService.increaseSales(itemId, amount); // 一次数据修改，行锁,写多

        // 修改操作状态
        StockLog stockLog = stockLogMapper.selectByPrimaryKey(stockLogId);
        if (stockLog == null) {
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        }
        // 这里看起来没有进行锁,应该是因为本身id就是在线程内进行的,其他线程本来也得不到
        stockLog.setStatus(2);
        stockLogMapper.updateByPrimaryKeySelective(stockLog);

        // TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

        //     @Override
        //     public void afterCommit() {
        //         // 异步更新库存
        //         boolean mqResult = itemService.asyncDecreaseStock(itemId, amount); // 发送和消费失败会导致数据库与缓存不同步
        //         // if (!mqResult) {
        //         //     itemService.increaseStock(itemId, amount);
        //         //     throw new BusinessException(EmBusinessError.MQ_SEND_FAIL);
        //         // }
        //     }

        // });

        // 4.返回前端
        return orderModel;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateOrderNo() {
        // 订单号16位
        StringBuilder stringBuilder = new StringBuilder();
        // 前8位为 年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        stringBuilder.append(nowDate);

        // 中间6位为自增序列
        // 获取当前sequence
        // int sequence = 0;
        // Sequence sequenceDO = sequenceMapper.getSequenceByName("order_info");
        // sequence = sequenceDO.getCurrentValue();
        // sequenceDO.setCurrentValue(sequenceDO.getCurrentValue() + sequenceDO.getStep());
        // sequenceMapper.updateByPrimaryKey(sequenceDO);
        int sequence = this.getSequenceByNameInCache("order_info");

        // 凑足6位
        String sequenceStr = String.valueOf(sequence);
        for (int i = 0; i < 6 - sequenceStr.length(); i++) {
            stringBuilder.append(0);
        }
        stringBuilder.append(sequence);

        // 后两位为分库分表位
        stringBuilder.append("00");

        return stringBuilder.toString();
    }

    private Order convertFromOrderModel(OrderModel orderModel) {
        if (orderModel == null) {
            return null;
        }
        Order orderDO = new Order();
        BeanUtils.copyProperties(orderModel, orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderAmount().doubleValue());
        return orderDO;
    }

    private Integer getSequenceByNameInCache(String name) {
        String sequenceValueKeyString = "seq_val_" + name;
        String sequenceStepKeyString = "seq_step_" + name;
        Integer res = null, step = null;
        // TODO:注意这里两个变量分别操作,不是原子性的,可能会出现初始化问题,要想解决需要使用脚本
        if ((step = (Integer)jsonEnhancedRedisTemplate.opsForValue().get(sequenceStepKeyString)) != null) {
            res = jsonEnhancedRedisTemplate.opsForValue().increment(sequenceValueKeyString, step.longValue()).intValue();
            return res;
        }
        // 但是下面的操作中的select有加for update修饰,会带来锁某种意义上能弥合上面非原子性的问题
        int sequence = 0;
        Sequence sequenceDO = sequenceMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue()); // 会进行异步更新这里不需要更新数值
        jsonEnhancedRedisTemplate.opsForValue().set(sequenceStepKeyString, sequenceDO.getStep());
        jsonEnhancedRedisTemplate.opsForValue().set(sequenceValueKeyString, Integer.valueOf(sequence + sequenceDO.getStep()));
        // sequenceMapper.updateByPrimaryKey(sequenceDO);

        
        return sequence;
    }

}
