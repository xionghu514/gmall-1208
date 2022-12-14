package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/14 10:21
 * @Email: 1796235969@qq.com
 */
@FeignClient("sms-service")
public interface GmallSmsClice extends GmallSmsApi {
}

