package com.atguigu.gmall.pms.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    private AttrMapper attrMapper;

    @Autowired
    private SpuAttrValueMapper spuAttrValueMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<AttrGroupEntity> queryAttrGroupAndAttr(Long catId) {
        // 根据分类id查询分组
        List<AttrGroupEntity> attrGroupEntities = list(
                new LambdaQueryWrapper<AttrGroupEntity>()
                        .eq(AttrGroupEntity::getCategoryId, catId)
        );
        // 便利分组集合 设置分组里的规格参数集合
        if (CollectionUtils.isNotEmpty(attrGroupEntities)) {
            attrGroupEntities.forEach(attrGroupEntity -> {
                attrGroupEntity.setAttrEntities(
                        // 根据分组的Id查询规格参数
                        attrMapper.selectList(
                                new LambdaQueryWrapper<AttrEntity>()
                                        .eq(AttrEntity::getGroupId, attrGroupEntity.getId())
                                        .eq(AttrEntity::getType, 1)
                        )
                );
            });
        }
        return attrGroupEntities;
    }

    @Override
    public List<ItemGroupVo> queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(Long cid, Long spuId, Long skuId) {
        // 1. 根据分类 id 查询分组
        List<AttrGroupEntity> groupEntities = list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));

        if (groupEntities == null) {
            return null;
        }

        // 2. 遍历分组查询组下的规格参数
        return groupEntities.stream().map(attrGroupEntity -> {
            ItemGroupVo itemGroupVo = new ItemGroupVo();
            itemGroupVo.setId(attrGroupEntity.getId());
            itemGroupVo.setName(attrGroupEntity.getName());

            // 根据分组 id 查询规格参数
            List<AttrEntity> attrEntities = attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", attrGroupEntity.getId()));

            if (CollectionUtils.isEmpty(attrEntities)) {
                return itemGroupVo;
            }

            // 获取 规格参数 id 集合
            List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());

            List<AttrValueVo> attrs = new ArrayList<>();

            // 根据 规格参数 id 集合结合 spuId 查询基本类型的规格参数与值
            List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrValueMapper.selectList(
                    new QueryWrapper<SpuAttrValueEntity>().in("attr_id", attrIds).eq("spu_id", spuId)
            );

            if (CollectionUtils.isNotEmpty(spuAttrValueEntities)) {
                attrs.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                    AttrValueVo attrValueVo = new AttrValueVo();
                    BeanUtils.copyProperties(spuAttrValueEntity, attrValueVo);
                    return attrValueVo;
                }).collect(Collectors.toList()));
            }

            // 根据 规格参数 id 集合结合 skuId 查询销售类型的规格参数与值
            List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueMapper.selectList(
                    new QueryWrapper<SkuAttrValueEntity>().in("attr_id", attrIds).eq("sku_id", skuId)
            );

            if (CollectionUtils.isNotEmpty(skuAttrValueEntities)) {
                attrs.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                    AttrValueVo attrValueVo = new AttrValueVo();
                    BeanUtils.copyProperties(skuAttrValueEntity, attrValueVo);
                    return attrValueVo;
                }).collect(Collectors.toList()));
            }

            itemGroupVo.setAttrs(attrs);
            return itemGroupVo;
        }).collect(Collectors.toList());

    }

}