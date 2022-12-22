package com.atguigu.gmall.item.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/22 13:44
 * @Email: 1796235969@qq.com
 */
@Data
public class SaleAttrValueVo {
    private Long attrId;
    private String attrName;
    private List<String> attrValue;
}
