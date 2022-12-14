package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/14 10:15
 * @Email: 1796235969@qq.com
 */
public interface GmallPmsApi {
    @PostMapping("pms/spu/json")
    @ApiOperation("分页查询")
    public ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo paramVo);

    @GetMapping("pms/spu/{id}")
    @ApiOperation("spu详情查询")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);


    @GetMapping("pms/sku/{id}")
    @ApiOperation("sku详情查询")
    public ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);

    @GetMapping("pms/sku/spu/{spuId}")
    @ApiOperation("根据 spuId 查询 sku")
    public ResponseVo<List<SkuEntity>> querySkusBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/spudesc/{spuId}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/brand/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);


    @GetMapping("pms/category/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    @GetMapping("pms/category/lvl123/{cid3}")
    public ResponseVo<List<CategoryEntity>> queryLvl123CategoriesByCid3(@PathVariable("cid3") Long cid3);

    @GetMapping("pms/category/parent/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryCategoryByPid(@PathVariable("parentId") Long pid);

    @GetMapping("pms/category/level23/{pid}")
    public ResponseVo<List<CategoryEntity>> queryLevel23CategoriesByPid(@PathVariable("pid") Long pid);

    @GetMapping("pms/skuimages/sku/{skuId}")
    @ApiOperation("根据skuId查询sku的图片列表")
    public ResponseVo<List<SkuImagesEntity>> querySkuImagesBySkuId(@PathVariable("skuId") Long skuId);


    @GetMapping("pms/spuattrvalue/search/attr/value/{cid}")
    @ApiOperation("根据分类Id和spuId查询基础属性集合")
    public ResponseVo<List<SpuAttrValueEntity>> querySpuAttrValueEntitiesBySpuIdAndCid(
            @PathVariable("cid") Long cid,
            @RequestParam("spuId") Long spuId
    );


    @GetMapping("pms/skuattrvalue/search/attr/value/{cid}")
    @ApiOperation("根据分类Id和skuId查询销售属性集合")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValueEntitiesBySkuIdAndCid(
            @PathVariable("cid") Long cid,
            @RequestParam("skuId") Long skuId
    );

    @GetMapping("pms/skuattrvalue/spu/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> querySaleAttrValuesBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/skuattrvalue/sku/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySaleAttrValueBySkuId(@PathVariable("skuId") Long skuId);

    @GetMapping("pms/skuattrvalue/mapping/{spuId}")
    public ResponseVo<String> queryMappingBySpuId(@PathVariable("spuId") Long spuId);


    @GetMapping("pms/attrgroup/with/attr/value/{cid}")
    public ResponseVo<List<ItemGroupVo>> queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(@PathVariable("cid") Long cid, @RequestParam("spuId") Long spuId, @RequestParam("skuId") Long skuId);
}
