package com.atguigu.gmall.index.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/17 12:39
 * @Email: 1796235969@qq.com
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
