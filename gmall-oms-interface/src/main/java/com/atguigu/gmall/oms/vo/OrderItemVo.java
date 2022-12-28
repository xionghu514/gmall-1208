package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/27 19:49
 * @Email: 1796235969@qq.com
 */
@Data
public class OrderItemVo {

    private Long skuId;
    private String defaultImage;
    private String title;
    private List<SkuAttrValueEntity> saleAttrs; // 销售属性
    private BigDecimal price; // 实时价格
    private BigDecimal count;
    private Boolean store = false; // 是否有货
    private List<ItemSaleVo> sales; // 营销信息:

    private Integer weight;
}
