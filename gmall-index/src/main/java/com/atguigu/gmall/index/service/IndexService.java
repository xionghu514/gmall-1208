package com.atguigu.gmall.index.service;

import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/17 12:06
 * @Email: 1796235969@qq.com
 */
@Service
public class IndexService {
    @Autowired
    private GmallPmsClient pmsClient;

    public List<CategoryEntity> queryCategoriesByPid() {
        List<CategoryEntity> categoryEntities = pmsClient.queryCategoryByPid(0l).getData();

        return categoryEntities;
    }
}
