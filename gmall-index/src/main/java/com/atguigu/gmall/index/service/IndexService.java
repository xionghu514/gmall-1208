package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final String KEY_PREFIX = "index:categories:";

    public List<CategoryEntity> queryCategoriesByPid() {
        List<CategoryEntity> categoryEntities = pmsClient.queryCategoryByPid(0l).getData();

        return categoryEntities;
    }

    public List<CategoryEntity> queryLvl23CategoriesByPid(Long pid) {
        // 先查询缓存 如果命中则直接走缓存
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(json)) {
            List<CategoryEntity> categoryEntities = JSON.parseArray(json, CategoryEntity.class);
            return categoryEntities;
        }

        // 没有缓存，走远程调用
        List<CategoryEntity> categoryEntityList = pmsClient.queryLevel23CategoriesByPid(pid).getData();
        // 解决缓存穿透，即使数据为null也进行缓存，缓存时间为5分钟
        if (CollectionUtils.isEmpty(categoryEntityList)) {
            redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryEntityList), 5, TimeUnit.MINUTES);
        }else {
            // 为解决缓存雪崩，给数据过期时间添加随机值
            redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryEntityList), 90 + new Random().nextInt(10), TimeUnit.DAYS);
        }

        return categoryEntityList;
    }
}
