package com.atguigu.gmall.auth.config;

import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/24 16:27
 * @Email: 1796235969@qq.com
 */
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    private String pubKeyPath;
    private String priKeyPath;
    private String secret;
    private Integer expire;
    private String cookieName;
    private String unick;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    @PostConstruct
    public void init() {
        try {
            File pubFile = new File(pubKeyPath);
            File priFile = new File(priKeyPath);

            if (!pubFile.exists() || !priFile.exists()) {
                RsaUtils.generateKey(pubKeyPath, priKeyPath, secret);
            }

            privateKey = RsaUtils.getPrivateKey(priKeyPath);
            publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
