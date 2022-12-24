package com.atguigu.gmall.ums.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        // type 代表的类型 1 = 用户名；2 = 手机；3 = 邮箱
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isEmpty(data)) {
            return null;
        }
        // 对type 验证
        switch (type){
            case 1: wrapper.eq(UserEntity::getUsername, data);     break;
            case 2: wrapper.eq(UserEntity::getPhone, data);     break;
            case 3: wrapper.eq(UserEntity::getEmail, data);     break;
            default: return null;
        }


        return count(wrapper) == 0;
    }

    @Override
    public void register(UserEntity userEntity, String code) {
        // 1.验证短信验证码
        // 2.生成盐
        String salt = UUID.randomUUID().toString().substring(0, 6);
        userEntity.setSalt(salt);

        // 3.加盐加密
        String password = userEntity.getPassword() + salt;

        password = DigestUtils.md5Hex(password);
        userEntity.setPassword(password);

        userEntity.setLevelId(1l);
        userEntity.setNickname(userEntity.getUsername());
        userEntity.setSourceType(1);
        userEntity.setIntegration(1000);
        userEntity.setGrowth(1000);
        userEntity.setStatus(1);
        userEntity.setCreateTime(new Date());

        // 4. 新增用户
        save(userEntity);


    }

    @Override
    public UserEntity queryUser(String loginName, String password) {
        // loginName: 用户名/手机号/邮箱
        if (loginName == null) {
            return null;
        }

        // 1. 获取用户信息
        UserEntity userEntity = getOne(
                new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, loginName).or()
                        .eq(UserEntity::getPhone, loginName).or()
                        .eq(UserEntity::getEmail, loginName)
        );

        if (userEntity == null) {
            return userEntity;
        }

        // 2. 获取盐
        String salt = userEntity.getSalt();

        // 3. 给密码加盐加密进行对比
        password = DigestUtils.md5Hex(password + salt);

        if (StringUtils.equals(userEntity.getPassword(), password)) {
            return userEntity;
        }

        return null;
    }

}