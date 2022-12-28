package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/28 13:25
 * @Email: 1796235969@qq.com
 */
@Data
public class OrderSubmitVo {
    private UserAddressEntity address;

    private Integer bounds; // 购物积分

    private String deliveryCompany; // 快递公司

    private List<OrderItemVo> items; // 送货清单

    private String orderToken; // 防重

    private Integer payType; // 支付方式

    private BigDecimal totalPrice; // 验实时总价


}
