package com.baidu.shop.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.global.ZiDingYi;
import com.baidu.shop.mapper.SpecParamMapper;
import com.baidu.shop.service.SpecParamService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName SpecParamServiceImpl
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/9/3
 * @Version V1.0
 **/
@RestController
public class SpecParamServiceImpl extends BaseApiService implements SpecParamService {

    @Resource
    private SpecParamMapper specParamMapper;

    @Override
    public Result<List<SpecParamEntity>> getSpecParamInfo(SpecParamDTO specParamDTO) {

       // if(specParamDTO.getGroupId() == null && specParamDTO.getGroupId() == 0) throw new ZiDingYi(500,"error!!!groupId isNull");
        Example example = new Example(SpecParamEntity.class);
        Example.Criteria criteria = example.createCriteria();
        //判断groupId不为空
        if(ObjectUtil.isNotNull(specParamDTO.getGroupId())) criteria.andEqualTo("groupId",specParamDTO.getGroupId());
        //判断cid不为空
        if(ObjectUtil.isNotNull(specParamDTO.getCid())) criteria.andEqualTo("cid",specParamDTO.getCid());
        //判断是否为搜索条件不为空
        if (ObjectUtil.isNotNull(specParamDTO.getSearching())) criteria.andEqualTo("searching",specParamDTO.getSearching());
        //判断是否为
        if (ObjectUtil.isNotNull(specParamDTO.getGeneric())) criteria.andEqualTo("generic",specParamDTO.getGeneric());

        List<SpecParamEntity> list = specParamMapper.selectByExample(example);
        return this.setResultSuccess(list);
    }

    @Override
    public Result<JSONObject> saveParam(SpecParamDTO specParamDTO) {
        specParamMapper.insertSelective(BaiduBeanUtil.copyProperties(specParamDTO,SpecParamEntity.class));
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> editParam(SpecParamDTO specParamDTO) {
        specParamMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(specParamDTO,SpecParamEntity.class));
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> delParam(Integer id) {
        specParamMapper.deleteByPrimaryKey(id);
        return this.setResultSuccess();
    }
}
