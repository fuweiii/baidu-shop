package com.baidu.shop.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecGroupDTO;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.entity.SpecgroupEntity;
import com.baidu.shop.mapper.SpecGroupMapper;
import com.baidu.shop.mapper.SpecParamMapper;
import com.baidu.shop.service.SpecgroupService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName SpecGroupServiceImpl
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/9/3
 * @Version V1.0
 **/
@RestController
public class SpecGroupServiceImpl extends BaseApiService implements SpecgroupService {

    @Resource
    private SpecGroupMapper specGroupMapper;

    @Resource
    private SpecParamMapper specParamMapper;

    @Override
    public Result<List<SpecgroupEntity>> getSpecGroupInfo(SpecGroupDTO specGroupDTO) {

        Example example = new Example(SpecgroupEntity.class);

        if(ObjectUtil.isNotNull(specGroupDTO.getCid())) example.createCriteria().andEqualTo("cid",specGroupDTO.getCid());
        List<SpecgroupEntity> list = specGroupMapper.selectByExample(example);
        return this.setResultSuccess(list);
    }

    @Transactional
    @Override
    public Result<JSONObject> save(SpecGroupDTO specGroupDTO) {
        specGroupMapper.insertSelective(BaiduBeanUtil.copyProperties(specGroupDTO,SpecgroupEntity.class));
        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> edit(SpecGroupDTO specGroupDTO) {
        specGroupMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(specGroupDTO,SpecgroupEntity.class));
        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> delete(Integer id) {
        //删除中间表数据
        //查询是否被绑定
        String msg = "";
        SpecParamEntity specParamEntity = new SpecParamEntity();
        specParamEntity.setGroupId(id);
        Example example = new Example(SpecParamEntity.class);
        example.createCriteria().andEqualTo("groupId",specParamEntity.getGroupId());
        List<SpecParamEntity> specParamEntities = specParamMapper.selectByExample(example);
        if(!specParamEntities.isEmpty())   return this.setResultError(msg += "该规格组下存在参数");

        specParamMapper.deleteByPrimaryKey(specParamEntities.get(0).getGroupId());

        specGroupMapper.deleteByPrimaryKey(id);
        return this.setResultSuccess();
    }
}
