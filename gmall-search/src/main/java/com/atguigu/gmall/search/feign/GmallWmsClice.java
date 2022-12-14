package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/14 11:52
 * @Email: 1796235969@qq.com
 */
@FeignClient("wms-service")
public interface GmallWmsClice extends GmallWmsApi {
}
