package com.abietic.ap1.controller;

import com.abietic.ap1.error.BusinessException;
import com.abietic.ap1.error.EmBusinessError;
import com.abietic.ap1.mq.RocketMqProducer;
import com.abietic.ap1.response.CommonReturnType;
import com.abietic.ap1.service.ItemService;
import com.abietic.ap1.service.OrderService;
import com.abietic.ap1.service.model.OrderModel;
import com.abietic.ap1.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
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

    //订单创建
    @PostMapping(value = "/createOrder", consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType createItem(@RequestParam(name = "itemId")Integer itemId,
                               @RequestParam(name = "amount")Integer amount,
                               @RequestParam(name = "promoId",required = false)Integer promoId) throws BusinessException {

        //判断是否登录
        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if(isLogin == null || !isLogin){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "请登录后下单");
        }

        //获取用户的登录信息
        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");

        // 非事务性的创建订单和扣减库存操作(只更新了缓存,未更新数据库)
        // OrderModel orderModel = orderService.createOrder(userModel.getId(), itemId, promoId, amount);


        // 加入库存流水init状态
        String stockLogId = itemService.initStockLog(itemId, amount);

        // 使用事务性消息完成事务性的创建订单和异步扣减库存
        boolean mqResult = mqProducer.transactionAsyncDecreaseStock(itemId, amount, promoId, userModel.getId(), stockLogId);
        if (!mqResult) {
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR, "下单失败");
        }
        return CommonReturnType.create(null);
    }
}
