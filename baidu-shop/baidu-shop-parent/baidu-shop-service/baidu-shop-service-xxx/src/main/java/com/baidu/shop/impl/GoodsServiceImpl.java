package com.baidu.shop.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.componen.MrRabbitMQ;
import com.baidu.shop.constant.MqMessageConstant;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.*;
import com.baidu.shop.feign.SearchFeign;
import com.baidu.shop.feign.SpuFeign;
import com.baidu.shop.global.ZiDingYi;
import com.baidu.shop.mapper.*;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.service.GoodsService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName GoodsServiceImpl
 * @Description: TODO
 * @Author fuwei
 * @Date 2020/9/7
 * @Version V1.0
 **/
@RestController
public class GoodsServiceImpl extends BaseApiService implements GoodsService {

    @Resource
    private SpuMapper spuMapper;
    @Resource
    private BrandService brandService;
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private SkuMapper skuMapper;
    @Resource
    private StockMapper stockMapper;
    @Resource
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private SpuFeign spuFeign;
    @Autowired
    private SearchFeign searchFeign;
    @Autowired
    private MrRabbitMQ mrRabbitMQ;

    @Override
    public Result<List<SpuDTO>> getSpuInfo(SpuDTO spuDTO) {
        //分页
        if (ObjectUtil.isNotNull(spuDTO.getPage()) && ObjectUtil.isNotNull(spuDTO.getRows()))
            PageHelper.startPage(spuDTO.getPage(),spuDTO.getRows());
        /*//构建条件查询
        Example example = new Example(SpuEntity.class);
        //构建查询条件
        Example.Criteria criteria = example.createCriteria();
        //排序
        if(ObjectUtil.isNotNull(spuDTO.getSort())) example.setOrderByClause(spuDTO.getOrderByClauser());
        //上架下架查询
        if(ObjectUtil.isNotNull(spuDTO.getSaleable()) && spuDTO.getSaleable() != 2)
            criteria.andEqualTo("saleable", spuDTO.getSaleable());
        //title
        if(StringUtil.isNotEmpty(spuDTO.getTitle()))
            criteria.andLike("title","%" + spuDTO.getTitle() + "%");
        List<SpuEntity> list = spuMapper.selectByExample(example);
        //摘出
        List<SpuDTO> dtoList = this.queryDto(list);

        PageInfo<SpuEntity> info = new PageInfo<>(list);
        this.setResult(HTTPStatus.OK,info.getTotal() + "",dtoList);*/
        List<SpuDTO> list = spuMapper.querySpuDetail(spuDTO);

        return this.setResultSuccess(list);
    }

