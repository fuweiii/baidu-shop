package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.entity.SpuEntity;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName GoodsService
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/9/7
 * @Version V1.0
 **/
@Api(tags = "商品接口")
public interface GoodsService {

    @ApiOperation(value = "获得spu商品信息")
    @GetMapping(value = "goods/getSpuInfo")
    Result<List<SpuDTO>> getSpuInfo(@SpringQueryMap SpuDTO spuDTO);

    @ApiOperation(value = "新增商品信息")
    @PostMapping(value = "goods/postSpu")
    Result<JSONObject> postSpu(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "获取spu详情信息,修改商品回显")
    @GetMapping(value = "goods/getDetailBySpuId")
    Result<SpuDetailEntity> getDetailBySpuId(@RequestParam Integer spuId);

    @ApiOperation(value = "获取spu详情信息,修改商品回显")
    @GetMapping(value = "goods/getSkuBySpuId")
    Result<List<SkuDTO>> getSkuBySpuId(@RequestParam Integer spuId);

    @ApiOperation(value = "修改商品信息")
    @PutMapping(value = "goods/postSpu")
    Result<JSONObject> editSpu(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "删除商品信息")
    @DeleteMapping(value ="goods/del")
    Result<JSONObject> delSpu(Integer spuId);

    @ApiOperation(value = "下架商品请求")
    @PutMapping(value = "goods/xiajia")
    Result<JSONObject> xiajia(@RequestBody SpuDTO spuDTO);

//    @ApiOperation(value = "上架商品请求")
//    @PutMapping(value = "goods/shangjia")
//    Result<JSONObject> shangjia(@RequestBody SpuDTO spuDTO);
}
