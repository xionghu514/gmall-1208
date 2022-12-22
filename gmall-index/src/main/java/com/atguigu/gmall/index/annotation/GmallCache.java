package com.atguigu.gmall.index.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/22 8:29
 * @Email: 1796235969@qq.com
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {

    /**
     * 指定缓存前缀，默认是gmall:cache:
     * @return
     */
    String prefix() default "gmall:cache:";

    /**
     * 指定缓存过期时间，默认时间是30分钟
     * @return
     */
    int timeOut() default 30;

    /**
     * 为了防止缓存雪崩，指定缓存随机时间，默认范围为10分钟
     * @return
     */
    int random() default 10;

    /**
     * 为了防止缓存击穿，给缓存添加分布式锁
     * 这里可以指定锁的前缀，默认为：gmall:lock:
     * @return
     */
    String lock() default "gmall:lock:";

}
