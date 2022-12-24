package com.atguigu.gmall.common.exception;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/24 17:41
 * @Email: 1796235969@qq.com
 */
public class AuthException extends RuntimeException {
    public AuthException() {
        super();
    }

    public AuthException(String message) {
        super(message);
    }
}
