package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.order.pojo.OrderConfirmVo;
import com.atguigu.gmall.order.pojo.OrderSubmitVo;
import com.atguigu.gmall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/27 21:43
 * @Email: 1796235969@qq.com
 */
@Controller
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("submit")
    @ResponseBody
    public ResponseVo<String> submit(@RequestBody OrderSubmitVo submitVo) {
        orderService.submit(submitVo);

        return ResponseVo.ok(submitVo.getOrderToken());
    }

    @GetMapping("confirm")
    public String confirm(Model model) {
        OrderConfirmVo confirmVo = orderService.confirm();

        model.addAttribute("confirmVo", confirmVo);
        return "trade";
    }
}
