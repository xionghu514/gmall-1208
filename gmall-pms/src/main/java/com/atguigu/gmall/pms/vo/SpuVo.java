package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/9 23:34
 * @Email: 1796235969@qq.com
 */
@Data
public class SpuVo extends SpuEntity {
    private List<String> spuImages;

    private List<BaseAttrVo> baseAttrs;

    private List<SkuVo> skus;

}
