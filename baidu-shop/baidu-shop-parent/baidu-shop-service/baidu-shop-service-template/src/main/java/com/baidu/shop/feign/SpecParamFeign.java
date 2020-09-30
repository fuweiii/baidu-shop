package com.baidu.shop.feign;

import com.baidu.shop.service.SpecParamService;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @ClassName SpecParamFeign
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/9/17
 * @Version V1.0
 **/
@FeignClient(contextId = "SpecParamService", value = "xxx-service")
public interface SpecParamFeign extends SpecParamService {
}
