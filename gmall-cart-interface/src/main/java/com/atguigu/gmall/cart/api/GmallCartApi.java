package com.atguigu.gmall.cart.api;

import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/27 22:27
 * @Email: 1796235969@qq.com
 */
public interface GmallCartApi {

    @GetMapping("checked/carts/{userId}")
    @ResponseBody
    public ResponseVo<List<Cart>> queryCheckedCartsByUserId(@PathVariable("userId") Long userId);
}
