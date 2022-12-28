package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/27 23:14
 * @Email: 1796235969@qq.com
 */
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}
