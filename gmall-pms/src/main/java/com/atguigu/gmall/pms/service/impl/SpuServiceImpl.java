package com.atguigu.gmall.pms.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.feign.GmallSmsClice;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.atguigu.gmall.pms.service.SpuService;
import com.atguigu.gmall.pms.vo.BaseAttrVo;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {
    @Autowired
    private SpuDescMapper descMapper;

    @Autowired
    private SpuAttrValueService spuAttrValueService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Autowired
    private GmallSmsClice smsClice;

    @Autowired
    private RabbitTemplate rabbitTemplate;

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

    @Override
    @GlobalTransactional
    public void bigSave(SpuVo spu) {

        //保存 spu 表信息
        Long spuId = saveSpuInfo(spu);


        // 保存 spuDesc 表信息
        saveSpuDesc(spu, spuId);

        // 保存 spuAttrValue 表信息
        saveSpuAttrValue(spu, spuId);

        // 保存 sku 相关信息
        saveSkuInfo(spu, spuId);


    }

    public void saveSkuInfo(SpuVo spu, Long spuId) {
        List<SkuVo> skus = spu.getSkus();
        // 遍历 skuVo 集合
        skus.forEach(skuVo -> {
            List<String> skuVoImages = skuVo.getImages();
            // 保存sku 表信息
            skuVo.setCategoryId(spu.getCategoryId());
            skuVo.setBrandId(spu.getBrandId());
            skuVo.setSpuId(spuId);
            // 判断 sku 的图片信息是否为空
            if (CollectionUtils.isNotEmpty(skuVoImages)) {
                // 设置默认图片地址
                skuVo.setDefaultImage(skuVoImages.get(0));
            }
            skuMapper.insert(skuVo);
            // 获取skuId
            Long skuId = skuVo.getId();

            // 获取 skuVo的 销售属性 集合
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            if (CollectionUtils.isNotEmpty(saleAttrs)) {
                // 保存 skuAttrValue 表信息
                saleAttrs.forEach(saleAttr -> {
                    saleAttr.setSort(0);
                    saleAttr.setSkuId(skuId);
                });
                // 批量保存 skuAttrValue表信息
                skuAttrValueService.saveBatch(saleAttrs);
            }

            if (CollectionUtils.isNotEmpty(skuVoImages)) {
                // 批量保存 skuImage 表信息
                skuImagesService.saveBatch(
                        skuVoImages.stream().map(skuVoImage -> {
                            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                            // 设置图片url信息
                            skuImagesEntity.setUrl(skuVoImage);
                            // 设置排序
                            skuImagesEntity.setSort(0);
                            // 设置skuId
                            skuImagesEntity.setSkuId(skuId);
                            // 设置是否是默认图片
                            skuImagesEntity.setDefaultStatus(
                                    StringUtils.equals(skuVoImage, skuVoImages.get(0)) ? 1 : 0
                            );

                            return skuImagesEntity;
                        }).collect(Collectors.toList())
                );
            }
            // 保存 sku 营销相关信息
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(skuVo, skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            smsClice.saveSales(skuSaleVo);
        });
    }

    public void saveSpuAttrValue(SpuVo spu, Long spuId) {
        List<BaseAttrVo> baseAttrVos = spu.getBaseAttrs();
        if (CollectionUtils.isNotEmpty(baseAttrVos)) {
            spuAttrValueService.saveBatch(baseAttrVos.stream()
                    .map(baseAttrVo -> {
                        SpuAttrValueEntity attrValueEntity = new SpuAttrValueEntity();
                        baseAttrVo.setSort(0);
                        baseAttrVo.setSpuId(spuId);
                        BeanUtils.copyProperties(baseAttrVo, attrValueEntity);
                        return attrValueEntity;
                    }).collect(Collectors.toList())
            );
        }
    }

    public void saveSpuDesc(SpuVo spu, Long spuId) {
        List<String> spuImages = spu.getSpuImages();
        if (CollectionUtils.isNotEmpty(spuImages)) {
            String join = StringUtils.join(spuImages, ",");
            // 创建 spuDesc 接受 信息
            SpuDescEntity spuDescEntity = new SpuDescEntity();
            spuDescEntity.setSpuId(spuId);
            spuDescEntity.setDecript(join);
            descMapper.insert(spuDescEntity);
        }
    }

    public Long saveSpuInfo(SpuVo spu) {
        // 设置创建spu的时间 和 修改spu的时间
        spu.setCreateTime(new Date());
        spu.setUpdateTime(spu.getCreateTime());
        // 保存spu信息, 因为spuVo继承spuEntity, 所以不用进行转换
        save(spu);
        // 获取spuId
        Long spuId = spu.getId();
        return spuId;
    }

}