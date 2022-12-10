package com.atguigu.gmall.pms.vo;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/9 23:37
 * @Email: 1796235969@qq.com
 */
@Data
public class BaseAttrVo extends SpuAttrValueEntity {

    public void setValueSelected(List<String> valueSelected) {
        if (CollectionUtils.isEmpty(valueSelected)) {
            return;
        }
        setAttrValue(StringUtils.join(valueSelected, ","));
    }
}
