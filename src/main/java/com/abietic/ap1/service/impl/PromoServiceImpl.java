package com.abietic.ap1.service.impl;

import com.abietic.ap1.mapper.PromoMapper;
import com.abietic.ap1.model.Promo;
import com.abietic.ap1.service.ItemService;
import com.abietic.ap1.service.PromoService;
import com.abietic.ap1.service.UserService;
import com.abietic.ap1.service.model.ItemModel;
import com.abietic.ap1.service.model.PromoModel;
import com.abietic.ap1.service.model.UserModel;

import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

/**
 * @Author manster
 * @Date 2021/5/27
 **/
@Service
public class PromoServiceImpl implements PromoService {

    private static final int THRESHHOLD_FACTOR = 5;

    @Autowired
    private PromoMapper promoMapper;

    @Autowired
    @Qualifier("cacheRedisRedisTemplate")
    private RedisTemplate<Object, Object> jsonEnhancedRedisTemplate;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

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
    public void publishPromo(Integer itemId) {
       // 通过活动id获取活动
        Promo promo = promoMapper.selectByItemId(itemId);
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

        // 在发布商品促销时
        // 将令牌流量大闸的限制数字设置到redis内
        String promoThreshholdKeyString = "promo_threshhold_"+promo.getId();
        // jsonEnhancedRedisTemplate.opsForValue().setIfAbsent(promoThreshholdKeyString, itemModel.getStock().intValue() * THRESHHOLD_FACTOR);
        jsonEnhancedRedisTemplate.opsForValue().set(promoThreshholdKeyString, itemModel.getStock().intValue() * THRESHHOLD_FACTOR); // 这里
    }

    @Override
    public String generateSecondKillToken(Integer promoId, Integer itemId, Integer userId) {
        // 如果相应商品已经售罄,再发布秒杀授权令牌就也不会成功生成订单了
        String promoInvalidKeyString = "promo_item_stock_invalid_" + itemId;
        if (jsonEnhancedRedisTemplate.hasKey(promoInvalidKeyString)) {
            return null;
        }

        Promo promoDO = promoMapper.selectByPrimaryKey(promoId);
        //entity->model
        PromoModel promoModel = convertFromEntity(promoDO);
        if(promoModel == null){
            System.err.println("Promotion not found for promo_id " + promoId + ".");
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

        if (promoModel.getStatus().intValue() != 2) {
            // 商品不在秒杀活动时间范围内
            return null;
        }


        // 判断商品是否存在
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if (itemModel == null) {
            return null;
        }

        // 判断用户是否存在
        UserModel userModel = userService.getUserByIdInCache(userId);
        if (userModel == null) {
            return null;
        }


        // 获取秒杀大闸的count的数量,如果授权出去的令牌已经超出阈值,就直接放弃令牌生成
        String promoThreshholdKeyString = "promo_threshhold_"+promoId;
        Long result = jsonEnhancedRedisTemplate.opsForValue().increment(promoThreshholdKeyString, -1);
        if (result < 0) {
            return null;
        }

        // 生成的全局唯一的token(令牌),用来授权相应商品的促销给相应用户提供订单
        String token = UUID.randomUUID().toString().replace("-", "");

        String promoAuthTokenKeyString = "promo_token_" + promoId + "_user_id_" + userId + "_item_id_"+itemId;

        // 将授权令牌存入redis并给5分钟的有效期
        jsonEnhancedRedisTemplate.opsForValue().set(promoAuthTokenKeyString, token, Duration.ofMinutes(5));
        return token;
    }

}
