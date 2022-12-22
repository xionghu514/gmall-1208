package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/22 13:02
 * @Email: 1796235969@qq.com
 */
@Data
public class ItemVo {
    // sku 基本信息
    private Long skuId;
    private String title;
    private String subtitle;
    private String defaultImage;
    private BigDecimal price;
    private Integer weight;

    // 面包屑所需字段
    private List<CategoryEntity> categories;

    // 品牌所需字段
    private Long brandId;
    private String brandName;

    // sku图片信息
    private List<SkuImagesEntity> image;

    // 销售属性
    private List<ItemSaleVo> sales;

    // 是否有货
    private Boolean stock;

    // [
    //  {attrId:3, attrName:机身颜色, attrValues:["白天白","暗夜黑"]}，
    //  {attrId:5, attrName:机身存储, attrValues:["256","512"]}，
    // ]
    private List<SaleAttrValueVo> saleAttrs;

    // 当前销售属性{3: 白天白, 4: 8G, 5: 256G}
    private Map<Long, String> saleAttr;

    // 销售属性与skuId的映射关系{'白天白，8G，256G': 10,'暗夜黑，16G，256G': 11,'白天白，8G，128G': 12}
    private String skuJsons;

    private List<ItemGroupVo> groups;

}