    @Transactional
    @Override
    public Result<JSONObject> postSpu(SpuDTO spuDTO) {
        Date date = new Date();
        //spu
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setCreateTime(date);
        spuEntity.setLastUpdateTime(date);
        spuEntity.setSaleable(1);
        spuEntity.setValid(1);
        spuMapper.insertSelective(spuEntity);
        //spuDetail
        SpuDetailEntity spuDetailEntity = BaiduBeanUtil.copyProperties(spuDTO.getSpuDetail(), SpuDetailEntity.class);
        spuDetailEntity.setSpuId(spuEntity.getId());
        spuDetailMapper.insertSelective(spuDetailEntity);

        this.saveSkuAndStock(spuDTO.getSkus(),spuEntity.getId(),date);

        //事务同步管理器
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                mrRabbitMQ.send(spuEntity.getId() + "", MqMessageConstant.SPU_ROUT_KEY_SAVE);
//                spuFeign.createStaticHTMLTemplate(spuEntity.getId());
//                searchFeign.saveElasticSearchTemplate(spuEntity.getId());
            }
        });
        return this.setResultSuccess();
    }

    @Override
    public Result<SpuDetailEntity> getDetailBySpuId(Integer spuId) {
        //修改回显数据,通过spuId查询
        SpuDetailEntity spuDetailEntity = spuDetailMapper.selectByPrimaryKey(spuId);
        return this.setResultSuccess(spuDetailEntity);
    }

    @Override
    public Result<List<SkuDTO>> getSkuBySpuId(Integer spuId) {
        //修改回显数据,查询sku集合
        List<SkuDTO> list = skuMapper.selectSkuAndStockBySpuId(spuId);
        return this.setResultSuccess(list);
    }

    @Transactional
    @Override
    public Result<JSONObject> editSpu(SpuDTO spuDTO) {
        System.out.println(spuDTO);
        //修改spu
        Date date = new Date();
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setLastUpdateTime(date);
        spuMapper.updateByPrimaryKeySelective(spuEntity);
        //修改spuDetail
        spuDetailMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(spuDTO.getSpuDetail(), SpuDetailEntity.class));

        //修改sku
        Integer spuId = spuDTO.getId();

        List<Long> skuIdArr = this.querySpuId(spuId);

        if(skuIdArr.size() == 0) throw new ZiDingYi(500,"error");
        skuMapper.deleteByIdList(skuIdArr);
        stockMapper.deleteByIdList(skuIdArr);
        this.saveSkuAndStock(spuDTO.getSkus(),spuEntity.getId(),date);

        //事务同步处理器
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                mrRabbitMQ.send(spuEntity.getId() + "", MqMessageConstant.SPU_ROUT_KEY_UPDATE);
//                searchFeign.deleteGoodsBySpuId(spuId.toString());
//                File file = new File("E:\\static-html\\web\\"+spuId+".html");
//                if(file.exists()){
//                    file.delete();
//                }
//                spuFeign.createStaticHTMLTemplate(spuEntity.getId());
//                searchFeign.saveElasticSearchTemplate(spuEntity.getId());
            }
        });

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> delSpu(Integer spuId) {

        spuMapper.deleteByPrimaryKey(spuId);
        spuDetailMapper.deleteByPrimaryKey(spuId);

        List<Long> skuIdArr = this.querySpuId(spuId);
        if(skuIdArr.size() > 0) {
            //删除skus
            skuMapper.deleteByIdList(skuIdArr);
            //删除stock,与修改时的逻辑一样,先查询出所有将要修改skuId然后批量删除
            stockMapper.deleteByIdList(skuIdArr);
        }

        //事务同步管理器
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                mrRabbitMQ.send(spuId + "", MqMessageConstant.SPU_ROUT_KEY_DELETE);
//                searchFeign.deleteGoodsBySpuId(spuId.toString());
//                File file = new File("E:\\static-html\\web\\"+spuId+".html");
//                if(file.exists()){
//                    file.delete();
//                }
            }
        });

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> xiajia(SpuDTO spuDTO) {
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        if(spuEntity.getSaleable() == 1){
            spuEntity.setSaleable(0);
        }else {
            spuEntity.setSaleable(1);
        }
        spuMapper.updateByPrimaryKeySelective(spuEntity);
        return this.setResultSuccess();
    }

    private List<Long> querySpuId(Integer spuId){
        Example example = new Example(SkuEntity.class);
        example.createCriteria().andEqualTo("spuId",spuId);
        List<SkuEntity> skuEntities = skuMapper.selectByExample(example);
        return  skuEntities.stream().map(sku -> sku.getId()).collect(Collectors.toList());
    }

    //新增修改方法 sku stock 公共code
    private void saveSkuAndStock(List<SkuDTO> skus,Integer spuId,Date date){
        //sku
        skus.stream().forEach(skuDTO -> {
            SkuEntity skuEntity = BaiduBeanUtil.copyProperties(skuDTO, SkuEntity.class);
            skuEntity.setSpuId(spuId);
            skuEntity.setCreateTime(date);
            skuEntity.setLastUpdateTime(date);
            skuMapper.insertSelective(skuEntity);
            //stock
            StockEntity stockEntity = new StockEntity();
            stockEntity.setSkuId(skuEntity.getId());
            stockEntity.setStock(skuDTO.getStock());
            stockMapper.insertSelective(stockEntity);
        });
    }
    //商品列表展示链表数据方法(摘出)
    private List<SpuDTO> queryDto(List<SpuEntity> list){
        //商品品牌
        List<SpuDTO> dtoList = list.stream().map(spuEntity -> {
            SpuDTO spuDTO1 = BaiduBeanUtil.copyProperties(spuEntity, SpuDTO.class);
            //品牌名称
            BrandDTO brandDTO = new BrandDTO();
            brandDTO.setId(spuEntity.getBrandId());
            Result<PageInfo<BrandEntity>> brandInfo = brandService.getBrandInfo(brandDTO);
            if (ObjectUtil.isNotNull(brandInfo)) {
                PageInfo<BrandEntity> data = brandInfo.getData();
                List<BrandEntity> list1 = data.getList();
                if (!list1.isEmpty())
                    spuDTO1.setBrandName(list1.get(0).getName());
            }
            //分类名称
//            List<CategoryEntity> categoryList = categoryMapper.selectByIdList(Arrays.asList(spuDTO1.getCid1(), spuDTO1.getCid2(), spuDTO1.getCid3()));
//            String collect = categoryList.stream().map(catrgoty -> catrgoty.getName()).collect(Collectors.joining("/"));
            //优化
            String categoryName = categoryMapper.selectByIdList(Arrays.asList(spuDTO1.getCid1(), spuDTO1.getCid2(), spuDTO1.getCid3()))
                    .stream().map(catrgoty -> catrgoty.getName()).collect(Collectors.joining("/"));
            spuDTO1.setCategoryName(categoryName);
            return spuDTO1;
        }).collect(Collectors.toList());

        return dtoList;
    }
}
