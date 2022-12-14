package com.atguigu.gmall.pms.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service("spuAttrValueService")
public class SpuAttrValueServiceImpl extends ServiceImpl<SpuAttrValueMapper, SpuAttrValueEntity> implements SpuAttrValueService {
    @Autowired
    private AttrMapper attrMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SpuAttrValueEntity> querySpuAttrValueEntitiesBySpuIdAndCid(Long cid, Long spuId) {
        // 根据cid 和 searchType 类型查询有哪些检索属性
        List<AttrEntity> attrEntities = attrMapper.selectList(
                new LambdaQueryWrapper<AttrEntity>().eq(AttrEntity::getCategoryId, cid)
                        .eq(AttrEntity::getSearchType, 1)
        );
        if (CollectionUtils.isEmpty(attrEntities)) {
            return null;
        }
        List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());

        // 根据检索属性的id 和 spuId 查询出普通类型的检索属性和值
        List<SpuAttrValueEntity> spuAttrValueEntities = list(new LambdaQueryWrapper<SpuAttrValueEntity>().eq(SpuAttrValueEntity::getSpuId, spuId)
                .in(SpuAttrValueEntity::getAttrId, attrIds));
        return spuAttrValueEntities;
    }

}