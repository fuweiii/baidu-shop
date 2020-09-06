package com.baidu.shop.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.CategoryBrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.entity.SpecgroupEntity;
import com.baidu.shop.mapper.*;
import com.baidu.shop.service.CategoryService;
import com.baidu.shop.utils.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @ClassName CategoryServiceImpl
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/8/27
 * @Version V1.0
 **/
@RestController
@Slf4j
public class CategoryServiceImpl extends BaseApiService implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private CategoryBrandMapper categoryBrandMapper;

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private SpecGroupMapper specGroupMapper;

    @Transactional
    @Override
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid) {

        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setParentId(pid);

        List<CategoryEntity> select = categoryMapper.select(categoryEntity);

        return this.setResultSuccess(select);
    }

    @Transactional
    @Override
    public Result<JSONObject> save(CategoryEntity categoryEntity) {

        CategoryEntity parentCateEntity = new CategoryEntity();
        //给当前新增数据的parentId数据赋值
        parentCateEntity.setId(categoryEntity.getParentId());
        parentCateEntity.setIsParent(1);
        //修改当前新增数据的parent值的数据的isParent为1
        categoryMapper.updateByPrimaryKeySelective(parentCateEntity);

        //新增操作
        categoryMapper.insertSelective(categoryEntity);
        //返回成功信息
        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> edit(CategoryEntity categoryEntity) {

        categoryMapper.updateByPrimaryKeySelective(categoryEntity);

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> del(Integer id) {
        String msg = "";
        //通过当前id查询分类信息
        CategoryEntity categoryEntity = categoryMapper.selectByPrimaryKey(id);
        //验证id是否有效
        //if (ObjectUtil.isNull(categoryEntity)) return this.setResultError("id有误");
        //判断当前节点是否为父节点
        //if (categoryEntity.getIsParent() == 1) return this.setResultError(msg += "Error!!!当前为父级节点,无法被删除");
        //构建条件查询  通过当前被删除节点的parentid查询数据
        Example example = new Example(CategoryEntity.class);
        example.createCriteria().andEqualTo("parentId",categoryEntity.getParentId());
        List<CategoryEntity> list = categoryMapper.selectByExample(example);
        //如果查询出来的数据只有一条
        if (list.size() == 1) { // 将父节点的isParent的数据修改为0
            CategoryEntity parentCateEntity = new CategoryEntity();
            parentCateEntity.setId(categoryEntity.getParentId());
            parentCateEntity.setIsParent(0);
            categoryMapper.updateByPrimaryKeySelective(parentCateEntity);
        }
        //查询中间表
        CategoryBrandEntity dto = new CategoryBrandEntity();
        dto.setCategoryId(id);
        Example example1 = new Example(CategoryBrandEntity.class);
        example1.createCriteria().andEqualTo("categoryId",dto.getCategoryId());
        List<CategoryBrandEntity> select = categoryBrandMapper.selectByExample(example1);

        BrandEntity brandEntity = new BrandEntity();
        Example example2 = new Example(BrandEntity.class);
        brandEntity.setId(select.get(0).getBrandId());

        example2.createCriteria().andEqualTo("id",brandEntity.getId());
        List<BrandEntity> brandEntities = brandMapper.selectByExample(example2);
        StringBuilder name = new StringBuilder();
        name.append(" < " + brandEntities.get(0).getName() + " > ");

        if(!select.isEmpty()) return this.setResultError(msg += "当前分类下已被" + name + "品牌绑定,不可被删除");
        //查询规格组
        Example example3 = new Example(SpecgroupEntity.class);
        SpecgroupEntity specgroupEntity = new SpecgroupEntity();
        specgroupEntity.setCid(id);
        example3.createCriteria().andEqualTo("cid",specgroupEntity.getCid());
        List<SpecgroupEntity> specgroupEntities = specGroupMapper.selectByExample(example3);
        if(!specgroupEntities.isEmpty()) return this.setResultError(msg += "当前分类下存在规格组,被规格组绑定");

        categoryMapper.deleteByPrimaryKey(id);//执行删除
        return this.setResultSuccess();
    }

    @Override
    public Result<List<CategoryEntity>> getByBrand(Integer brandId) {

        List<CategoryEntity> list = categoryMapper.getByBrandId(brandId);
        return this.setResultSuccess(list);
    }


}
