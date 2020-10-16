package com.baidu.shop.service.impl;

import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.enrity.UserEntity;
import com.baidu.shop.mapper.UserOauthMapper;
import com.baidu.shop.service.UserOauthService;
import com.baidu.shop.utils.BCryptUtil;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName UserOauthServiceImpl
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/10/15
 * @Version V1.0
 **/
@Service
public class UserOauthServiceImpl implements UserOauthService {

    @Resource
    private UserOauthMapper userOauthMapper;

    @Override
    public String login(UserEntity userEntity, JwtConfig jwtConfig) {

        Example example = new Example(UserEntity.class);
        example.createCriteria().andEqualTo("username",userEntity.getUsername());

        List<UserEntity> userEntities = userOauthMapper.selectByExample(example);

        String token = null;
        if (userEntities.size() == 1) {
            UserEntity entity = userEntities.get(0);
            //判断密码
            if(BCryptUtil.checkpw(userEntity.getPassword(),entity.getPassword())){

                try {
                    token = JwtUtils.generateToken(new UserInfo(entity.getId(),entity.getUsername()),jwtConfig.getPrivateKey(),jwtConfig.getExpire());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return token;
    }
}
