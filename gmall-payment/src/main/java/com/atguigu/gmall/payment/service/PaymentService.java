package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.payment.feign.GmallOmsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/30 12:14
 * @Email: 1796235969@qq.com
 */
@Service
public class PaymentService {
    @Autowired
    private GmallOmsClient omsClient;

    public OrderEntity queryOrderByToken(String orderToken) {
        OrderEntity orderentity = omsClient.queryOrderByToken(orderToken).getData();
        return orderentity;
    }
}
