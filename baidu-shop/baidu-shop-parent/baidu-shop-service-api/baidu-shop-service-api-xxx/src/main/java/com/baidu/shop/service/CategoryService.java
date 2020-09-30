package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "商品分类接口")
public interface CategoryService {

    @ApiOperation(value = "通过查询商品分类")
    @GetMapping(value = "category/list")
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid);

    @ApiOperation(value = "新增商品类目")
    @PostMapping(value = "category/save")
    Result<JSONObject> save(@Validated({MingruiOperation.Add.class}) @RequestBody CategoryEntity categoryEntity);

    @ApiOperation(value = "修改商品类目")
    @PutMapping(value = "category/edit")
    Result<JSONObject> edit(@Validated({MingruiOperation.Update.class}) @RequestBody CategoryEntity categoryEntity);

    @ApiOperation(value = "删除商品类目")
    @DeleteMapping(value = "category/del")
    Result<JSONObject> del(Integer id);

    @ApiOperation(value = "通过品牌id查询商品分类")
    @GetMapping(value = "category/getBrand")
    Result<List<CategoryEntity>> getByBrand(Integer brandId);

    @ApiOperation(value = "通过分类id集合查询商品分类")
    @GetMapping(value = "category/getCategoryByIdList")
    Result<List<CategoryEntity>> getCategoryByIdList(@RequestParam String cidsStr);

}
