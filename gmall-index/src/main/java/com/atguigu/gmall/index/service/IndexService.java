package com.atguigu.gmall.index.service;

import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.annotation.GmallCache;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    private final String KEY_PREFIX = "index:categories:";

    private final String LOCK_PREFIX = "index:categories:lock:";

    public List<CategoryEntity> queryCategoriesByPid() {
        List<CategoryEntity> categoryEntities = pmsClient.queryCategoryByPid(0l).getData();

        return categoryEntities;
    }

    @GmallCache(prefix = KEY_PREFIX, timeOut = 129600, random = 14400, lock = LOCK_PREFIX)
    public List<CategoryEntity> queryLvl23CategoriesByPid(Long pid) {
        List<CategoryEntity> categoryEntityList = pmsClient.queryLevel23CategoriesByPid(pid).getData();

        return categoryEntityList;
    }
//    public List<CategoryEntity> queryLvl23CategoriesByPidw(Long pid) {
//        // 1.先查询缓存 如果命中则直接走缓存
//        String json = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
//        if (StringUtils.isNotBlank(json)) {
//            List<CategoryEntity> categoryEntities = JSON.parseArray(json, CategoryEntity.class);
//            return categoryEntities;
//        }
//        // 2.为防止缓存击穿，添加分布式锁
//        RLock lock = redissonClient.getLock(LOCK_PREFIX + pid);
//        // 加锁
//        lock.lock();
//        try {
//            // 3.在获取锁的过程中，其他请求也许已经将数据放入缓存，所以再次访问缓存
//            String json2 = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
//            if (StringUtils.isNotBlank(json2)) {
//                return JSON.parseArray(json2, CategoryEntity.class);
//            }
//
//            // 4.没有缓存，走远程调用
//            List<CategoryEntity> categoryEntityList = pmsClient.queryLevel23CategoriesByPid(pid).getData();
//            // 5. 解决缓存穿透，即使数据为null也进行缓存，缓存时间为5分钟
//            if (CollectionUtils.isEmpty(categoryEntityList)) {
//                redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryEntityList), 5, TimeUnit.MINUTES);
//            }else {
//                // 6. 为解决缓存雪崩，给数据过期时间添加随机值
//                redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryEntityList), 90 + new Random().nextInt(10), TimeUnit.DAYS);
//            }
//
//            return categoryEntityList;
//        } finally {
//            // 解锁
//            lock.unlock();
//        }
//    }
}
