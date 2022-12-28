package com.atguigu.gmall.order.service;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.order.feign.GmallCartClient;
import com.atguigu.gmall.order.feign.GmallPmsClient;
import com.atguigu.gmall.order.feign.GmallSmsClient;
import com.atguigu.gmall.order.feign.GmallUmsClient;
import com.atguigu.gmall.order.feign.GmallWmsClient;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.pojo.OrderConfirmVo;
import com.atguigu.gmall.order.pojo.OrderItemVo;
import com.atguigu.gmall.order.pojo.OrderSubmitVo;
import com.atguigu.gmall.order.pojo.UserInfo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/27 22:02
 * @Email: 1796235969@qq.com
 */
@Service
public class OrderService {
    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallCartClient cartClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "order:token:";

    public OrderConfirmVo confirm() {
        // 获取用户id
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        // 1.根据userId查询收货地址列表
        List<UserAddressEntity> userAddressEntities = umsClient.queryAddressByUserId(userId).getData();
        confirmVo.setAddresses(userAddressEntities);

        // 2.根据userId查询已选中的购物车记录
        List<Cart> carts = cartClient.queryCheckedCartsByUserId(userId).getData();
        if (CollectionUtils.isEmpty(carts)) {
            throw new RuntimeException("您没有选中的购物车！");
        }

        List<OrderItemVo> items = carts.stream().map(cart -> {
            OrderItemVo itemVo = new OrderItemVo();
            itemVo.setSkuId(cart.getSkuId());
            itemVo.setCount(cart.getCount());

            // 3.根据skuId查询sku
            SkuEntity skuEntity = pmsClient.querySkuById(cart.getSkuId()).getData();
            if (skuEntity == null) {
                throw new RuntimeException("您选择的商品不存在！");
            }
            itemVo.setDefaultImage(skuEntity.getDefaultImage());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setWeight(skuEntity.getWeight());

            // 4.根据skuId查询sku销售属性
            List<SkuAttrValueEntity> skuAttrValueEntities = pmsClient.querySaleAttrValueBySkuId(skuEntity.getId()).getData();
            itemVo.setSaleAttrs(skuAttrValueEntities);

            // 5.根据skuId查询sku营销信息
            List<ItemSaleVo> itemSaleVos = smsClient.querySalesBySkuId(skuEntity.getId()).getData();
            itemVo.setSales(itemSaleVos);

            // 6.根据skuId查询sku库存信息
            List<WareSkuEntity> wareSkuEntities = wmsClient.queryWareSkuEntities(skuEntity.getId()).getData();
            if (CollectionUtils.isNotEmpty(wareSkuEntities)) {
                itemVo.setStore(
                        wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0)
                );
            }

            return itemVo;
        }).collect(Collectors.toList());

        confirmVo.setItems(items);

        // 7.根据userId查询用户
        UserEntity userEntity = umsClient.queryUserById(userId).getData();
        if (userEntity != null) {
            confirmVo.setBounds(userEntity.getIntegration());
        }

        // 雪花算法生成防重唯一id
        String orderToken = IdWorker.getIdStr();
        confirmVo.setOrderToken(orderToken);

        redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, orderToken);

        return confirmVo;
    }

    public void submit(OrderSubmitVo submitVo) {
        // 1. 防重
        // 2. 验总价
        // 3. 验库存并锁库存
        // 4. 创建订单
        // 5. 异步删除购物车记录
    }
}
