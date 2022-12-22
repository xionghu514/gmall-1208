package com.atguigu.gmall.index.config;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/22 12:07
 * @Email: 1796235969@qq.com
 */
@Configuration
public class BloomFilterConfig {
    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private GmallPmsClient pmsClient;

    private final String KEY_PREFIX = "index:categories:";

    @Bean
    public RBloomFilter bloomFilter() {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter("bf");
        bloomFilter.tryInit(2000, 0.03);

        // 向布隆过滤器初始化广告和分类信息
        ResponseVo<List<CategoryEntity>> responseVo = pmsClient.queryCategoryByPid(0l);
        List<CategoryEntity> categoryEntities = responseVo.getData();

        categoryEntities.forEach(categoryEntity -> {
            bloomFilter.add(KEY_PREFIX + categoryEntity.getId());
        });

        return bloomFilter;
    }

}
