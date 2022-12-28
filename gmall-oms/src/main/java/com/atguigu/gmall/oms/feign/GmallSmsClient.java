package com.atguigu.gmall.oms.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/22 16:17
 * @Email: 1796235969@qq.com
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}
