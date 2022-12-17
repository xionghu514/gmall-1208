package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
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

    @GetMapping("pms/sku/spu/{spuId}")
    @ApiOperation("根据 spuId 查询 sku")
    public ResponseVo<List<SkuEntity>> querySkusBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/brand/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    @GetMapping("pms/category/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    @GetMapping("pms/category/parent/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryCategoryByPid(@PathVariable("parentId") Long pid);


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
}
