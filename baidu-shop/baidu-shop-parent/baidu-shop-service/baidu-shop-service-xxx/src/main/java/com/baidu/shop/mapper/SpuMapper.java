package com.baidu.shop.mapper;

import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.SpuEntity;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuMapper extends Mapper<SpuEntity>{
    List<SpuDTO> querySpuDetail(SpuDTO spuDTO);
}
