package com.atguigu.gmall.search.listener;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.search.feign.GmallPmsClice;
import com.atguigu.gmall.search.feign.GmallWmsClice;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValueVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/15 20:05
 * @Email: 1796235969@qq.com
 */
@Component
public class SearchListener {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private GmallPmsClice pmsClice;


    @Autowired
    private GmallWmsClice wmsClice;

    @Autowired
    private ElasticsearchRestTemplate restTemplate;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "SEARCH_INSERT_QUEUE"),
            exchange = @Exchange(value = "PMS_SPU_EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"item.*"}
    ))
    public void contextLoads(Long spuId, Channel channel, Message message) throws IOException {
        if (spuId == null) {
            // 确认消息: 游标， 是否批量确认消息， 如果为true，从最近确认消息开始到当前消息之间未被确认的消息都被批量确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }

        try {
            // 1. 根据spuId查询spu
            ResponseVo<SpuEntity> spuEntityResponseVo = pmsClice.querySpuById(spuId);
            SpuEntity spuEntity = spuEntityResponseVo.getData();

            // 如果 spus 为空的话直接直接返回
            if (spuEntity == null) {
                // 确认消息: 游标， 是否批量确认消息， 如果为true，从最近确认消息开始到当前消息之间未被确认的消息都被批量确认
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            // 2. 根据 spuId 查询 sku
            ResponseVo<List<SkuEntity>> skuResponseVo = pmsClice.querySkusBySpuId(spuEntity.getId());
            List<SkuEntity> skuEntities = skuResponseVo.getData();

            // 判断当前 spu 下的 skus 是否为空, 如果不为空则转化成 goods 集合
            if (CollectionUtils.isNotEmpty(skuEntities)) {

                // 4. 根据 品牌id 查询 品牌
                ResponseVo<BrandEntity> brandEntityResponseVo = pmsClice.queryBrandById(spuEntity.getBrandId());
                BrandEntity brandEntity = brandEntityResponseVo.getData();

                // 5. 根据 分类id 查询 分类
                ResponseVo<CategoryEntity> categoryEntityResponseVo = pmsClice.queryCategoryById(225L);
                CategoryEntity categoryEntity = categoryEntityResponseVo.getData();

                // 7. 查询 基本类型的检索属性和值
                ResponseVo<List<SpuAttrValueEntity>> baseAttrResponseVo = pmsClice.querySpuAttrValueEntitiesBySpuIdAndCid(
                        spuEntity.getCategoryId(), spuEntity.getId()
                );
                List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrResponseVo.getData();

                List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                    Goods goods = new Goods();
                    BeanUtils.copyProperties(skuEntity, goods);

                    // 设置 商品列表所需要的字段 相关字段
                    goods.setTitle(skuEntity.getTitle()); // 设置标题
                    goods.setSubtitle(skuEntity.getSubtitle()); // 设置副标题
                    goods.setPrice(skuEntity.getPrice().doubleValue()); // 设置价格
                    goods.setDefaultImage(skuEntity.getDefaultImage()); // 设置默认图片
                    goods.setSkuId(skuEntity.getId()); // 设置 skuId

                    // 设置 排序及过滤 相关字段
                    goods.setCreateTime(spuEntity.getCreateTime()); // 设置新品

                    // 3. 根据 skuId 查询对应的 sku 库存信息
                    ResponseVo<List<WareSkuEntity>> wareResponseVo = wmsClice.queryWareSkuEntities(skuEntity.getId());
                    List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
                    // 如果库存不为空, 默认销量为 0, 库存为无货. 如果不为空才需要设置
                    if (CollectionUtils.isNotEmpty(wareSkuEntities)) {
                        // 设置 是否有货
                        goods.setStore(
                                // 任意库存有货默认都有货, anyMatch 集合中 任意仓库商品数量减去已锁定的库存数量 大于 0 默认此 sku 有货
                                wareSkuEntities.stream().anyMatch( // [1, 2, 3] anyMatch(x -> x > 0) = true
                                        wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0
                                )
                        );
                        // 设置销量
                        goods.setSales(
                                // 将库存集合的销量转换成 集合进行求和
                                wareSkuEntities.stream().map(
                                        WareSkuEntity::getSales
                                ).reduce((a, b) -> a + b).get()
                        );
                    }

                    // 设置品牌相关参数
                    if (brandEntity != null) {
                        goods.setBrandId(brandEntity.getId()); // 品牌 id
                        goods.setBrandName(brandEntity.getName()); // 品牌 名称
                        goods.setLogo(brandEntity.getLogo());
                    }

                    // 设置分类相关参数
                    if (categoryEntity != null) {
                        goods.setCategoryId(categoryEntity.getId());
                        goods.setCategoryName(categoryEntity.getName());
                    }

                    // 检索类型的规格参数
                    // 6. 查询 销售类型的检索属性和值
                    ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = pmsClice.querySkuAttrValueEntitiesBySkuIdAndCid(
                            skuEntity.getCategoryId(), skuEntity.getId()
                    );
                    List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrResponseVo.getData();

                    List<SearchAttrValueVo> searchAttrs = new ArrayList<>();

                    // 把销售类型的检索属性和值转换成 vo 对象
                    if (CollectionUtils.isNotEmpty(skuAttrValueEntities)) {
                        searchAttrs.addAll(
                                skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                                    SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                                    BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValueVo);
                                    return searchAttrValueVo;
                                }).collect(Collectors.toList())
                        );
                    }

                    // 把基本类型的检索属性和值转换成 vo 对象
                    if (CollectionUtils.isNotEmpty(spuAttrValueEntities)) {
                        searchAttrs.addAll(
                                spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                                    SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                                    BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValueVo);
                                    return searchAttrValueVo;
                                }).collect(Collectors.toList())
                        );
                    }

                    goods.setSearchAttrs(searchAttrs); // 设置 检索类型的规格参数

                    return goods;
                }).collect(Collectors.toList());

                restTemplate.save(goodsList); // 导入索引库
            }

            // 确认消息: 游标， 是否批量确认消息， 如果为true，从最近确认消息开始到当前消息之间未被确认的消息都被批量确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
            if (message.getMessageProperties().getRedelivered()) {
                // 拒绝消息  不重新入队
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            }else {
                // 不确认消息: 游标, 是否批量确认, 重新入队
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        }
    }



}
