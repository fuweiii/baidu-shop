package com.baidu.shop.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.entity.SpuEntity;
import com.baidu.shop.mapper.BrandMapper;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.mapper.SpuMapper;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.PinyinUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName BrandServiceImpl
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/8/31
 * @Version V1.0
 **/
@RestController
public class BrandServiceImpl extends BaseApiService implements BrandService {

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private CategoryBrandMapper categoryBrandMapper;

    @Resource
    private SpuMapper spuMapper;

    @Override
    public Result<PageInfo<BrandEntity>> getBrandInfo(BrandDTO brandDTO) {

        //分页
        if (ObjectUtil.isNotNull(brandDTO.getPage()) && ObjectUtil.isNotNull(brandDTO.getRows()))
            PageHelper.startPage(brandDTO.getPage(),brandDTO.getRows());

        Example example = new Example(BrandEntity.class);
        //排序/条件查询
        if (ObjectUtil.isNotNull(brandDTO.getSort())) example.setOrderByClause(brandDTO.getOrderByClauser());

//        Example.Criteria criteria = example.createCriteria();
//        //条件查询
//        if (StringUtil.isNotEmpty(brandDTO.getName())) criteria.andLike("name","%" + brandDTO.getName() + "%");

        Example.Criteria criteria = example.createCriteria();

        if(ObjectUtil.isNotNull(brandDTO.getId()))
            criteria.andEqualTo("id",brandDTO.getId());

        if(StringUtil.isNotEmpty(brandDTO.getName()))
            criteria.andLike("name","%" + brandDTO.getName() + "%");
        //查询
        List<BrandEntity> list = brandMapper.selectByExample(example);
        //把参数放到pageinfo中
        PageInfo<BrandEntity> pageInfo = new PageInfo<>(list);

        return this.setResultSuccess(pageInfo);
    }

    @Override
    public Result<List<BrandEntity>> getBrandByIdList(String brandIdsStr) {

        List<Integer> brandIdList = Arrays.asList(brandIdsStr.split(","))
                .stream().map(brandId -> Integer.parseInt(brandId))
                .collect(Collectors.toList());
        List<BrandEntity> list = brandMapper.selectByIdList(brandIdList);

        return this.setResultSuccess(list);
    }

    @Transactional
    @Override
    public Result<JSONObject> saveBrand(BrandDTO brandDTO) {

        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);
        //获取到品牌名称
        //获取到品牌名称第一个字符
        //将第一个字符转换为pinyin
        //获取拼音的首字母
        //统一转为大写
        /*String name = brandEntity.getName();
        char c = name.charAt(0);
        String upperCase = PinyinUtil.getUpperCase(String.valueOf(c), PinyinUtil.TO_FIRST_CHAR_PINYIN);
        brandEntity.setLetter(upperCase.charAt(0));*/
        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().charAt(0))
                ,PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));

        //普通新增
        brandMapper.insertSelective(brandEntity);

        /*if(brandDTO.getCategory().contains(",")){

            String[] cidArr = brandDTO.getCategory().split(",");
            List<String> list = Arrays.asList(cidArr);

            *//*List<CategoryBrandEntity> cblist = new ArrayList<>();
            list.stream().forEach(cid ->{
                CategoryBrandEntity entity = new CategoryBrandEntity();
                entity.setBrandId(brandEntity.getId());
                entity.setCategoryId(StringUtil.toInteger(cid));
                cblist.add(entity);
            });*/
        /*
            //通过split方法分割字符串的Array
            //Arrays.asList将Array转换为List
            //使用JDK1,8的stream
            //使用map函数返回一个新的数据
            //collect 转换集合类型Stream<T>
            //Collectors.toList())将集合转换为List类型
            List<CategoryBrandEntity> collect = list.stream().map(cid -> {
                CategoryBrandEntity entity = new CategoryBrandEntity();
                entity.setBrandId(brandEntity.getId());
                entity.setCategoryId(StringUtil.toInteger(cid));
                return entity;
            }).collect(Collectors.toList());
            //批量新增
            categoryBrandMapper.insertList(collect);
        }else{
            CategoryBrandEntity entity = new CategoryBrandEntity();
            //赋值
            entity.setBrandId(brandEntity.getId());
            entity.setCategoryId(StringUtil.toInteger(brandDTO.getCategory()));
            //往中间表新增数据
            categoryBrandMapper.insertSelective(entity);
        }*/
        this.insertCategoryAndBrand(brandDTO,brandEntity);
        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> editBrand(BrandDTO brandDTO) {
        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);
        //拼音首字母
        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().charAt(0))
                ,PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));

        //执行修改操作
        brandMapper.updateByPrimaryKeySelective(brandEntity);

        //操作中间表,删除brandId的数据
        this.deleteCategoryAndBrand(brandEntity.getId());
        //新增中间表数据
        this.insertCategoryAndBrand(brandDTO,brandEntity);

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> delBrand(Integer id) {

        Example example = new Example(SpuEntity.class);
        example.createCriteria().andEqualTo("brandId",id);
        List<SpuEntity> spuEntities = spuMapper.selectByExample(example);
        if(spuEntities.size() > 0) return this.setResultError("该品牌已被商品绑定");

        brandMapper.deleteByPrimaryKey(id);
        this.deleteCategoryAndBrand(id);

        return this.setResultSuccess();
    }

    @Override
    public Result<List<BrandEntity>> getBrandByCate(Integer cid) {
        //两表联查
        Example example = new Example(CategoryBrandEntity.class);
        example.createCriteria().andEqualTo("categoryId",cid);
        List<CategoryBrandEntity> categoryBrandEntities = categoryBrandMapper.selectByExample(example);

      List<Integer> arr = new ArrayList<>();
      categoryBrandEntities.forEach(cb -> arr.add(cb.getBrandId()));
        List<BrandEntity> list = brandMapper.selectByIdList(arr);

        return this.setResultSuccess(list);
    }

    //删除中间表数据
    private void deleteCategoryAndBrand(Integer id){
        Example example = new Example(CategoryBrandEntity.class);
        example.createCriteria().andEqualTo("brandId",id);
        categoryBrandMapper.deleteByExample(example);
    }

    //封装公共方法(公共代码)
    private void insertCategoryAndBrand(BrandDTO brandDTO,BrandEntity brandEntity){
        if(brandDTO.getCategory().contains(",")){
            List<CategoryBrandEntity> categoryBrandEntities = Arrays.asList(brandDTO.getCategory().split(","))
                    .stream().map(cid -> {
                        CategoryBrandEntity entity = new CategoryBrandEntity();
                        entity.setCategoryId(StringUtil.toInteger(cid));
                        entity.setBrandId(brandEntity.getId());
                        return entity;
                    }).collect(Collectors.toList());
            categoryBrandMapper.insertList(categoryBrandEntities);
        }else{
            CategoryBrandEntity entity = new CategoryBrandEntity();
            entity.setCategoryId(StringUtil.toInteger(brandDTO.getCategory()));
            entity.setBrandId(brandEntity.getId());
            categoryBrandMapper.insertSelective(entity);
        }
    }

}
