package com.baidu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import tk.mybatis.spring.annotation.MapperScan;
/**
 * @ClassName RunXXXApplication
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/8/27
 * @Version V1.0
 **/
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@MapperScan("com.baidu.shop.mapper")
public class RunXXXApplication {

    public static void main(String[] args) {
        SpringApplication.run(RunXXXApplication.class);
    }
}
