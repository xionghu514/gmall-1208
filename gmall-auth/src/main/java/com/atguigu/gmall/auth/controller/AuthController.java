package com.atguigu.gmall.auth.controller;

import com.atguigu.gmall.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/24 16:35
 * @Email: 1796235969@qq.com
 */
@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("toLogin.html")
    public String toLogin(@RequestParam(value = "returnUrl", defaultValue = "http://gmall.com") String returnUrl, Model model) {
        model.addAttribute("returnUrl", returnUrl);

        return "login";
    }

    @PostMapping("login")
    public String login(
            @RequestParam(value = "returnUrl", defaultValue = "http://gmall.com") String returnUrl,
            @RequestParam("loginName") String loginName,
            @RequestParam("password") String password,
            HttpServletResponse response, HttpServletRequest request
                        ) {
        authService.login(loginName,password, response, request);

        return "redirect:" + returnUrl;
    }

}
