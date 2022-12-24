package com.atguigu.gmall.auth.service;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.AuthException;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/24 17:24
 * @Email: 1796235969@qq.com
 */
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {
    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private JwtProperties jwtProperties;

    public void login(String loginName, String password, HttpServletResponse response, HttpServletRequest request) {
        // 1. 根据用户信息，查询用户
        ResponseVo<UserEntity> responseVo = umsClient.queryUser(loginName, password);
        UserEntity userEntity = responseVo.getData();

        // 2. 判空，如果为空则说明用户账号或者密码错误
        if (userEntity == null) {
            throw new AuthException("账号或者密码错误");
        }

        // 3. 组装载荷
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userEntity.getId());
        map.put("username", userEntity.getUsername());

        // 组装ip，以防数据被盗用
        map.put("ip", IpUtils.getIpAddressAtService(request));

        try {
            // 4. 生成jwt类型的token
            String token = JwtUtils.generateToken(map, jwtProperties.getPrivateKey(), jwtProperties.getExpire() * 60);

            // 5. 放入cookie
            CookieUtils.setCookie(request, response, jwtProperties.getCookieName(), token, jwtProperties.getExpire() * 60);
            CookieUtils.setCookie(request, response, jwtProperties.getUnick(), userEntity.getNickname(), jwtProperties.getExpire() * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
