package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/22 14:31
 * @Email: 1796235969@qq.com
 */
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
