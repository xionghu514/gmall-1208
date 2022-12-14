package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

    @GetMapping("/spu/{spuId}")
    @ApiOperation("根据 spuId 查询 sku")
    public ResponseVo<List<SkuEntity>> querySkusBySpuId(@PathVariable("spuId") Long spuId);


    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    @GetMapping("spu/{spuId}")
    @ApiOperation("根据spuId查询基本属性")
    public ResponseVo<List<SpuAttrValueEntity>> querySpuAttrValuesBySpuId(@PathVariable("spuId") Long spuId);
}
