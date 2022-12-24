package com.atguigu.gmall.item.service;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/22 22:26
 * @Email: 1796235969@qq.com
 */
@Service
public class ItemService {
    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private TemplateEngine templateEngine;

    public ItemVo loadData(Long skuId) {
        ItemVo itemVo = new ItemVo();

//        1.根据skuId查询sku
        CompletableFuture<SkuEntity> skuFuture = CompletableFuture.supplyAsync(() -> {
            SkuEntity skuEntity = pmsClient.querySkuById(skuId).getData();

            if (skuEntity == null) {
                throw new RuntimeException("您访问的商品不存在");
            }
            itemVo.setSkuId(skuId);
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setSubtitle(skuEntity.getSubtitle());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setWeight(skuEntity.getWeight());
            itemVo.setDefaultImage(skuEntity.getDefaultImage());
            return skuEntity;
        }, executorService);


//        2.根据三级分类的id查询一二三级分类
        CompletableFuture<Void> cateFuture = skuFuture.thenAcceptAsync((skuEntity -> {
            List<CategoryEntity> categoryEntities = pmsClient.queryLvl123CategoriesByCid3(skuEntity.getCategoryId()).getData();

            itemVo.setCategories(categoryEntities);
        }), executorService);


//        3.根据品牌id查询品牌
        CompletableFuture<Void> brandFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            BrandEntity brandEntity = pmsClient.queryBrandById(skuEntity.getBrandId()).getData();

            if (brandEntity != null) {
                itemVo.setBrandId(skuEntity.getBrandId());
                itemVo.setBrandName(brandEntity.getName());
            }
        }, executorService);


//        4.根据spuId查询spu
        CompletableFuture<Void> spuFuture = skuFuture.thenAcceptAsync((skuEntity -> {
            Long spuId = skuEntity.getSpuId();
            SpuEntity spuEntity = pmsClient.querySpuById(spuId).getData();

            if (spuEntity != null) {
                itemVo.setSpuId(spuId);
                itemVo.setSpuName(spuEntity.getName());
            }
        }), executorService);


//        5.根据skuId查询sku的图片列表
        CompletableFuture<Void> skuImageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> skuImagesEntities = pmsClient.querySkuImagesBySkuId(skuId).getData();

            if (CollectionUtils.isNotEmpty(skuImagesEntities)) {
                itemVo.setImage(skuImagesEntities);
            }
        }, executorService);


//        6.根据skuId查询营销信息
        CompletableFuture<Void> salesFuture = CompletableFuture.runAsync(() -> {
            List<ItemSaleVo> saleVos = smsClient.querySalesBySkuId(skuId).getData();

            if (CollectionUtils.isNotEmpty(saleVos)) {
                itemVo.setSales(saleVos);
            }
        }, executorService);


//        7.根据skuId查询库存
        CompletableFuture<Void> wareFuture = CompletableFuture.runAsync(() -> {
            List<WareSkuEntity> wareSkuEntities = wmsClient.queryWareSkuEntities(skuId).getData();

            if (CollectionUtils.isNotEmpty(wareSkuEntities)) {
                itemVo.setStore(
                        wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0)
                );
            }
        }, executorService);


//        8.根据spuId查询spu下所有sku的销售属性列表
        CompletableFuture<Void> saleAttrsFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            List<SaleAttrValueVo> saleAttrValueVos = pmsClient.querySaleAttrValuesBySpuId(skuEntity.getSpuId()).getData();

            if (CollectionUtils.isNotEmpty(saleAttrValueVos)) {
                itemVo.setSaleAttrs(saleAttrValueVos);
            }
        }, executorService);


//        9.根据skuId查询当前sku的销售属性
        CompletableFuture<Void> saleAttrFuture = CompletableFuture.runAsync(() -> {
            List<SkuAttrValueEntity> skuAttrValueEntities = pmsClient.querySaleAttrValueBySkuId(skuId).getData();
            // 当前 sku 的销售属性: {3: 白天白, 4: 12G, 5: 256G}
            if (CollectionUtils.isNotEmpty(skuAttrValueEntities)) {
                Map<Long, String> skuAttrValueVo = skuAttrValueEntities.stream().collect(Collectors.toMap(map -> map.getAttrId(), map -> map.getAttrValue()));

                itemVo.setSaleAttr(skuAttrValueVo);
            }
        }, executorService);


//        10.根据spuId查询spu下所有销售属性组合与skuId的映射关系
        CompletableFuture<Void> mappingFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            String mapping = pmsClient.queryMappingBySpuId(skuEntity.getSpuId()).getData();
            if (StringUtils.isNotEmpty(mapping)) {
                itemVo.setSkuJsons(mapping);
            }
        }, executorService);


//        11.根据spuId查询spu的描述信息
        CompletableFuture<Void> spuDescFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            SpuDescEntity spuDescEntity = pmsClient.querySpuDescById(skuEntity.getSpuId()).getData();
            if (spuDescEntity != null) {
                String[] spuDescs = StringUtils.split(spuDescEntity.getDecript(), ",");
                List<String> spuImages = Arrays.asList(spuDescs);

                itemVo.setSpuImages(spuImages);
            }
        }, executorService);


//        12.查询规格参数分组及组下的规格参数和值
        CompletableFuture<Void> groupFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            List<ItemGroupVo> itemGroupVos = pmsClient.queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(skuEntity.getCategoryId(), skuEntity.getSpuId(), skuId).getData();
            if (CollectionUtils.isNotEmpty(itemGroupVos)) {
                itemVo.setGroups(itemGroupVos);
            }
        }, executorService);

        CompletableFuture.allOf(groupFuture, spuDescFuture, mappingFuture, saleAttrFuture, saleAttrsFuture, wareFuture, salesFuture,
                skuImageFuture, brandFuture, cateFuture, spuFuture).join();

        // 异步执行页面静态化
        executorService.execute(() -> {
            generateHtml(itemVo);
        });

        return itemVo;
    }

    public void generateHtml(ItemVo itemVo) {
        try(PrintWriter printWriter = new PrintWriter("E:\\learn\\gmall-1208\\html\\" + itemVo.getSkuId() + ".html")) {
            // 初始化一个上下文对象
            Context context = new Context();
            // 给模板传递动态数据
            context.setVariable("itemVo", itemVo);

            // 页面静态化方法： 1.模板名称 2.上下文对象 3.输出流
            templateEngine.process("item", context, printWriter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}
