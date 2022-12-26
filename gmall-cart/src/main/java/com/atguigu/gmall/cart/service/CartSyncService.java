package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/26 15:15
 * @Email: 1796235969@qq.com
 */
@Service
public class CartSyncService {

    @Autowired
    private CartMapper cartMapper;

    @Async
    public void insertCart(Cart cart) {
        cartMapper.insert(cart);
    }

    @Async
    public void updataCart(Cart cart, String userId, String skuId) {
        cartMapper.update(
                cart, new UpdateWrapper<Cart>()
                        .eq("user_id", userId)
                        .eq("sku_id", skuId)
        );
    }
}
