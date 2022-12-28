package com.atguigu.gmall.oms.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.entity.OrderItemEntity;
import com.atguigu.gmall.oms.feign.GmallPmsClient;
import com.atguigu.gmall.oms.feign.GmallSmsClient;
import com.atguigu.gmall.oms.mapper.OrderItemMapper;
import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.atguigu.gmall.oms.service.OrderService;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements OrderService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private OrderItemMapper orderItemMapper;


    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<OrderEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<OrderEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public void saveOrder(OrderSubmitVo submitVo, Long userId) {
        // 1.保存订单表
        OrderEntity orderEntity = new OrderEntity();

        orderEntity.setUserId(userId);
        orderEntity.setOrderSn(submitVo.getOrderToken());
        orderEntity.setCreateTime(new Date());
        orderEntity.setTotalAmount(submitVo.getTotalPrice());
        orderEntity.setPayAmount(submitVo.getTotalPrice());
        orderEntity.setPayType(submitVo.getPayType());
        orderEntity.setSourceType(0);
        orderEntity.setStatus(0);
        orderEntity.setDeliveryCompany(submitVo.getDeliveryCompany());
        UserAddressEntity address = submitVo.getAddress();

        if (address != null) {
            orderEntity.setReceiverName(address.getName());
            orderEntity.setReceiverPhone(address.getPhone());
            orderEntity.setReceiverPostCode(address.getPostCode());
            orderEntity.setReceiverProvince(address.getProvince());
            orderEntity.setReceiverCity(address.getCity());
            orderEntity.setReceiverRegion(address.getRegion());
            orderEntity.setReceiverAddress(address.getAddress());
        }

        orderEntity.setConfirmStatus(0);
        orderEntity.setDeleteStatus(0);
        orderEntity.setUseIntegration(submitVo.getBounds());

        save(orderEntity);
        // 获取订单id
        Long orderId = orderEntity.getId();

        // 2.保存订单详情表
        List<OrderItemVo> items = submitVo.getItems();

        if (CollectionUtils.isNotEmpty(items)) {
            items.forEach(item -> {
                OrderItemEntity itemEntity = new OrderItemEntity();
                itemEntity.setOrderId(orderId);
                itemEntity.setOrderSn(submitVo.getOrderToken());

                // 根据skuId获取sku
                SkuEntity skuEntity = pmsClient.querySkuById(item.getSkuId()).getData();
                itemEntity.setSkuId(item.getSkuId());
                itemEntity.setSkuName(skuEntity.getName());
                itemEntity.setSkuPrice(skuEntity.getPrice());
                itemEntity.setSkuPic(skuEntity.getDefaultImage());
                itemEntity.setSkuQuantity(item.getCount().intValue());

                // 根据skuId获取sku销售属性
                List<SkuAttrValueEntity> skuAttrValueEntities = pmsClient.querySaleAttrValueBySkuId(skuEntity.getId()).getData();
                itemEntity.setSkuAttrsVals(JSON.toJSONString(skuAttrValueEntities));

                // 根据spuId获取spu
                SpuEntity spuEntity = pmsClient.querySpuById(skuEntity.getSpuId()).getData();
                itemEntity.setSpuId(skuEntity.getSpuId());
                itemEntity.setSpuBrand(spuEntity.getBrandId().toString());
                itemEntity.setSpuName(spuEntity.getName());
                itemEntity.setCategoryId(spuEntity.getCategoryId());
                // 根据spuId获取描述信息
                SpuDescEntity spuDescEntity = pmsClient.querySpuDescById(spuEntity.getId()).getData();

                if (spuDescEntity != null) {
                    itemEntity.setSpuPic(spuDescEntity.getDecript());
                }

                itemEntity.setRealAmount(skuEntity.getPrice());

                orderItemMapper.insert(itemEntity);
            });
        }


    }

}