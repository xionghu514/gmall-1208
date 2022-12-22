package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * sku销售属性&值
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2022-12-09 09:47:54
 */
public interface SkuAttrValueService extends IService<SkuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SkuAttrValueEntity> querySkuAttrValueEntitiesBySkuIdAndCid(Long cid, Long skuId);

    List<SaleAttrValueVo> querySaleAttrValuesBySpuId(Long spuId);
}

