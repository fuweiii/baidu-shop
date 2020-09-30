package com.baidu.shop.feign;

import com.baidu.shop.service.ShopElasticsearchService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "search-server",contextId = "ShopElasticsearchService")
public interface SearchFeign extends ShopElasticsearchService {
}
