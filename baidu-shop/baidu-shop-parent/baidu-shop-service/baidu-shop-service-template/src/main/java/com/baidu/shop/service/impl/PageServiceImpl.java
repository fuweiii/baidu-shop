//package com.baidu.shop.service.impl;
//
//import com.alibaba.fastjson.JSONObject;
//import com.baidu.shop.base.Result;
//import com.baidu.shop.dto.*;
//import com.baidu.shop.entity.*;
//import com.baidu.shop.feign.*;
//import com.baidu.shop.service.PageService;
//import com.baidu.shop.status.HTTPStatus;
//import com.baidu.shop.utils.BaiduBeanUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
///**
// * @ClassName PageServiceImpl
// * @Description: TODO
// * @Author fuwei
// * @Date 2020/9/23
// * @Version V1.0
// **/
//@Service
//@Slf4j
//public class PageServiceImpl implements PageService {
//
//    @Autowired
//    private GoodsFeign goodsFeign;
//
//    @Autowired
//    private CategoryFeign categoryFeign;
//
//    @Autowired
//    private SpecGroupFeign specGroupFeign;
//
//    @Autowired
//    private SpecParamFeign specParamFeign;
//
//    @Override
//    public Map<String, Object> getPageInfoSpuId(Integer spuId) {
//        Map<String, Object> map = new HashMap<String, Object>();
//        SpuDTO spuDTO = new SpuDTO();
//        spuDTO.setId(spuId);
//        //通过spuId查询一条数据
//        Result<List<SpuDTO>> spuInfoResult = goodsFeign.getSpuInfo(spuDTO);
//
//        if(spuInfoResult.getCode() == 200){
//            if(spuInfoResult.getData().size() == 1){
//                //spu信息
//                SpuDTO spuInfo = spuInfoResult.getData().get(0);
//                map.put("spuInfo",spuInfo);
//
//                //查询分类的信息
//                Result<List<CategoryEntity>> categoryResult = categoryFeign.getCategoryByIdList(
//                        String.join(
//                                ","
//                                , Arrays.asList(spuInfo.getCid1() + ""
//                                        , spuInfo.getCid2() + ""
//                                        , spuInfo.getCid3() + "")
//                        )
//                );
//                if(categoryResult.getCode() == 200){
//                    List<CategoryEntity> cateInfo = categoryResult.getData();
//                    map.put("cateInfo",cateInfo);
//                }
//
//                //skuInfo
//                Result<List<SkuDTO>> skusResult = goodsFeign.getSkuBySpuId(spuId);
//                if(skusResult.getCode() == 200){
//                    List<SkuDTO> data = skusResult.getData();
//                    map.put("skuInfo",data);
//                }
//
//                //spuDetail信息
//                Result<SpuDetailEntity> spuDetailResult = goodsFeign.getDetailBySpuId(spuId);
//                if(spuDetailResult.getCode() == 200){
//                    SpuDetailEntity spuDetailInfo = spuDetailResult.getData();
//                    String specialSpec = spuDetailInfo.getSpecialSpec();
//                    String genericSpec = spuDetailInfo.getGenericSpec();
//
//                    JSONObject specialSpecJson = JSONObject.parseObject(specialSpec);
//                    JSONObject genericSpecJson = JSONObject.parseObject(genericSpec);
//                    //两种方式实现
//                    map.put("genericSpec",genericSpecJson);
//                    map.put("specialSpec",specialSpecJson);
//
//                    map.put("spuDetailInfo",spuDetailInfo);
//                }
//
//                //特有规格参数
//                SpecParamDTO specParamDTO = new SpecParamDTO();
//                specParamDTO.setCid(spuInfo.getCid3());
//                specParamDTO.setGeneric(false);
//                Result<List<SpecParamEntity>> specParamResult = specParamFeign.getSpecParamInfo(specParamDTO);
//                if (specParamResult.getCode() == 200) {
//
//                    List<SpecParamEntity> specParamList = specParamResult.getData();
//                    HashMap<Integer, String> specParamMap = new HashMap<>();
//
//                    specParamList.stream().forEach(specParam ->{
//                        specParamMap.put(specParam.getId(),specParam.getName());
//                    });
//                    map.put("specParamMap",specParamMap);
//                }
//
//                //规格组
//                SpecGroupDTO specGroupDTO = new SpecGroupDTO();
//                specGroupDTO.setCid(spuInfo.getCid3());
//                Result<List<SpecgroupEntity>> specGroupResult = specGroupFeign.getSpecGroupInfo(specGroupDTO);
//                if (specGroupResult.getCode() == 200) {
//
//                    List<SpecgroupEntity> specgroupEntityList = specGroupResult.getData();
//                    List<SpecGroupDTO> specGroupDTOList = specgroupEntityList.stream().map(specGroup -> {
//                        //GroupDTO
//                        SpecGroupDTO specgroup = BaiduBeanUtil.copyProperties(specGroup, specGroupDTO.getClass());
//
//                        SpecParamDTO specParam = new SpecParamDTO();
//                        specParam.setGroupId(specGroup.getId());
//                        specParam.setGeneric(true);
//                        Result<List<SpecParamEntity>> specParamInfo = specParamFeign.getSpecParamInfo(specParam);
//
//                        if (specParamInfo.getCode() == HTTPStatus.OK) {
//                            specgroup.setSpecParamEntityList(specParamInfo.getData());
//                        }
//                        return specgroup;
//                    }).collect(Collectors.toList());
//
//                    map.put("specGroupDTOList",specGroupDTOList);
//                }
//            }
//        }
//        return map;
//    }
//
//    //新增商品  新增静态化页面
//
//}
