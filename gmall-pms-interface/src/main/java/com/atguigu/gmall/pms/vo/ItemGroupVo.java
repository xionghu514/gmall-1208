package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/22 13:59
 * @Email: 1796235969@qq.com
 */
@Data
public class ItemGroupVo {
    private Long Id;
    private String Name;
    private List<AttrValueVo> attrs;
}
