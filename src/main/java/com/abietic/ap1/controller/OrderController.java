package com.abietic.ap1.controller;

import com.abietic.ap1.error.BusinessException;
import com.abietic.ap1.error.EmBusinessError;
import com.abietic.ap1.mq.RocketMqProducer;
import com.abietic.ap1.response.CommonReturnType;
import com.abietic.ap1.service.ItemService;
import com.abietic.ap1.service.OrderService;
import com.abietic.ap1.service.PromoService;
import com.abietic.ap1.service.model.OrderModel;
import com.abietic.ap1.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author manster
 * @Date 2021/5/26
 **/
@RestController
@RequestMapping("/order")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class OrderController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RocketMqProducer mqProducer;

    @Autowired
    @Qualifier("cacheRedisRedisTemplate")
    private RedisTemplate<Object, Object> jsonEnhancedRedisTemplate;

    @Autowired
    private PromoService promoService;

    // 生成秒杀订单授权令牌
    @PostMapping(value = "/generateToken", consumes = { CONTENT_TYPE_FORMED })
    public CommonReturnType generateToken(@RequestParam(name = "itemId") Integer itemId,
            @RequestParam(name = "promoId") Integer promoId) throws BusinessException {
        // 判断是否登录
        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if (isLogin == null || !isLogin) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登录, 无法参加商品秒杀");
        }

        // 获取用户的登录信息
        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登录, 无法参加商品秒杀");
        }
        // 获取秒杀令牌
        String promoToken = promoService.generateSecondKillToken(promoId, itemId, userModel.getId());
        if (promoToken == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "生成秒杀授权令牌失败");
        }
        return CommonReturnType.create(promoToken);
    }

    // 订单创建
    @PostMapping(value = "/createOrder", consumes = { CONTENT_TYPE_FORMED })
    public CommonReturnType createItem(@RequestParam(name = "itemId") Integer itemId,
            @RequestParam(name = "amount") Integer amount,
            @RequestParam(name = "promoId", required = false) Integer promoId,
            @RequestParam(name = "promoToken", required = false) String promoToken) throws BusinessException {

        // 判断是否登录
        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if (isLogin == null || !isLogin) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "请登录后下单");
        }

        // 获取用户的登录信息
        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");

        // 校验秒杀令牌是否正确
        if (promoId != null) {
            String promoAuthTokenKeyString = "promo_token_" + promoId + "_user_id_" + userModel.getId() + "_item_id_" + itemId;
            String inRedisPromoTOken = (String) jsonEnhancedRedisTemplate.opsForValue().get(promoAuthTokenKeyString);
            if (inRedisPromoTOken == null
                    || !org.apache.commons.lang3.StringUtils.equals(promoToken, inRedisPromoTOken)) {
                // 如果用户给出的令牌与存储的令牌不一致
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "秒杀令牌校验失败");
            }
        }

        // 非事务性的创建订单和扣减库存操作(只更新了缓存,未更新数据库)
        // OrderModel orderModel = orderService.createOrder(userModel.getId(), itemId,
        // promoId, amount);

        // 这里是在商品售罄后,ItemService的decreaseStock中会为售罄的商品在redis中打上售罄标记
        // 判断是否库存已售罄,若对应的商品售罄key存在,则直接返回下单失败
        // 减少了不必要的库存流水状态记录的流量和存储的浪费
        String promoInvalidKeyString = "promo_item_stock_invalid_" + itemId;
        if (jsonEnhancedRedisTemplate.hasKey(promoInvalidKeyString)) {
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        // 加入库存流水init状态
        String stockLogId = itemService.initStockLog(itemId, amount);

        // 使用事务性消息完成事务性的创建订单和异步扣减库存
        boolean mqResult = mqProducer.transactionAsyncDecreaseStock(itemId, amount, promoId, userModel.getId(),
                stockLogId);
        if (!mqResult) {
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR, "下单失败");
        }
        return CommonReturnType.create(null);
    }
}
