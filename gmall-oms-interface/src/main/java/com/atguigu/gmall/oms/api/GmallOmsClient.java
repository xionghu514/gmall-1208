package com.atguigu.gmall.oms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/28 20:22
 * @Email: 1796235969@qq.com
 */
public interface GmallOmsClient {

    @PostMapping("oms/order/save/{userId}")
    public ResponseVo saveOrder(@RequestBody OrderSubmitVo submitVo, @PathVariable("userId") Long userId);

}
