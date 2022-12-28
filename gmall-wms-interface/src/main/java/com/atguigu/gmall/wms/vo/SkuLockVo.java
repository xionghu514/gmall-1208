package com.atguigu.gmall.wms.vo;

import lombok.Data;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/28 13:42
 * @Email: 1796235969@qq.com
 */
@Data
public class SkuLockVo {
    private Long skuId;
    private Integer count;
    private Boolean lock; // 是否锁定成功
    private Long wareSkuId; // 锁定成功的仓库
}
