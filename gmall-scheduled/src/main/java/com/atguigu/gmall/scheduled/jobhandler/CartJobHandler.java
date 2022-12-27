package com.atguigu.gmall.scheduled.jobhandler;

import com.atguigu.gmall.scheduled.mapper.CartMapper;
import com.atguigu.gmall.scheduled.pojo.Cart;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/27 16:12
 * @Email: 1796235969@qq.com
 */
@Component
public class CartJobHandler {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CartMapper cartMapper;

    private static final String EXCEPTION_PREFIX = "CART:EXCEPTION";

    private static final String KEY_PREFIX = "CART:INFO:";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @XxlJob("cartAsyncData")
    public ReturnT<String> cartAsyncData(String param) {
        BoundSetOperations<String, String> setOps = redisTemplate.boundSetOps(EXCEPTION_PREFIX);

        String userId = setOps.pop();
        while (StringUtils.isNotEmpty(userId)) {
            // 1. 根据userId将数据库该用户的购物车全部删除
            cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id", userId));

            // 2. 获取redis中改用户的数据
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);

            List<Object> cartsJson = hashOps.values();
            // 3. 如果购物车数据为空 直接跳过这次循环
            if (CollectionUtils.isEmpty(cartsJson)) {
                userId = setOps.pop();
                continue;
            }
            cartsJson.forEach(cartJson -> {
                try {
                    Cart cart = MAPPER.readValue(cartJson.toString(), Cart.class);
                    cart.setId(null);

                    // 4. 将redis中的数据同步到数据库中
                    cartMapper.insert(cart);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });

            // 获取下一个元素
            userId = setOps.pop();
        }

        return ReturnT.SUCCESS;
    }
}
