package com.atguigu.gmall.pms.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {
    @Autowired
    private AttrMapper attrMapper;

    @Autowired
    private SkuMapper skuMapper;

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
        if (CollectionUtils.isEmpty(attrEntities)) {
            return null;
        }

        List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());

        // 根据检索属性的id 和 spuId 查询出普通类型的检索属性和值
        List<SkuAttrValueEntity> skuAttrValueEntities = list(new LambdaQueryWrapper<SkuAttrValueEntity>().eq(SkuAttrValueEntity::getSkuId, skuId)
                .in(SkuAttrValueEntity::getAttrId, attrIds));
        return skuAttrValueEntities;
    }

    @Override
    public List<SaleAttrValueVo> querySaleAttrValuesBySpuId(Long spuId) {
        // 1.根据spuId获取所有的sku
        List<SkuEntity> skuEntities = skuMapper.selectList(
                new LambdaQueryWrapper<SkuEntity>().eq(SkuEntity::getSpuId, spuId)
        );
        // 如果sku集合为空就直接返回
        if (CollectionUtils.isEmpty(skuEntities)) {
            return null;
        }

        // 2.根据skuId集合获取销售属性
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());
        List<SkuAttrValueEntity> skuAttrValueEntities = list(new LambdaQueryWrapper<SkuAttrValueEntity>().in(SkuAttrValueEntity::getSkuId, skuIds));

        // 如果销售属性集合为空， 直接返回null
        if (CollectionUtils.isEmpty(skuAttrValueEntities)) {
            return null;
        }

        // 3. 将销售属性集合转换成List<SaleAttrValueVo>
        ArrayList<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();
        // 将销售属性集合按照attrId进行分组
        Map<Long, List<SkuAttrValueEntity>> map = skuAttrValueEntities.stream().collect(Collectors.groupingBy(SkuAttrValueEntity::getAttrId));
        map.forEach((attrId, valueEntities) -> {
            SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
            // 设置attrId
            saleAttrValueVo.setAttrId(attrId);
            // 在有该数据分组的情况下，至少会有一条数据
            saleAttrValueVo.setAttrName(valueEntities.get(0).getAttrName());
            // 过滤重复条件
            Set<String> skuAttrValues = valueEntities.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet());
            saleAttrValueVo.setAttrValue(skuAttrValues);

            saleAttrValueVos.add(saleAttrValueVo);
        });

        return saleAttrValueVos;
    }

}