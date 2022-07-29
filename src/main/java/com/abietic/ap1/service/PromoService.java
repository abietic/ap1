package com.abietic.ap1.service;

import com.abietic.ap1.service.model.PromoModel;


public interface PromoService {

    //根据商品id获取即将进行以及正在进行的活动信息
    PromoModel getPromoByItemId(Integer itemId);

    void publishPromo(Integer promoId);

}
