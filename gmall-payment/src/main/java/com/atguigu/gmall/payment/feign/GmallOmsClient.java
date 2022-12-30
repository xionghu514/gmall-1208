package com.atguigu.gmall.payment.feign;

import com.atguigu.gmall.oms.api.GmallOmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/29 12:35
 * @Email: 1796235969@qq.com
 */
@FeignClient("oms-service")
public interface GmallOmsClient extends GmallOmsApi {
}
