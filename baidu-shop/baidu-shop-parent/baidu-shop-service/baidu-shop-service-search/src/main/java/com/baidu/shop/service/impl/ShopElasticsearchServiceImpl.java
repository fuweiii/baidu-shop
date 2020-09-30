package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.DTO.SearchDTO;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecParamFeign;
import com.baidu.shop.global.ZiDingYi;
import com.baidu.shop.response.GoodsResponse;
import com.baidu.shop.service.ShopElasticsearchService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.ESHighLightUtil;
import com.baidu.shop.utils.JSONUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName ShopElasticsearchServiceImpl
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/9/16
 * @Version V1.0
 **/
@RestController
@Slf4j
public class ShopElasticsearchServiceImpl extends BaseApiService implements ShopElasticsearchService {

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private SpecParamFeign specParamFeign;

    @Resource
    private CategoryFeign categoryFeign;

    @Resource
    private BrandFeign brandFeign;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public GoodsResponse search(SearchDTO searchDTO) {
        //searcch 查询   page 页面    filter 条件查询字段
        if(StringUtil.isEmpty(searchDTO.getSearch())) throw new ZiDingYi(500,"没有查询参数");
        //调用构建查询条件方法,并生成变量
        NativeSearchQueryBuilder queryBuilder = this.getQueryBuilder(searchDTO.getSearch(),searchDTO.getPage(),searchDTO.getFilter());
        SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsDoc.class);

        //执行高亮方法
        List<SearchHit<GoodsDoc>> highLightHit = ESHighLightUtil.getHighLightHit(searchHits.getSearchHits());
        //要返回的数据
        List<GoodsDoc> goodsList = highLightHit.stream().map(searchHit -> searchHit.getContent()).collect(Collectors.toList());
//        long total = searchHits.ge tTotalHits();//总条数
//        double v = Long.valueOf(total).doubleValue();
//        double ceil = Math.ceil(v);
//        int i = Double.valueOf(ceil).intValue();
        //返回总条数  总页数 / 10 向上取整得到总页数
        Long total = Long.valueOf(searchHits.getTotalHits()).longValue();
        Long totalPage = Double.valueOf(Math.ceil(Long.valueOf(searchHits.getTotalHits()).doubleValue() / 10)).longValue();


        //调用category的idList查询方法
        //List<CategoryEntity> categoryList = this.getCategoryList(aggregations);
        Aggregations aggregations = searchHits.getAggregations();
        //cid查询分类 / brand
        Map<Integer, List<CategoryEntity>> map = this.getCategoryList(aggregations);
        //热度最高的分类id
        Integer hotCid = 0;
        //热度最高的id代表的数据
        List<CategoryEntity> categoryList = null;

        for (Map.Entry<Integer, List<CategoryEntity>> mapEntry : map.entrySet()){
            hotCid = mapEntry.getKey();
            categoryList = mapEntry.getValue();
        }

        //通过cid去查询规格参数
        Map<String, List<String>> stringListHashMap = this.getspecParam(hotCid, searchDTO.getSearch());

