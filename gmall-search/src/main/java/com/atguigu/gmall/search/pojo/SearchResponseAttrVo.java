package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/11/2 23:07
 * @Email: moumouguan@gmail.com
 */
@Data
public class SearchResponseAttrVo {
    private Long attrId;
    private String attrName;
    private List<String> attrValues;
}