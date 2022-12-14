package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/14 10:19
 * @Email: 1796235969@qq.com
 */
@FeignClient("pms-service")
public interface GmallPmsClice extends GmallPmsApi {
}
