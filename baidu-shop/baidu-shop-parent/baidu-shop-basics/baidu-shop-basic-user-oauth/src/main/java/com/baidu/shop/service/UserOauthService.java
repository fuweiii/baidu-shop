package com.baidu.shop.service;

import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.enrity.UserEntity;

public interface UserOauthService {
    String login(UserEntity userEntity, JwtConfig jwtConfig);
}
