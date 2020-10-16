package com.baidu.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName UserInfo
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/10/15
 * @Version V1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    private Integer id;

    private String username;
}
