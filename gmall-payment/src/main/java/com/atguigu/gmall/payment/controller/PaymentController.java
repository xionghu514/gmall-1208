package com.atguigu.gmall.payment.controller;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.payment.interceptor.LoginInterceptor;
import com.atguigu.gmall.payment.service.PaymentService;
import com.atguigu.gmall.payment.vo.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/30 12:03
 * @Email: 1796235969@qq.com
 */
@Controller
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @GetMapping("pay.html")
    public String pay(@RequestParam("orderToken") String orderToken, Model model) {
        OrderEntity orderEntity = paymentService.queryOrderByToken(orderToken);

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();

        if (orderEntity == null) {
            throw new RuntimeException("您的订单不存在!");
        }

        if (userId != orderEntity.getUserId()) {
            throw new RuntimeException("该订单不属于您!");
        }

        if (orderEntity.getStatus() != 0) {
            throw new RuntimeException("该订单不能支付");
        }

        model.addAttribute("orderEntity", orderEntity);
        return "pay";
    }
}
