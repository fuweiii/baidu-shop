package com.baidu.shop.service.impl;
import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.*;
import com.baidu.shop.entity.*;
import com.baidu.shop.feign.*;
import com.baidu.shop.service.TemplateService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BaiduBeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName TemplateServiceImpl
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/9/24
 * @Version V1.0
 **/
@RestController
public class TemplateServiceImpl extends BaseApiService implements TemplateService {

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    @Autowired
    private SpecParamFeign specParamFeign;

    @Autowired
    private BrandFeign brandFeign;

    @Autowired
    private SpecGroupFeign specGroupFeign;

    //注入静态化模版
    @Autowired
    private TemplateEngine templateEngine;

    //静态文件生成的路径
    @Value(value = "${mrshop.static.html.path}")
    private String staticHTMLPath;


    @Override
    public Result<JSONObject> createStaticHTMLTemplate(Integer spuId) {
        Map<String, Object> map = this.getPageInfoSpuId(spuId);

        //创建模板引擎上下文
        Context context = new Context();
        //将所有准备的数据放到模板中
        context.setVariables(map);

        //创建文件 param1:文件路径 param2:文件名称
        File file = new File(staticHTMLPath, spuId    + ".html");
        //构建文件输出流
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file, "UTF-8");
            //根据模板生成静态文件
            //param1:模板名称 params2:模板上下文[上下文中包含了需要填充的数据],文件输出流
            templateEngine.process("web",context,writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }finally {
            writer.close();
        }

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> initStaticHTMLTemplate() {
        //获取所有的spu信息,注意:应该写一个只获取id集合的接口,我只是为了省事
        Result<List<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(new SpuDTO());
        if(spuInfo.getCode() == 200){

            List<SpuDTO> spuList = spuInfo.getData();

            spuList.stream().forEach(spu -> {
                createStaticHTMLTemplate(spu.getId());
            });
        }
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> delHTMLBySpuId(Integer spuId) {
        File file = new File(staticHTMLPath + File.separator + spuId + ".html");

        if(!file.delete()){
            return this.setResultError("文件删除失败");
        }
        return this.setResultSuccess();
    }

    private Map<String, Object> getPageInfoSpuId(Integer spuId) {
        Map<String, Object> map = new HashMap<String, Object>();
        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);
        //通过spuId查询一条数据
        Result<List<SpuDTO>> spuInfoResult = goodsFeign.getSpuInfo(spuDTO);
        if(spuInfoResult.getCode() == 200){
            if(spuInfoResult.getData().size() == 1){
                //spu信息
                SpuDTO spuInfo = spuInfoResult.getData().get(0);
                map.put("spuInfo",spuInfo);
                //查询分类的信息
                List<CategoryEntity> cateList = this.getCateList(spuInfo);
                map.put("cateInfo",cateList);
                //skuInfo
                List<SkuDTO> skuList = this.getSkuList(spuId);
                map.put("skuInfo",skuList);
                //spuDetail信息
                SpuDetailEntity spuDetail = this.getSpuDetail(spuId);
                map.put("spuDetailInfo",spuDetail);
                //特有规格参数
                HashMap<Integer, String> specParamMap = this.getSpecParam(spuInfo.getCid3());
                map.put("specParamMap",specParamMap);
                //规格组
                List<SpecGroupDTO> specGroupDTOList = this.getSpecGroup(spuInfo.getCid3());
                map.put("specGroupDTOList",specGroupDTOList);
            }
        }
        return map;
    }
    //spuDetail
    private SpuDetailEntity getSpuDetail(Integer spuId){
        Result<SpuDetailEntity> spuDetailResult = goodsFeign.getDetailBySpuId(spuId);
        if(spuDetailResult.getCode() == 200){
            return spuDetailResult.getData();
//                    String specialSpec = spuDetailInfo.getSpecialSpec();
//                    String genericSpec = spuDetailInfo.getGenericSpec();
//                    JSONObject specialSpecJson = JSONObject.parseObject(specialSpec);
//                    JSONObject genericSpecJson = JSONObject.parseObject(genericSpec);
//                    //两种方式实现
//                    map.put("genericSpec",genericSpecJson);
//                    map.put("specialSpec",specialSpecJson);
        }
        return null;
    }
    //分类
    private List<CategoryEntity> getCateList(SpuDTO spuInfo){
        Result<List<CategoryEntity>> categoryResult = categoryFeign.getCategoryByIdList(
                String.join(
                        ","
                        , Arrays.asList(spuInfo.getCid1() + ""
                                , spuInfo.getCid2() + ""
                                , spuInfo.getCid3() + "")
                )
        );
        if(categoryResult.getCode() == 200){
            return categoryResult.getData();
        }
        return null;
    }
    //skuInfo
    private List<SkuDTO> getSkuList(Integer spuId){
        Result<List<SkuDTO>> skusResult = goodsFeign.getSkuBySpuId(spuId);
        if(skusResult.getCode() == 200){
            return skusResult.getData();
        }
        return null;
    }
    //特有规格参数
    private HashMap<Integer, String> getSpecParam(Integer cid3){
        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(cid3);
        specParamDTO.setGeneric(false);
        Result<List<SpecParamEntity>> specParamResult = specParamFeign.getSpecParamInfo(specParamDTO);
        if (specParamResult.getCode() == 200) {

            List<SpecParamEntity> specParamList = specParamResult.getData();
            HashMap<Integer, String> specParamMap = new HashMap<>();

            specParamList.stream().forEach(specParam ->{
                specParamMap.put(specParam.getId(),specParam.getName());
            });
            return specParamMap;
        }
        return null;
    }
    //规格组
    private List<SpecGroupDTO> getSpecGroup(Integer cid3){
        SpecGroupDTO specGroupDTO = new SpecGroupDTO();
        specGroupDTO.setCid(cid3);
        Result<List<SpecgroupEntity>> specGroupResult = specGroupFeign.getSpecGroupInfo(specGroupDTO);
        if (specGroupResult.getCode() == 200) {

            List<SpecgroupEntity> specgroupEntityList = specGroupResult.getData();
            List<SpecGroupDTO> specGroupDTOList = specgroupEntityList.stream().map(specGroup -> {
                //GroupDTO
                SpecGroupDTO specgroup = BaiduBeanUtil.copyProperties(specGroup, specGroupDTO.getClass());

                SpecParamDTO specParam = new SpecParamDTO();
                specParam.setGroupId(specGroup.getId());
                specParam.setGeneric(true);
                Result<List<SpecParamEntity>> specParamInfo = specParamFeign.getSpecParamInfo(specParam);

                if (specParamInfo.getCode() == HTTPStatus.OK) {
                    specgroup.setSpecParamEntityList(specParamInfo.getData());
                }
                return specgroup;
            }).collect(Collectors.toList());
            return specGroupDTOList;
        }
        return null;
    }
}