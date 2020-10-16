package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.constant.RegisterConstant;
import com.baidu.shop.dto.UserDTO;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.mapper.UserMapper;
import com.baidu.shop.redis.repository.RedisRepository;
import com.baidu.shop.service.UserService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BCryptUtil;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.LuosimaoDuanxinUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @ClassName UserServiceImpl
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/10/13
 * @Version V1.0
 **/
@RestController
@Slf4j
public class UserServiceImpl extends BaseApiService implements UserService {

    @Resource
    private UserMapper userMapper;

    @Autowired
    private RedisRepository redisRepository;

    @Override
    public Result<JSONObject> register(UserDTO userDTO) {

        UserEntity userEntity = BaiduBeanUtil.copyProperties(userDTO, UserEntity.class);
        userEntity.setPassword(BCryptUtil.hashpw(userEntity.getPassword(),BCryptUtil.gensalt()));
        userEntity.setCreated(new Date());
        userMapper.insertSelective(userEntity);
        return this.setResultSuccess();
    }

    @Override
    public Result<List<UserEntity>> check(String value, Integer type) {
        Example example = new Example(UserEntity.class);
        Example.Criteria criteria = example.createCriteria();
        if (type != null && value != null ){
            if(type == RegisterConstant.USER_TYPE_USERNAME){
                criteria.andEqualTo("username",value);
            }else if(type == RegisterConstant.USER_TYPE_PHONE){
                criteria.andEqualTo("phone",value);
            }
        }
        List<UserEntity> userEntities = userMapper.selectByExample(example);
        return this.setResultSuccess(userEntities);
    }

    @Override
    public Result<JSONObject> endValidCode(UserDTO userDTO) {
        //生成随机6位验证码
        String code = (int)((Math.random() * 9 + 1) * 100000) + "";
        //发送短信验证码
//        LuosimaoDuanxinUtil.SendCode(userDTO.getPhone(),code);
        //短信条数只有10条,不够我们测试.所以就不发送短信验证码了,直接在控制台打印就可以
        log.debug("向手机号码:{} 发送验证码:{}",userDTO.getPhone(),code);

        redisRepository.set(RegisterConstant.USER_PHONE_CODE + userDTO.getPhone(),code);
        redisRepository.expire(RegisterConstant.USER_PHONE_CODE + userDTO.getPhone(),120);

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> checlValidCode(String phone, String codeValid) {

        String s = redisRepository.get(RegisterConstant.USER_PHONE_CODE + phone);
        if(!codeValid.equals(s)) return this.setResultError(HTTPStatus.PHONE_VALIDATE_ERROR,"验证码校验失败");

        return this.setResultSuccess();
    }
}
