package com.atguigu.gmall.cart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/25 14:12
 * @Email: 1796235969@qq.com
 */
@Controller
public class TestController {

    @GetMapping("test")
    @ResponseBody
    public String test() {

        System.out.println("这是controller方法");

        return "hello cart";
    }
}
