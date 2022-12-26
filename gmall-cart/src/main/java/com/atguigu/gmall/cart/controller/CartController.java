package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/25 14:12
 * @Email: 1796235969@qq.com
 */
@Controller
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping
    public String saveCart(Cart cart) {
        cartService.saveCart(cart);

        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + cart.getSkuId() + "&count=" + cart.getCount();
    }

    @GetMapping("addCart.html")
    public String queryCart(Cart cart) {
        BigDecimal count = cart.getCount();
        cart = cartService.queryCartBySkuId(cart.getSkuId());
        cart.setCount(count);

        return "addCart";
    }


    @GetMapping("test")
    @ResponseBody
    public String test(HttpServletRequest request) {
        long currentTimeMillis = System.currentTimeMillis();
        ListenableFuture<String> result = cartService.exception1();
        ListenableFuture<String> result2 = cartService.exception2();
        try {
            // 非阻塞方式获取返回结果
            result.addCallback(res -> {
                System.out.println(res);
            }, ex -> {
                System.out.println(ex);
            });

            result2.addCallback(res -> {
                System.out.println(res);
            }, ex -> {
                System.out.println(ex);
            });
            // 同步获取返回结果集
//            System.out.println(result.get() + "============" + result2.get());
        } catch (Exception e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println( "所用时间: " + (System.currentTimeMillis() - currentTimeMillis) + "毫秒");
        return "hello cart";
    }
}
