package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

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

    @GetMapping("checked/carts/{userId}")
    @ResponseBody
    public ResponseVo<List<Cart>> queryCheckedCartsByUserId(@PathVariable("userId") Long userId) {
        List<Cart> carts = cartService.queryCheckedCartsByUserId(userId);

        return ResponseVo.ok(carts);
    }

    @PostMapping("/deleteCart")
    @ResponseBody
    public ResponseVo<Cart> deleteCart(@RequestParam("skuId") Long skuId) {
        cartService.deleteCart(skuId);

        return ResponseVo.ok();
    }

    @PostMapping("/updateNum")
    @ResponseBody
    public ResponseVo<Cart> updataNum(@RequestBody Cart cart) {
        cartService.updataNum(cart);

        return ResponseVo.ok();
    }

    @GetMapping("cart.html")
    public String queryCarts(Model model) {
        List<Cart> carts = cartService.queryCarts();
        model.addAttribute("carts", carts);

        return "cart";
    }


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
//        ListenableFuture<String> result = cartService.exception1();
//        ListenableFuture<String> result2 = cartService.exception2();

//            // ?????????????????????????????????
//            result.addCallback(res -> {
//                System.out.println(res);
//            }, ex -> {
//                System.out.println(ex);
//            });
//
//            result2.addCallback(res -> {
//                System.out.println(res);
//            }, ex -> {
//                System.out.println(ex);
//            });
            // ???????????????????????????
//            System.out.println(result.get() + "============" + result2.get());

        cartService.exception1();
        cartService.exception2();
        System.out.println( "????????????: " + (System.currentTimeMillis() - currentTimeMillis) + "??????");
        return "hello cart";
    }
}
