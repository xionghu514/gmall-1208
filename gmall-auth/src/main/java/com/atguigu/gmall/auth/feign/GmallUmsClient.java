package com.atguigu.gmall.auth.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/24 17:22
 * @Email: 1796235969@qq.com
 */
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}
