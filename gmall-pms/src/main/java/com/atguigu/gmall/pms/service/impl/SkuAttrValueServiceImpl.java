package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {
    @Autowired
    private AttrMapper attrMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuAttrValueEntity> querySkuAttrValueEntitiesBySkuIdAndCid(Long cid, Long skuId) {
        // 根据cid 和 searchType 类型查询有哪些检索属性
        List<AttrEntity> attrEntities = attrMapper.selectList(
                new LambdaQueryWrapper<AttrEntity>().eq(AttrEntity::getCategoryId, cid)
                        .eq(AttrEntity::getSearchType, 1)
        );
        List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());

        // 根据检索属性的id 和 spuId 查询出普通类型的检索属性和值
        List<SkuAttrValueEntity> skuAttrValueEntities = list(new LambdaQueryWrapper<SkuAttrValueEntity>().eq(SkuAttrValueEntity::getSkuId, skuId)
                .in(SkuAttrValueEntity::getAttrId, attrIds));
        return skuAttrValueEntities;
    }

}