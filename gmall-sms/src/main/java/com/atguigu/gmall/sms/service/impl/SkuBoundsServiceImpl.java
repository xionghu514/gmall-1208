package com.atguigu.gmall.sms.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.mapper.SkuBoundsMapper;
import com.atguigu.gmall.sms.mapper.SkuFullReductionMapper;
import com.atguigu.gmall.sms.mapper.SkuLadderMapper;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {
    @Autowired
    private SkuFullReductionMapper fullReductionMapper;

    @Autowired
    private SkuLadderMapper ladderMapper;


    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuBoundsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    @Transactional
    public void saveSales(SkuSaleVo skuSaleVo) {

        System.out.println("skuSaleVo = " + skuSaleVo);
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(skuSaleVo, skuBoundsEntity);

        // 获取优惠生效情况
        List<Integer> work = skuSaleVo.getWork();
        if (CollectionUtils.isNotEmpty(work)) {
            skuBoundsEntity.setWork(work.get(3) * 8 + work.get(2) * 4 + work.get(1) * 2 + work.get(0));
        }
        // 保存积分表
        save(skuBoundsEntity);

        // 保存满减信息表
        SkuFullReductionEntity fullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuSaleVo, fullReductionEntity);
        fullReductionEntity.setAddOther(skuSaleVo.getFullAddOther());
        fullReductionMapper.insert(fullReductionEntity);

        // 保存打折表
        SkuLadderEntity ladderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuSaleVo, ladderEntity);
        ladderEntity.setAddOther(skuSaleVo.getLadderAddOther());
        ladderMapper.insert(ladderEntity);

    }

    @Override
    public List<ItemSaleVo> querySalesBySkuId(Long skuId) {
        ArrayList<ItemSaleVo> itemSaleVos = new ArrayList<>();

        SkuBoundsEntity skuBoundsEntity = getOne(
                new LambdaQueryWrapper<SkuBoundsEntity>()
                        .eq(SkuBoundsEntity::getSkuId, skuId)
        );
        // 积分
        if (skuBoundsEntity != null) {
            ItemSaleVo itemSaleVo = new ItemSaleVo();
            itemSaleVo.setSaleId(skuBoundsEntity.getId());
            itemSaleVo.setType("积分");
            itemSaleVo.setDesc("送" + skuBoundsEntity.getBuyBounds() + "购物积分，送" + skuBoundsEntity.getGrowBounds() + "成长积分");
            itemSaleVos.add(itemSaleVo);
        }

        SkuFullReductionEntity fullReductionEntity = fullReductionMapper.selectOne(
                new LambdaQueryWrapper<SkuFullReductionEntity>().eq(SkuFullReductionEntity::getSkuId, skuId)
        );
        // 满减
        if (fullReductionEntity != null) {
            ItemSaleVo itemSaleVo = new ItemSaleVo();
            itemSaleVo.setSaleId(fullReductionEntity.getId());
            itemSaleVo.setType("满减");
            itemSaleVo.setDesc("满" + fullReductionEntity.getFullPrice() + "减" + fullReductionEntity.getReducePrice());
            itemSaleVos.add(itemSaleVo);
        }

        // 打折
        SkuLadderEntity skuLadderEntity = ladderMapper.selectOne(
                new LambdaQueryWrapper<SkuLadderEntity>().eq(SkuLadderEntity::getSkuId, skuId)
        );
        if (skuLadderEntity != null) {
            ItemSaleVo itemSaleVo = new ItemSaleVo();
            itemSaleVo.setSaleId(skuLadderEntity.getId());
            itemSaleVo.setType("打折");
            itemSaleVo.setDesc("买" + skuLadderEntity.getFullCount() + "件打" + skuLadderEntity.getDiscount().divide(new BigDecimal(10)) + "折");
            itemSaleVos.add(itemSaleVo);
        }

        return itemSaleVos;
    }

}