package com.abietic.ap1.service.impl;

import com.abietic.ap1.mapper.UserMapper;
import com.abietic.ap1.mapper.UserPasswordMapper;
import com.abietic.ap1.model.User;
import com.abietic.ap1.model.UserPassword;
import com.abietic.ap1.error.BusinessException;
import com.abietic.ap1.error.EmBusinessError;
import com.abietic.ap1.service.UserService;
import com.abietic.ap1.service.model.UserModel;
import com.abietic.ap1.validator.ValidationResult;
import com.abietic.ap1.validator.ValidatorImpl;

import java.time.Duration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserPasswordMapper userPasswordMapper;

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    @Qualifier("cacheRedisRedisTemplate")
    private RedisTemplate<Object, Object> jsonEnhancedRedisTemplate;

    @Override
    public UserModel getUserById(Integer id) {
        User user = userMapper.selectByPrimaryKey(id);
        if(user == null){
            return null;
        }
        //获得加密密码信息
        UserPassword userPassword = userPasswordMapper.selectByUserId(id);
        return convertFromEntity(user, userPassword);

    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if(userModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
    //    if(StringUtils.isEmpty(userModel.getName())
    //            || userModel.getGender() == null
    //            || userModel.getAge() == null
    //            || StringUtils.isEmpty(userModel.getTelphone())){
    //        throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
    //    }
        ValidationResult result = validator.validate(userModel);
        if(result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }


        //实现model->entity
        User user = convertFromModel(userModel);
        try{
            userMapper.insertSelective(user);
            userModel.setId(user.getId());
        }catch (DuplicateKeyException ex){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"手机号已注册");
        }

        UserPassword userPassword = convertPasswordFromModel(userModel);
        userPasswordMapper.insertSelective(userPassword);

        return;
    }

    @Override
    public UserModel vaildateLogin(String telphone, String encrptPassword) throws BusinessException {
        //通过手机获取用户信息
        User user = userMapper.selectByTelphone(telphone);
        if(user == null){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        UserPassword userPassword = userPasswordMapper.selectByUserId(user.getId());
        UserModel userModel = convertFromEntity(user, userPassword);

        //比对密码
        if(!StringUtils.equals(encrptPassword, userModel.getEncrptPassword())){
            throw new BusinessException((EmBusinessError.USER_LOGIN_FAIL));
        }
        return userModel;
    }

    /**
     * 将 UserModel 传入 UserPasswordDO
     * @param userModel 密码领域模型
     * @return userPasswordDO
     */
    private UserPassword convertPasswordFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }
        UserPassword userPassword = new UserPassword();
        userPassword.setEncrptPassword(userModel.getEncrptPassword());
        userPassword.setUserId(userModel.getId());
        return userPassword;
    }

    /**
     * 将 UserModel 传入 UserDO
     * @param userModel 用户领域模型
     * @return userDO
     */
    private User convertFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }
        User user = new User();
        BeanUtils.copyProperties(userModel, user);

        return user;
    }

    /**
     * 将orm对象转为领域模型
     * @param userDO user对象
     * @param userPasswordDO 密码对象
     * @return 领域模型
     */
    private UserModel convertFromEntity(User user, UserPassword userPassword){
        if(user == null){
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(user, userModel);

        if(userPassword != null){
            userModel.setEncrptPassword(userPassword.getEncrptPassword());
        }

        return userModel;
    }

    @Override
    public UserModel getUserByIdInCache(Integer id) {
        String userValidateKeyString = "user_validate_" + id;
        UserModel userModel = (UserModel) jsonEnhancedRedisTemplate.opsForValue().get(userValidateKeyString);
        if (userModel == null) {
            userModel = getUserById(id);
            if (userModel != null) {
                jsonEnhancedRedisTemplate.opsForValue().set(userValidateKeyString, userModel, Duration.ofMinutes(10));
            }
        }
        return userModel;
    }
}
