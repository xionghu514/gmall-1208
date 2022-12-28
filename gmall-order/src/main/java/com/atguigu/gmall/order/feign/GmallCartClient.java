package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.cart.api.GmallCartApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/27 23:15
 * @Email: 1796235969@qq.com
 */
@FeignClient("cart-service")
public interface GmallCartClient extends GmallCartApi {
}
