package com.baidu.shop.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Table;

/**
 * @ClassName StockDTO
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/9/8
 * @Version V1.0
 **/
@Data
@Table(name = "tb_stock")
public class StockDTO {

    @ApiModelProperty(value = "主键",example = "1")
    private Long skuId;

    @ApiModelProperty(value = "可秒杀库存",example = "1")
    private Integer seckillStock;

    @ApiModelProperty(value = "秒杀总数量",example = "1")
    private Integer seckillTotal;

    @ApiModelProperty(value = "库存数量",example = "1")
    private Integer stock;
}
