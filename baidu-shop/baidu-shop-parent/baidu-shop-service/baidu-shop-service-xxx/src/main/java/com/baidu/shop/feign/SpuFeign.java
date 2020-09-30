package com.baidu.shop.feign;

import com.baidu.shop.service.TemplateService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(contextId = "TemplateService",value = "template-server")
public interface SpuFeign extends TemplateService {
}
