package com.baidu.shop.feign;

import com.baidu.shop.service.SpecgroupService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(contextId = "SpecGroupFeign", value = "xxx-service")
public interface SpecGroupFeign extends SpecgroupService {
}
