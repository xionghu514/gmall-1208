package com.atguigu.gmall.index.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/16 12:59
 * @Email: 1796235969@qq.com
 */
@Controller
public class IndexController {
    @GetMapping("/**")
    public String index() {

        return "index";
    }
}
