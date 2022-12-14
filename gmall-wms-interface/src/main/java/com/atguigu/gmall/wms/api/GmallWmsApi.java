package com.atguigu.gmall.wms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/14 11:47
 * @Email: 1796235969@qq.com
 */
public interface GmallWmsApi {
    @GetMapping("/sku/{skuId}")
    @ApiOperation("根据 skuId 查询 库存")
    public ResponseVo<List<WareSkuEntity>> queryWareSkuEntities(@PathVariable("skuId") Long skuId);
}
