package com.atguigu.gmall.wms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/14 11:47
 * @Email: 1796235969@qq.com
 */
public interface GmallWmsApi {
    @GetMapping("wms/waresku/sku/{skuId}")
    @ApiOperation("根据 skuId 查询 库存")
    public ResponseVo<List<WareSkuEntity>> queryWareSkuEntities(@PathVariable("skuId") Long skuId);

    @PostMapping("wms/waresku/check/lock/{orderToken}")
    public ResponseVo<List<SkuLockVo>> checkLock(@RequestBody List<SkuLockVo> skuLockVos, @PathVariable("orderToken") String orderToken);
}
