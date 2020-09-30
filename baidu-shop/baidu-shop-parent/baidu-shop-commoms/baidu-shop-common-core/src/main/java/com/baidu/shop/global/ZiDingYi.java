package com.baidu.shop.global;

import net.bytebuddy.implementation.bind.annotation.Super;

/**
 * @ClassName Exception
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/9/9
 * @Version V1.0
 **/
public class ZiDingYi extends RuntimeException {

    private Integer code;

    private String msg;

    public ZiDingYi(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}