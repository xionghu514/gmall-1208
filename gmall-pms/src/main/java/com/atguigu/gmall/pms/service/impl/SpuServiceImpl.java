package com.atguigu.gmall.pms.service.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.SpuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpusByCidAndPage(Long cid, PageParamVo paramVo) {
        LambdaQueryWrapper<SpuEntity> wrapper = new LambdaQueryWrapper<>();
        // cid = 0 是查询所有, 否则查询指定分类
        if (cid != 0) {
            wrapper.eq(SpuEntity::getCategoryId, cid);
        }

        // 获取关键字 key 既可以作为 spuId 也可以作为spuName
        String key = paramVo.getKey();
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(t -> t.eq(SpuEntity::getId, key).or().like(SpuEntity::getName, key));
        }
        IPage<SpuEntity> page = page(paramVo.getPage(), wrapper);

        return new PageResultVo(page);
    }

}