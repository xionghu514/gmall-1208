package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.annotation.GmallCache;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/22 9:02
 * @Email: 1796235969@qq.com
 */
@Component
@Aspect
public class GmallCacheAspect {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RBloomFilter bloomFilter;

    @Pointcut("@annotation(com.atguigu.gmall.index.annotation.GmallCache)")
    public void pointcut(){};


    /**
     *  环绕通知要点：
     *  1.方法必须返回Object参数
     *  2.方法必须有ProceedingJoinPoint参数
     *  3.方法必须抛出Throwable异常
     *  4.必须手动执行目标方法
     */
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 目标方法形参
        Object[] args = joinPoint.getArgs();

        // 通过反射获得签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取方法对象
        Method method = signature.getMethod();
        GmallCache gmallCache = method.getAnnotation(GmallCache.class);

        // 获取缓存前缀
        String prefix = gmallCache.prefix();
        // 获取形参字符串
        String argsString = StringUtils.join(args, ",");
        // 获取缓存key
        String key = prefix + argsString;

        // 通过布隆过滤器查询数据是否存在，不存在则返回null
        if (!bloomFilter.contains(key)) {
            return null;
        }

        // 1、先查询缓存，如果命中直接走缓存
        String json = redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotEmpty(json)) {
            return JSON.parseObject(json, signature.getReturnType());
        }

        // 获取锁前缀
        String lock = gmallCache.lock();
        // 2.为防止缓存击穿，添加分布式锁
        RLock fairLock = redissonClient.getFairLock(lock + argsString);

        // 加锁
        fairLock.lock();

        try {
            // 3.在获取锁的过程中，其他请求也许已经将数据放入缓存，所以再次访问缓存
            String json2 = redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotEmpty(json2)) {
                return JSON.parseObject(json2, signature.getReturnType());
            }

            // 4. 执行目标方法
            Object result = joinPoint.proceed(args);

            // 5.将结果放入缓存设置过期时间 并释放分布式锁
            int timeout = gmallCache.timeOut() + new Random().nextInt(gmallCache.random());
            redisTemplate.opsForValue().set(key, JSON.toJSONString(result), timeout, TimeUnit.MINUTES);

            return result;
        } finally {
            // 解锁
            fairLock.unlock();
        }


    }


/*    @Pointcut("execution(* com.atguigu.gmall.index.service.*.*(..))")
    public void pointcut(){};

    @Before("pointcut()")
    public void before() {
        System.out.println("我是前置通知");
    }

    @After("pointcut()")
    public void after() {
        System.out.println("我是后置通知");
    }*/
}
