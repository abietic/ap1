package com.abietic.ap1.service.impl;

import com.abietic.ap1.mapper.PromoMapper;
import com.abietic.ap1.model.Promo;
import com.abietic.ap1.service.ItemService;
import com.abietic.ap1.service.PromoService;
import com.abietic.ap1.service.model.ItemModel;
import com.abietic.ap1.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @Author manster
 * @Date 2021/5/27
 **/
@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoMapper promoMapper;

    @Autowired
    @Qualifier("cacheRedisRedisTemplate")
    private RedisTemplate<Object, Object> jsonEnhancedRedisTemplate;

    @Autowired
    private ItemService itemService;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        Promo promoDO = promoMapper.selectByItemId(itemId);
        //entity->model
        PromoModel promoModel = convertFromEntity(promoDO);
        if(promoModel == null){
            System.err.println("Promotion not found for item_id " + itemId + ".");
            return null;
        }

        //判断当前时间活动是否即将开始或正在进行
        if(promoModel.getStartDate().isAfterNow()){
            //活动未开始
            promoModel.setStatus(1);
        }else if (promoModel.getEndDate().isBeforeNow()){
            //活动已结束
            promoModel.setStatus(3);
        }else {
            //正在进行中
            promoModel.setStatus(2);
        }

        return promoModel;
    }

    private PromoModel convertFromEntity(Promo promo){
        if(promo == null){
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promo, promoModel);
        promoModel.setPromoItemPrice(BigDecimal.valueOf(promo.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promo.getStartDate()));
        promoModel.setEndDate(new DateTime(promo.getEndDate()));
        return promoModel;
    }

    @Override
    public void publishPromo(Integer promoId) {
       // 通过活动id获取活动
        Promo promo = promoMapper.selectByItemId(promoId);
        if (promo == null || promo.getId() == null || promo.getId().intValue() == 0) {
            return;
        }
        ItemModel itemModel = itemService.getItemById(promo.getItemId());

        if (itemModel == null || itemModel.getId() == null || itemModel.getStock() == null) {
            return;
        }

        // 将库存同步到redis内
        String promoItemStockKeyString = "promo_item_stock_" + itemModel.getId();
        jsonEnhancedRedisTemplate.opsForValue().set(promoItemStockKeyString, itemModel.getStock());
    }

}
