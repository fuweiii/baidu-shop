package com.baidu.shop.web;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.enrity.UserEntity;
import com.baidu.shop.service.UserOauthService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.CookieUtils;
import com.baidu.shop.utils.JwtUtils;
import com.baidu.shop.utils.ObjectUtil;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName UserOauthController
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/10/15
 * @Version V1.0
 **/
@RestController
@RequestMapping(value = "oauth")
@Api(tags = "用户登录方法接口")
public class UserOauthController extends BaseApiService {

    @Autowired
    private UserOauthService userOauthService;

    @Autowired
    private JwtConfig jwtConfig;

    @ApiOperation(value = "登录方法")
    @PostMapping(value = "user/login")
    public Result<JsonObject> login(@RequestBody UserEntity userEntity
                                        , HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){

        String token = userOauthService.login(userEntity,jwtConfig);

        if (ObjectUtil.isNull(token)) {
            return this.setResultError(HTTPStatus.VALID_USER_PASSWORD_ERROR,"用户名或密码错误");
        }
        CookieUtils.setCookie(httpServletRequest,httpServletResponse
                ,jwtConfig.getCookieName(),token,jwtConfig.getCookieMaxAge());
        return this.setResultSuccess();
    }

    @GetMapping(value = "user/checkUserLogin")
    @ApiOperation(value = "验证用户是否是登录状态")
    public Result<UserInfo> checkUserLogin(@CookieValue(value = "MRSHOP_TOKEN") String token){

        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());
            return this.setResultSuccess(userInfo);
        } catch (Exception e) {//如果有异常 说明token有问题
            //e.printStackTrace();
            //应该新建http状态为用户验证失败,状态码为403
            return this.setResultError(403,"");
        }
    }

}
