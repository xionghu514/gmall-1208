package com.atguigu.gmall.order.pojo;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/27 19:44
 * @Email: 1796235969@qq.com
 */
@Data
public class OrderConfirmVo {

    private List<UserAddressEntity> addresses; //收货地址列表

    private List<OrderItemVo> items; // 送货清单

    private Integer bounds; // 购物积分

    private String orderToken; // 防重
}
