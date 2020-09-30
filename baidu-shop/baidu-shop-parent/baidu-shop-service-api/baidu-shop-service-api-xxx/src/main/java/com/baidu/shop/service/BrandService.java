package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName BrandService
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/8/31
 * @Version V1.0
 **/
@Api(tags = "品牌管理接口")
public interface BrandService {

    @ApiOperation(value = "查询品牌")
    @GetMapping(value = "brand/getBrandInfo")
    Result<PageInfo<BrandEntity>> getBrandInfo(@SpringQueryMap BrandDTO brandDTO);

    @ApiOperation(value = "新增品牌")
    @PostMapping(value = "brand/save")
    Result<JSONObject> saveBrand(@Validated({MingruiOperation.Add.class}) @RequestBody BrandDTO brandDTO);

    @ApiOperation(value = "修改品牌信息")
    @PutMapping(value = "brand/save")
    Result<JSONObject> editBrand(@Validated({MingruiOperation.Update.class}) @RequestBody BrandDTO brandDTO);

    @ApiOperation(value = "删除品牌信息")
    @DeleteMapping(value = "brand/deleteBrand")
    Result<JSONObject> delBrand(Integer id);

    //通过分类查询品牌 商品新增
    @ApiOperation(value = "通过分类查询品牌")
    @GetMapping(value = "brand/getBrandByCate")
    Result<List<BrandEntity>> getBrandByCate(Integer cid);

    @GetMapping(value = "brand/getBrandByIdList")
    @ApiOperation(value = "通过品牌id集合查询品牌信息")
    Result<List<BrandEntity>> getBrandByIdList(@RequestParam String brandIdsStr);
}
