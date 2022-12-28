package com.atguigu.gmall.order.interceptor;

import com.atguigu.gmall.order.pojo.UserInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/25 14:06
 * @Email: 1796235969@qq.com
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {


    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {


        // 判断token是否为空，不为空获取userId
        Long userId = Long.valueOf(request.getHeader("userId"));

        String username = request.getHeader("username");

        UserInfo userInfo = new UserInfo(userId, null, username);

        THREAD_LOCAL.set(userInfo);

        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        THREAD_LOCAL.remove();
    }

    public static UserInfo getUserInfo() {
        return THREAD_LOCAL.get();
    }

}