        //调用brand的idList查询方法
        List<BrandEntity> brandList = this.getBrandList(aggregations);
        return new GoodsResponse(total, totalPage, brandList, categoryList,goodsList,stringListHashMap);
    }

    @Override
    public Result<JSONObject> deleteGoodsBySpuId(String spuId) {
        elasticsearchRestTemplate.delete(spuId,GoodsDoc.class);
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> saveElasticSearchTemplate(Integer id) {
        List<GoodsDoc> goodsDoc = this.esGoodsInfo(id);
        elasticsearchRestTemplate.save(goodsDoc.get(0));
        return this.setResultSuccess();
    }

    //param规格参数查询
    private Map<String, List<String>> getspecParam(Integer hotCid,String search){

        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(hotCid);
        specParamDTO.setSearching(true);//只搜索有查询属性的数据
        //参数查询
        Result<List<SpecParamEntity>> specParamResult = specParamFeign.getSpecParamInfo(specParamDTO);

        if (specParamResult.getCode() == 200) {
            List<SpecParamEntity> specParamList  = specParamResult.getData();
            //聚合查询
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            queryBuilder.withQuery(QueryBuilders.multiMatchQuery(search,"brandName","categoryName","title"));


            //分页,必须查询一条数据
            queryBuilder.withPageable(PageRequest.of(0,1));

            specParamList.stream().forEach(specParam ->{
                queryBuilder.addAggregation(AggregationBuilders.terms(specParam.getName()).field("specs." + specParam.getName() + ".keyword"));
            });

            SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsDoc.class);

            //
            Map<String, List<String>> map = new HashMap<>();
            Aggregations aggregations = searchHits.getAggregations();
            specParamList.stream().forEach(specParam ->{
                Terms terms = aggregations.get(specParam.getName());
                List<? extends Terms.Bucket> buckets = terms.getBuckets();
                List<String> valueList = buckets.stream().map(bucket -> {
                    return bucket.getKeyAsString();
                }).collect(Collectors.toList());
                map.put(specParam.getName(),valueList);
            });
            return map;
        }
        return null;
    }

    //构建查询条件
    private NativeSearchQueryBuilder getQueryBuilder(String search, Integer page ,String filter){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //多条件同时查询
        queryBuilder.withQuery(QueryBuilders.multiMatchQuery(search,"title","brandName","categoryName"));

        if(StringUtil.isNotEmpty(filter) && filter.length() > 2){
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            Map<String, String> stringStringMap = JSONUtil.toMapValueString(filter);

            for (Map.Entry<String,String> item : stringStringMap.entrySet()){
                MatchQueryBuilder matchQueryBuilder = null;

                //分类 品牌和 规格参数的查询方式不一样
                if (item.getKey().equals("cid3") || item.getKey().equals("brandId")){
                    matchQueryBuilder = QueryBuilders.matchQuery(item.getKey(),item.getValue());
                }else{
                    matchQueryBuilder = QueryBuilders.matchQuery("specs." + item.getKey() + ".keyword",item.getValue());
                }
                boolQueryBuilder.must(matchQueryBuilder);
            }
            //添加过滤,过滤不会影响评分
            queryBuilder.withFilter(boolQueryBuilder);
        }

        //分页
        if(ObjectUtil.isNotNull(page)) queryBuilder.withPageable(PageRequest.of(page-1,10));

        //设置查询出来的内容,页面上做多只需要id,title,skus
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","title","skus"},null));

        //构建高亮条件
        queryBuilder.withHighlightBuilder(ESHighLightUtil.getHighlightBuilder("title"));

        queryBuilder.addAggregation(AggregationBuilders.terms("cid_agg").field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms("brand_agg").field("brandId"));

        //设置不返回多余的信息
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","title","skus"}, null));
        return queryBuilder;
    }

    //获取品牌id集合
    private List<BrandEntity> getBrandList(Aggregations aggregations){
        Terms brand_agg = aggregations.get("brand_agg");
        List<String> brandStrList = brand_agg.getBuckets().stream().map(brandId -> brandId.getKeyAsString()).collect(Collectors.toList());
        String brandIdStr = String.join(",", brandStrList);
        Result<List<BrandEntity>> brandByIdList = brandFeign.getBrandByIdList(brandIdStr);
        return brandByIdList.getData();
    }

    //获取分类id集合
    private Map<Integer, List<CategoryEntity>> getCategoryList(Aggregations aggregations){
        Terms cid_agg = aggregations.get("cid_agg");
        List<? extends Terms.Bucket> cidBuckets = cid_agg.getBuckets();

        List<Integer> hotCidArr = Arrays.asList(0);//热度最高的分类id
        List<Long> maxCount = Arrays.asList(0L);

        Map<Integer, List<CategoryEntity>> map = new HashMap<>();

        List<String> cidStrList = cidBuckets.stream().map(cidbucket -> {
            Number keyAsNumber = cidbucket.getKeyAsNumber();

            if(cidbucket.getDocCount() > maxCount.get(0)){
                maxCount.set(0,cidbucket.getDocCount());
                hotCidArr.set(0,keyAsNumber.intValue());
            }
            return keyAsNumber.intValue() + "";
        }).collect(Collectors.toList());

        String cidsStr = String.join(",", cidStrList);
        Result<List<CategoryEntity>> caterogyResult = categoryFeign.getCategoryByIdList(cidsStr);
        map.put(hotCidArr.get(0),caterogyResult.getData());//key为热度最高的cid value为cid集合对应的数据
        return map;
    }
    @Override
    public Result<JSONObject> clearGoodsEsDatas() {
        //创建索引和映射
        IndexOperations index = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if(index.exists()){
            index.delete();
            log.info("索引删除成功");
        }
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> initGoodsEsData() {
        //创建索引和映射
        IndexOperations index = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if(!index.exists()){
            index.create();
            index.createMapping();
            log.info("索引创建成功");
        }
        SpuDTO spuDTO = new SpuDTO();
        //查询数据
        List<GoodsDoc> goodsDocs = this.esGoodsInfo(spuDTO.getId());
        if(!goodsDocs.isEmpty()) elasticsearchRestTemplate.save(goodsDocs);
        System.out.println(goodsDocs);
        return this.setResultSuccess();
    }

    //查询Goods数据并赋值给新的GoodsFoc对象
    //查询全部数据并赋值
    private List<GoodsDoc> esGoodsInfo(Integer spuId) {
        SpuDTO spuDTO = new SpuDTO();
        if (spuId != null && spuId != 0 ){
            spuDTO.setId(spuId);
        }
        Result<List<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(spuDTO);
        if(spuInfo.getCode() == HTTPStatus.OK){
            List<GoodsDoc> goodsDocs = spuInfo.getData().stream().map(spu -> this.queryGoodsDoc(spu)).collect(Collectors.toList());
            return goodsDocs;
        }
        return null;
    }
    //拆模块
    private GoodsDoc queryGoodsDoc(SpuDTO spu){
        //查询出来的数据是多个spu
        GoodsDoc goodsDoc = new GoodsDoc();
        //spu数据填充
        goodsDoc.setId(spu.getId().longValue());
        goodsDoc.setBrandId(spu.getBrandId().longValue());
        goodsDoc.setCid1(spu.getCid1().longValue());
        goodsDoc.setCid2(spu.getCid2().longValue());
        goodsDoc.setCid3(spu.getCid3().longValue());
        goodsDoc.setCreateTime(spu.getCreateTime());
        goodsDoc.setSubTitle(spu.getSubTitle());
        //可被查询的数据
        goodsDoc.setTitle(spu.getTitle());
        goodsDoc.setBrandName(spu.getBrandName());
        goodsDoc.setCategoryName(spu.getCategoryName());
        //通过spuID查询skuList
        Map<List<Long>, List<Map<String, Object>>> skus = this.getSkusAndPriceList(spu.getId());
        //取数据为goodsDoc赋值
        skus.forEach((key, value) -> {
            goodsDoc.setPrice(key);
            goodsDoc.setSkus(JSONUtil.toJsonString(value));
        });
        //通过cid3查询规格参数
        Map<String, Object> specMap = this.getSpecMap(spu);

        goodsDoc.setSpecs(specMap);
        return  goodsDoc;
    }
    //通过cid3查询规格参数 (摘出)
    private Map<String, Object> getSpecMap(SpuDTO spuDTO){
        Map<String, Object> map = new HashMap<>();

        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(spuDTO.getCid3());

        Result<List<SpecParamEntity>> specParamResult = specParamFeign.getSpecParamInfo(specParamDTO);

        if(specParamResult.getCode() == HTTPStatus.OK){
            //只有规格参数的id和规格参数的名字
            List<SpecParamEntity> paramList = specParamResult.getData();

            //通过spuid去查询spuDetail,detail里面有通用和特殊规格参数的值
            Result<SpuDetailEntity> spuDetailResult = goodsFeign.getDetailBySpuId(spuDTO.getId());

            if(spuDetailResult.getCode() == HTTPStatus.OK){
                SpuDetailEntity spuDetailEntity = spuDetailResult.getData();

                //通用规格参数的值
                String genericSpec = spuDetailEntity.getGenericSpec();
                Map<String, String> stringStringMap = JSONUtil.toMapValueString(genericSpec);

                //特有规格参数的值
                String specialSpec = spuDetailEntity.getSpecialSpec();
                Map<String, List<String>> stringListMap = JSONUtil.toMapValueStrList(specialSpec);

                paramList.stream().forEach(param ->{

                    if(param.getGeneric()){
                        if(param.getNumeric() && param.getSearching()){
                            map.put(param.getName(),this.chooseSegment(stringStringMap.get(param.getId() + ""),param.getSegments(),param.getUnit()));
                        }else{
                            map.put(param.getName(),stringStringMap.get(param.getId() + "" ));
                        }
                    }else{
                        map.put(param.getName(),stringListMap.get(param.getId() + ""));
                    }
                });
            }
        }
        return map;
    }
    //通过spuId查询sku(摘出)
    private Map<List<Long>, List<Map<String, Object>>> getSkusAndPriceList(Integer spuId){
        Map<List<Long>, List<Map<String, Object>>> map = new HashMap<>();

        Result<List<SkuDTO>> skuList = goodsFeign.getSkuBySpuId(spuId);
        List<Long> priceList = new ArrayList<>();
        List<Map<String, Object>> skuMap = null;

        if(skuList.getCode() == HTTPStatus.OK){

            skuMap = skuList.getData().stream().map(sku -> {
                Map<String, Object> map1 = new HashMap<>();
                map1.put("id", sku.getId());
                map1.put("title", sku.getTitle());
                map1.put("images", sku.getImages());
                map1.put("price", sku.getPrice());
                priceList.add(sku.getPrice().longValue());
                return map1;
            }).collect(Collectors.toList());
        }
        map.put(priceList,skuMap);
        return map;
    }

    /**
     * 把具体的值转换成区间-->不做范围查询
     * @param value
     * @param segments
     * @param unit
     * @return
     */
    private String chooseSegment(String value, String segments, String unit) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : segments.split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + unit + "以上";
                }else if(begin == 0){
                    result = segs[1] + unit + "以下";
                }else{
                    result = segment + unit;
                }
                break;
            }
        }
        return result;
    }
}
