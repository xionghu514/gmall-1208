package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/10 17:04
 * @Email: 1796235969@qq.com
 */
@FeignClient("sms-service")
public interface GmallSmsClice extends GmallSmsApi {
}
