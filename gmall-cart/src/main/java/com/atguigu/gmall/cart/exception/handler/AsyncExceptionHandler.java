package com.atguigu.gmall.cart.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/26 14:22
 * @Email: 1796235969@qq.com
 */
@Component
@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
        // 记录日志 或者 记录到数据库
        log.error("异步任务执行失败，失败信息: {}, 失败方法: {}, 方法参数: {}", throwable.getMessage(), method.getName(), Arrays.asList(objects));
    }
}
