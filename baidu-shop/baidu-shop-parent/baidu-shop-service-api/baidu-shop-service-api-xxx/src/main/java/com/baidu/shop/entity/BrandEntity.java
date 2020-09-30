package com.baidu.shop.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ClassName BrandEntity
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/8/31
 * @Version V1.0
 **/
@ApiModel(value = "品牌管理实现类")
@Table(name = "tb_brand")
@Data
public class BrandEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//使用的主键生成策略
    private Integer id;

    private String name;

    private String image;

    private Character letter;

}
