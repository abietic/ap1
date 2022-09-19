package com.abietic.ap1.service;

import com.abietic.ap1.service.model.PromoModel;


public interface PromoService {

    //根据商品id获取即将进行以及正在进行的活动信息
    PromoModel getPromoByItemId(Integer itemId);

    int publishPromo(Integer promoId);

    // 生成秒杀用的令牌
    // 只有携带了合法令牌的下单申请才会被处理
    String generateSecondKillToken (Integer promoId, Integer itemId, Integer userId);
}
