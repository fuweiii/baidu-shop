package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.UserDTO;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName UserService
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/10/13
 * @Version V1.0
 **/
@Api(tags = "用户接口")
public interface UserService {

    @ApiOperation(value = "注册方法")
    @PostMapping(value = "user/register")
    Result<JSONObject> register(@Validated({MingruiOperation.Add.class}) @RequestBody UserDTO userDTO);

    @ApiOperation(value = "校验用户/手机号")
    @GetMapping(value = "user/check/{value}/{type}")
    Result<List<UserEntity>> check(@PathVariable(value = "value") String value, @PathVariable(value = "type") Integer type);

    @ApiOperation(value = "手机发送验证码")
    @PostMapping(value = "user/sendValidCode")
    Result<JSONObject> endValidCode(@RequestBody UserDTO userDTO);


    @ApiOperation(value = "校验验证码")
    @GetMapping(value = "user/checkValidCode")
    Result<JSONObject> checlValidCode(String phone ,String codeValid);
}
