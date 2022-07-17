package com.abietic.ap1.service;

import com.abietic.ap1.error.BusinessException;
import com.abietic.ap1.service.model.UserModel;


public interface UserService {

    //通过用户id获取用户对象
    UserModel getUserById(Integer id);

    //注册
    void register(UserModel userModel) throws BusinessException;

    /**
     * 用户登录校验
     * @param telphone 手机号
     * @param encrptPassword 用户加密后的密码
     * @throws BusinessException
     */
    UserModel vaildateLogin(String telphone, String encrptPassword) throws BusinessException;
}
