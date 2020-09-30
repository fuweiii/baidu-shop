package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.DTO.SearchDTO;
import com.baidu.shop.base.Result;
import com.baidu.shop.response.GoodsResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @ClassName ShopElasticsearchService
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/9/16
 * @Version V1.0
 **/
@Api(tags = "es接口")
public interface ShopElasticsearchService {

    @ApiOperation(value = "清除索引")
    @GetMapping(value = "es/clearGoodsEsData")
    Result<JSONObject> clearGoodsEsDatas();

    @ApiOperation(value = "ES商品数据初始化-->索引创建,映射创建,mysql数据同步")
    @GetMapping(value = "es/initGoodsEsData")
    Result<JSONObject> initGoodsEsData();

    @ApiOperation(value = "ES查询")
    @GetMapping(value = "es/search")
    GoodsResponse search(@SpringQueryMap SearchDTO searchDTO);

    @ApiOperation(value = "删除静态化页面附带删除class")
    @GetMapping(value = "es/deleteGoodsBySpuId")
    Result<JSONObject> deleteGoodsBySpuId(@RequestParam String spuId);

    @ApiOperation(value = "新增商品附带新增elasticSearchTemplate")
    @GetMapping(value = "es/saveElasticSearchTemplate")
    Result<JSONObject> saveElasticSearchTemplate(@RequestParam Integer id);
}
