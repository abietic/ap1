package com.abietic.ap1.controller;

// import com.alibaba.druid.util.StringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abietic.ap1.configuration.SmsServiceProperties;
import com.abietic.ap1.controller.view.UserVO;
import com.abietic.ap1.error.BusinessException;
import com.abietic.ap1.error.EmBusinessError;
import com.abietic.ap1.response.CommonReturnType;
import com.abietic.ap1.service.UserService;
import com.abietic.ap1.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
// import sun.misc.BASE64Encoder;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

// 腾讯云短信API
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class UserController extends BaseController {

    private final static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private SmsServiceProperties smsServiceProperties;

    // 用户登录接口
    @PostMapping(value = "/login", consumes = { CONTENT_TYPE_FORMED })
    public CommonReturnType login(@RequestParam(name = "telphone") String telphone,
            @RequestParam(name = "password") String password)
            throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        // 入参校验
        if (org.apache.commons.lang3.StringUtils.isEmpty(telphone) ||
                org.apache.commons.lang3.StringUtils.isEmpty(password)) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "请登录后下单");
        }

        // 用户登录服务
        UserModel userModel = userService.vaildateLogin(telphone, EncodeByMd5(password));

        // 将登录凭证加入到用户登录成功的session内
        httpServletRequest.getSession().setAttribute("IS_LOGIN", true);
        httpServletRequest.getSession().setAttribute("LOGIN_USER", userModel);

        return CommonReturnType.create(null);
    }

    // 统一登录接口
    @GetMapping(value = "/centralLogin")
    public void centralLogin(HttpServletResponse response)
            throws BusinessException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登录,无法参加商品秒杀");
        }
        UserModel userModel = null;
        if (authentication.getPrincipal() instanceof UserModel) {
            userModel = (UserModel) authentication.getPrincipal();
        }
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登录,无法参加商品秒杀");
        } else {
            // 将登录凭证加入到用户登录成功的session内
            httpServletRequest.getSession().setAttribute("IS_LOGIN", true);
            httpServletRequest.getSession().setAttribute("LOGIN_USER", userModel);
            response.sendRedirect("/listitem.html");
        }
    }

    // 用户注册接口
    @PostMapping(value = "/register", consumes = { CONTENT_TYPE_FORMED })
    public CommonReturnType register(@RequestParam(name = "telphone") String telphone,
            @RequestParam(name = "otpCode") String otpCode,
            @RequestParam(name = "name") String name,
            @RequestParam(name = "gender") Integer gender,
            @RequestParam(name = "age") Integer age,
            @RequestParam(name = "password") String password)
            throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        // 验证手机号和对应的otpcode是否相符合
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        if (!StringUtils.equals(otpCode, inSessionOtpCode)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "短信验证码不符合");
        }

        // 用户注册
        UserModel userModel = new UserModel();
        userModel.setName(name);
        // userModel.setGender(new Byte(String.valueOf(gender)));
        userModel.setGender(String.valueOf(gender).getBytes()[0]);
        userModel.setAge(age);
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(EncodeByMd5(password));

        userService.register(userModel);

        return CommonReturnType.create(null);
    }

    public String EncodeByMd5(String str) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        // 确定计算方法
        // MessageDigest md5 = MessageDigest.getInstance("MD5");
        // BASE64Encoder base64Encoder = new BASE64Encoder();
        // 加密字符串
        // String newstr =
        // Base64.getEncoder().encodeToString(md5.digest(str.getBytes("utf-8")));

        // 为了简化直接使用MD5
        String newstr = DigestUtils.md5DigestAsHex(str.getBytes("utf-8"));
        return newstr;
    }

    // 获取验证码
    @PostMapping(value = "/getotp", consumes = { CONTENT_TYPE_FORMED })
    public CommonReturnType getOtp(@RequestParam(name = "telphone") String telphone) {
        // 需要按照一定规则生成OTP验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);

        // 将otp验证码同对应的用户手机号关联,使用httpsession的方式绑定手机号与OTPCODE
        httpServletRequest.getSession().setAttribute(telphone, otpCode);

        // 将otp验证码通过短信通道发送给用户
        System.out.println("telphone = " + telphone + "& otpCode = " + otpCode);
        return CommonReturnType.create(null);
    }

    // 获取手机验证码
    @PostMapping(value = "/getauthcode", consumes = { CONTENT_TYPE_FORMED })
    public CommonReturnType getAuthCode(@RequestParam(name = "telphone") String telphone) {

        final int authCodeAvailTime = 20;

        try {
            Credential cred = new Credential(this.smsServiceProperties.getSmsSecretId(),
                    this.smsServiceProperties.getSmsSecretKey());
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            SmsClient client = new SmsClient(cred, this.smsServiceProperties.getSmsRegion(), clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            SendSmsRequest req = new SendSmsRequest();
            // 国内短信发送需要在手机尾号前加
            String[] phoneNumberSet1 = { "86" + telphone };
            req.setPhoneNumberSet(phoneNumberSet1);

            // 需要按照一定规则生成OTP验证码
            Random random = new Random();
            int randomInt = random.nextInt(99999);
            randomInt += 10000;
            String authCode = String.valueOf(randomInt);

            // 将otp验证码同对应的用户手机号关联,使用httpsession的方式绑定手机号与OTPCODE
            // 以后可能改为直接存在redis中,这样可能更好控制超时时间
            httpServletRequest.getSession().setAttribute(telphone, authCode);

            req.setSmsSdkAppId(this.smsServiceProperties.getSmsSdkAppId());
            req.setSignName(this.smsServiceProperties.getSmsSignName());
            req.setTemplateId(this.smsServiceProperties.getSmsTemplateId());

            String[] templateParamSet1 = { authCode, Integer.toString(authCodeAvailTime) };
            req.setTemplateParamSet(templateParamSet1);

            // 返回的resp是一个SendSmsResponse的实例，与请求对象对应
            SendSmsResponse resp = client.SendSms(req);
            // 输出json格式的字符串回包
            logger.info(SendSmsResponse.toJsonString(resp));
        } catch (TencentCloudSDKException e) {
            // System.out.println(e.toString());
            logger.error("Telephone authcode send failed.", e);
        }
        return CommonReturnType.create(null);
    }

    @GetMapping("/get")
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BusinessException {
        UserModel userModel = userService.getUserById(id);
        UserVO userVO = convertFromModel(userModel);

        // 若获取的对应用户信息不存在
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
            // userModel.setEncrptPassword("123");
        }

        // 返回通用对象
        return CommonReturnType.create(userVO);
    }

    /**
     * 将核心领域模型转为视图对象
     * 
     * @param userModel 领域模型
     * @return 视图对象
     */
    private UserVO convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);
        return userVO;
    }

}
