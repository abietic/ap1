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
import com.google.common.util.concurrent.RateLimiter;
import com.ramostear.captcha.HappyCaptcha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/order")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class OrderController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    // // 现在改为在异步更新数据库的消息队列中进行执行
    // @Autowired
    // private OrderService orderService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RocketMqProducer mqProducer;

    @Autowired
    @Qualifier("cacheRedisRedisTemplate")
    private RedisTemplate<Object, Object> jsonEnhancedRedisTemplate;

    private RateLimiter orderCreateRateLimiter;

    private static final int PERMITS_PER_SEC_FOR_RATE_LIMITER = 300;

    @Autowired
    private PromoService promoService;

    private ExecutorService executorService;

    private static final int CONCURRENT_COUNT = 20;

    @PostConstruct
    public void init() {

        // 通过限制同时执行的操作线程进行限流,应该用信号量也可以实现类似的效果
        executorService = Executors.newFixedThreadPool(CONCURRENT_COUNT);

        // 使用Guava提供的
        // 大概原理是检查当前是否还有多余的可用,如果没有,sleep线程到下一秒尝试
        orderCreateRateLimiter = RateLimiter.create(PERMITS_PER_SEC_FOR_RATE_LIMITER);
    }

    @GetMapping(value = "/generateCaptcha")
    public void happyCaptcha(HttpServletResponse response) throws BusinessException {
        // 判断是否登录
        Boolean isLogin = (Boolean)
        httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if (isLogin == null || !isLogin) {
        throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登录, 无法参加商品秒杀");
        }

        // 获取用户的登录信息
        UserModel userModel = (UserModel)
        httpServletRequest.getSession().getAttribute("LOGIN_USER");
        if (userModel == null) {
        throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登录, 无法参加商品秒杀");
        }

        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // if (authentication == null) {
        //     throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登录,无法参加商品秒杀");
        // }
        // UserModel userModel = null;
        // if (authentication.getPrincipal() instanceof UserModel) {
        //     userModel = (UserModel) authentication.getPrincipal();
        // }
        // if (userModel == null) {
        //     throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登录,无法参加商品秒杀");
        // }
        HappyCaptcha.require(httpServletRequest, response).build().finish();

    }

    // 生成秒杀订单授权令牌
    @PostMapping(value = "/generateToken", consumes = { CONTENT_TYPE_FORMED })
    public CommonReturnType generateToken(@RequestParam(name = "itemId") Integer itemId,
            @RequestParam(name = "promoId") Integer promoId,
            @RequestParam(name = "verifyCode") String verifyCode) throws BusinessException {
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

        // 验证验证码
        boolean verifyPass = HappyCaptcha.verification(httpServletRequest, verifyCode, true);
        if (!verifyPass) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "请求非法, 验证码错误");
        }
        HappyCaptcha.remove(httpServletRequest);

        // 获取秒杀令牌
        String promoToken = promoService.generateSecondKillToken(promoId, itemId, userModel.getId());
        if (promoToken == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "生成秒杀授权令牌失败");
        }
        return CommonReturnType.create(promoToken);
    }

    // 订单创建
    @PostMapping(value = "/createOrder", consumes = { CONTENT_TYPE_FORMED })
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
            @RequestParam(name = "amount") Integer amount,
            @RequestParam(name = "promoId", required = false) Integer promoId,
            @RequestParam(name = "promoToken", required = false) String promoToken) throws BusinessException {

        if (!orderCreateRateLimiter.tryAcquire()) {
            throw new BusinessException(EmBusinessError.RATE_LIMIT);
        }
        // 判断是否登录
        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if (isLogin == null || !isLogin) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "请登录后下单");
        }

        // 获取用户的登录信息
        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");

        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "请登录后下单");
        }

        // 校验秒杀令牌是否正确
        if (promoId != null) {
            String promoAuthTokenKeyString = "promo_token_" + promoId + "_user_id_" + userModel.getId() + "_item_id_"
                    + itemId;
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
        // 这部分现在被放入订单授权令牌的大闸中进行了,在PromoService的generateSecondKillToken中
        // String promoInvalidKeyString = "promo_item_stock_invalid_" + itemId;
        // if (jsonEnhancedRedisTemplate.hasKey(promoInvalidKeyString)) {
        // throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        // }

        // 同步调用线程池的submit方法
        // 拥塞窗口为20的等待队列,用来队列化泄洪
        // 但是,这种调用方式真的有上述描述的效果吗?
        // 这里是指各个连接对应的请求线程会并发调用这个类的实例方法,
        // 这时他们执行这个方法时,会调用这个实例的成员executor,而这个executor有执行容量的限制
        // 当其设置的所有线程都被占用,且都还没执行完成时,新的执行请求会被阻塞
        // 感觉其实可以用信号量semaphore来代替
        Future<Object> futureResult = executorService.submit(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                // 加入库存流水init状态
                String stockLogId = itemService.initStockLog(itemId, amount);

                // 使用事务性消息完成事务性的创建订单和异步扣减库存
                boolean mqResult = mqProducer.transactionAsyncDecreaseStock(itemId, amount, promoId, userModel.getId(),
                        stockLogId);
                if (!mqResult) {
                    throw new BusinessException(EmBusinessError.UNKNOWN_ERROR, "下单失败");
                } else {
                    // 如果下单成功,回收用户所拥有的token
                    String promoAuthTokenKeyString = "promo_token_" + promoId + "_user_id_" + userModel.getId() + "_item_id_"
                    + itemId;
                    jsonEnhancedRedisTemplate.opsForValue().set(promoAuthTokenKeyString, "");
                }
                return null;
            }

        });

        try {
            futureResult.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        }

        // // 加入库存流水init状态
        // String stockLogId = itemService.initStockLog(itemId, amount);

        // // 使用事务性消息完成事务性的创建订单和异步扣减库存
        // boolean mqResult = mqProducer.transactionAsyncDecreaseStock(itemId, amount,
        // promoId, userModel.getId(),
        // stockLogId);
        // if (!mqResult) {
        // throw new BusinessException(EmBusinessError.UNKNOWN_ERROR, "下单失败");
        // }
        return CommonReturnType.create(null);
    }
}
