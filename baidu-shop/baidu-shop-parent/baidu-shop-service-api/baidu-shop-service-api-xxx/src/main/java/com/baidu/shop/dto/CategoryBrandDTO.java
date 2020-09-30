package com.baidu.shop.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.Table;

/**
 * @ClassName CategoryBrandEntity
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/9/1
 * @Version V1.0
 **/
@Data
@ApiModel(value = "分类规格数据传输DTO")
public class CategoryBrandDTO {

    private Integer categoryId;

    private Integer brandId;
}
