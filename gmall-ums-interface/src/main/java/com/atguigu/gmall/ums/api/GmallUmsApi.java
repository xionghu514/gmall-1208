package com.atguigu.gmall.ums.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/24 12:17
 * @Email: 1796235969@qq.com
 */
public interface GmallUmsApi {

    @GetMapping("ums/user/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<UserEntity> queryUserById(@PathVariable("id") Long id);

    @GetMapping("ums/user/query")
    public ResponseVo<UserEntity> queryUser(@RequestParam("loginName") String loginName, @RequestParam("password") String password);

    @PostMapping("ums/user/register")
    public ResponseVo register(UserEntity userEntity, @RequestParam("code") String code);

    @GetMapping("ums/user/check/{data}/{type}")
    public ResponseVo<Boolean> checkData(@PathVariable("data") String data, @PathVariable("type") Integer type);

    @GetMapping("ums/useraddress/user/{userId}")
    public ResponseVo<List<UserAddressEntity>> queryAddressByUserId(@PathVariable("userId") Long userId);
}
