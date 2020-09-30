package com.baidu.shop.base;

import com.baidu.shop.utils.StringUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * @ClassName BaseDTO
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/8/31
 * @Version V1.0
 **/
@Data
@ApiModel(value = "BaseDTO用于数据传输,其他dto需要继承此类")
public class BaseDTO {

    @ApiModelProperty(value = "当前页", example = "1")
    private Integer page;

    @ApiModelProperty(value = "每页显示多少条",example = "5")
    private Integer rows;

    @ApiModelProperty(value = "排序字段")
    private String sort;

    @ApiModelProperty(value = "是否升序")
    private Boolean order;


    /*@ApiModelProperty(hidden = true)
    public String getOrderByClause(){

        if(!StringUtils.isEmpty(sort))return sort + " " +
                order.replace("false","asc").replace("true","desc");
        return "";
    }*/

    @ApiModelProperty(hidden = true)
    public String getOrderByClauser(){
        if(StringUtil.isNotEmpty(sort)) return sort + " " + (order?"desc":"");
        return null;
    }
}
