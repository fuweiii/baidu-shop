package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @ClassName TemplateService
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/9/24
 * @Version V1.0
 **/
@Api(tags = "静态化页面接口")
public interface TemplateService {

    @ApiOperation(value = "新增数据静态化页面方法")
    @PostMapping(value = "template/createStaticHTMLTemplate")
    Result<JSONObject> createStaticHTMLTemplate(@RequestBody Integer spuId);

    @ApiOperation(value = "All页面静态化方法")
    @GetMapping(value = "template/initStaticHTMLTemplate")
    Result<JSONObject> initStaticHTMLTemplate();

}
