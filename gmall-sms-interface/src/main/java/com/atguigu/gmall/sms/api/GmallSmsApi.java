package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/10 17:03
 * @Email: 1796235969@qq.com
 */
public interface GmallSmsApi {
    @GetMapping("sms/skubounds/sales/save")
    ResponseVo saveSales (@RequestBody SkuSaleVo skuSaleVo);
}
