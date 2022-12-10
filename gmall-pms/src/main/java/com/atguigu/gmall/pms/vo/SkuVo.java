package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/10/29 13:26
 * @Email: 1796235969@qq.com
 */
@Data
public class SkuVo extends SkuEntity {
    // sku 的 图片列表
    private List<String> images;

    // 销售属性
    private List<SkuAttrValueEntity> saleAttrs;

    /*                       积分优惠信息                       */

    /**
     * 成长积分
     */
    private BigDecimal growBounds;
    /**
     * 购物积分
     */
    private BigDecimal buyBounds;
    /**
     * 优惠生效情况[1111（四个状态位，从右到左）;0 - 无优惠，成长积分是否赠送;1 - 无优惠，购物积分是否赠送;2 - 有优惠，成长积分是否赠送;3 - 有优惠，购物积分是否赠送【状态位0：不赠送，1：赠送】]
     */
//    private Integer work;
    private List<Integer> work;


    /*                       满减优惠信息                       */

    /**
     * 满多少
     */
    private BigDecimal fullPrice;
    /**
     * 减多少
     */
    private BigDecimal reducePrice;
    /**
     * 是否参与其他优惠
     */
//    private Integer addOther;
    private Integer fullAddOther;


    /*                       打折优惠信息                       */

    /**
     * 满几件
     */
    private Integer fullCount;
    /**
     * 打几折
     */
    private BigDecimal discount;
    /**
     * 是否叠加其他优惠[0-不可叠加，1-可叠加]
     */
//    private Integer addOther;
    private Integer ladderAddOther;




}