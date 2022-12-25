package com.atguigu.gmall.gateway.filter;

import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.gateway.config.JwtProperties;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/24 22:48
 * @Email: 1796235969@qq.com
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class AuthGatewayFilterFactory  extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.KeyValueConfig> {
    @Autowired
    private JwtProperties jwtProperties;

    public AuthGatewayFilterFactory() {
        super(KeyValueConfig.class);
    }

    @Override
    public GatewayFilter apply(KeyValueConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                // 获取拦截名单
                List<String> paths = config.getPaths();

                ServerHttpResponse response = exchange.getResponse();
                ServerHttpRequest request = exchange.getRequest();
                // 1. 判断请求是否在拦截名单， 不在就直接放行
                String curPath = request.getURI().getPath();

                if (!CollectionUtils.isEmpty(paths) && !paths.stream().anyMatch(path -> curPath.startsWith(path))) {
                    return chain.filter(exchange);
                }

                // 2. 获取token, 异步-头信息，同步-cookie
                String token = request.getHeaders().getFirst(jwtProperties.getToken());

                if (StringUtils.isEmpty(token)) {
                    MultiValueMap<String, HttpCookie> multiValueMap = request.getCookies();
                    if (CollectionUtils.isNotEmpty(multiValueMap) && multiValueMap.containsKey(jwtProperties.getCookieName())) {
                        token = multiValueMap.getFirst(jwtProperties.getCookieName()).getValue();
                    }
                }

                // 3. 判空，如果token为空则直接重定向到登录页面
                if (StringUtils.isEmpty(token)) {
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    return response.setComplete();
                }

                try {
                    // 4. 解析token，如果解析失败直接重定向到登录页面
                    Map<String, Object> map = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());

                    // 5.比较ip地址，如果ip不一致，直接重定向到登录页面
                    String curIp = IpUtils.getIpAddressAtGateway(request);
                    String ip = map.get("ip").toString(); //载荷中的ip

                    if (!StringUtils.equals(curIp, ip)) {
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                        return response.setComplete();
                    }

                    // 6. 将用户数据传给之后的业务操作
                    request.mutate().header("userId", map.get("userId").toString())
                            .header("username", map.get("username").toString())
                            .build();

                    exchange.mutate().request(request).build();

                    // 7. 放行
                    return chain.filter(exchange);
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    return response.setComplete();
                }

            }
        };
    }

    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("paths");
    }

    @Data
    public static class KeyValueConfig {
        private List<String> paths;
    }
}
