package com.abietic.ap1.service;

import com.abietic.ap1.error.BusinessException;
import com.abietic.ap1.service.model.OrderModel;


public interface OrderService {

    //1.通过前端url传秒杀活动id,然后下单接口内校验对应id是否属于对应的商品且活动已开始
    //2.直接在下单接口内判断对应商品是否存在秒杀活动，如存在就以秒杀价格下单
    OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId) throws BusinessException;

    // 生成订单号
    // 由于如果用自增做订单号会带来全表
    String generateOrderNo();

    Integer getSequenceByNameInCache(String name);

}
