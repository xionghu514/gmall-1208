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
        // cid = 0 ???????????????, ????????????????????????
        if (cid != 0) {
            wrapper.eq(SpuEntity::getCategoryId, cid);
        }

        // ??????????????? key ??????????????? spuId ???????????????spuName
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

        //?????? spu ?????????
        Long spuId = saveSpuInfo(spu);


        // ?????? spuDesc ?????????
        saveSpuDesc(spu, spuId);

        // ?????? spuAttrValue ?????????
        saveSpuAttrValue(spu, spuId);

        // ?????? sku ????????????
        saveSkuInfo(spu, spuId);

        rabbitTemplate.convertAndSend("PMS_SPU_EXCHANGE", "item.insert", spuId);

    }

    public void saveSkuInfo(SpuVo spu, Long spuId) {
        List<SkuVo> skus = spu.getSkus();
        // ?????? skuVo ??????
        skus.forEach(skuVo -> {
            List<String> skuVoImages = skuVo.getImages();
            // ??????sku ?????????
            skuVo.setCategoryId(spu.getCategoryId());
            skuVo.setBrandId(spu.getBrandId());
            skuVo.setSpuId(spuId);
            // ?????? sku ???????????????????????????
            if (CollectionUtils.isNotEmpty(skuVoImages)) {
                // ????????????????????????
                skuVo.setDefaultImage(skuVoImages.get(0));
            }
            skuMapper.insert(skuVo);
            // ??????skuId
            Long skuId = skuVo.getId();

            // ?????? skuVo??? ???????????? ??????
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            if (CollectionUtils.isNotEmpty(saleAttrs)) {
                // ?????? skuAttrValue ?????????
                saleAttrs.forEach(saleAttr -> {
                    saleAttr.setSort(0);
                    saleAttr.setSkuId(skuId);
                });
                // ???????????? skuAttrValue?????????
                skuAttrValueService.saveBatch(saleAttrs);
            }

            if (CollectionUtils.isNotEmpty(skuVoImages)) {
                // ???????????? skuImage ?????????
                skuImagesService.saveBatch(
                        skuVoImages.stream().map(skuVoImage -> {
                            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                            // ????????????url??????
                            skuImagesEntity.setUrl(skuVoImage);
                            // ????????????
                            skuImagesEntity.setSort(0);
                            // ??????skuId
                            skuImagesEntity.setSkuId(skuId);
                            // ???????????????????????????
                            skuImagesEntity.setDefaultStatus(
                                    StringUtils.equals(skuVoImage, skuVoImages.get(0)) ? 1 : 0
                            );

                            return skuImagesEntity;
                        }).collect(Collectors.toList())
                );
            }
            // ?????? sku ??????????????????
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
            // ?????? spuDesc ?????? ??????
            SpuDescEntity spuDescEntity = new SpuDescEntity();
            spuDescEntity.setSpuId(spuId);
            spuDescEntity.setDecript(join);
            descMapper.insert(spuDescEntity);
        }
    }

    public Long saveSpuInfo(SpuVo spu) {
        // ????????????spu????????? ??? ??????spu?????????
        spu.setCreateTime(new Date());
        spu.setUpdateTime(spu.getCreateTime());
        // ??????spu??????, ??????spuVo??????spuEntity, ????????????????????????
        save(spu);
        // ??????spuId
        Long spuId = spu.getId();
        return spuId;
    }

}