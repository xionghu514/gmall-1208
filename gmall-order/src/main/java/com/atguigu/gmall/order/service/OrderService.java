package com.atguigu.gmall.order.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.feign.GmallCartClient;
import com.atguigu.gmall.order.feign.GmallOmsClient;
import com.atguigu.gmall.order.feign.GmallPmsClient;
import com.atguigu.gmall.order.feign.GmallSmsClient;
import com.atguigu.gmall.order.feign.GmallUmsClient;
import com.atguigu.gmall.order.feign.GmallWmsClient;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.order.vo.UserInfo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
    private GmallOmsClient omsClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "order:token:";

    public OrderConfirmVo confirm() {
        // ????????????id
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        // 1.??????userId????????????????????????
        List<UserAddressEntity> userAddressEntities = umsClient.queryAddressByUserId(userId).getData();
        confirmVo.setAddresses(userAddressEntities);

        // 2.??????userId?????????????????????????????????
        List<Cart> carts = cartClient.queryCheckedCartsByUserId(userId).getData();
        if (CollectionUtils.isEmpty(carts)) {
            throw new RuntimeException("??????????????????????????????");
        }

        List<OrderItemVo> items = carts.stream().map(cart -> {
            OrderItemVo itemVo = new OrderItemVo();
            itemVo.setSkuId(cart.getSkuId());
            itemVo.setCount(cart.getCount());

            // 3.??????skuId??????sku
            SkuEntity skuEntity = pmsClient.querySkuById(cart.getSkuId()).getData();
            if (skuEntity == null) {
                throw new RuntimeException("??????????????????????????????");
            }
            itemVo.setDefaultImage(skuEntity.getDefaultImage());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setWeight(skuEntity.getWeight());

            // 4.??????skuId??????sku????????????
            List<SkuAttrValueEntity> skuAttrValueEntities = pmsClient.querySaleAttrValueBySkuId(skuEntity.getId()).getData();
            itemVo.setSaleAttrs(skuAttrValueEntities);

            // 5.??????skuId??????sku????????????
            List<ItemSaleVo> itemSaleVos = smsClient.querySalesBySkuId(skuEntity.getId()).getData();
            itemVo.setSales(itemSaleVos);

            // 6.??????skuId??????sku????????????
            List<WareSkuEntity> wareSkuEntities = wmsClient.queryWareSkuEntities(skuEntity.getId()).getData();
            if (CollectionUtils.isNotEmpty(wareSkuEntities)) {
                itemVo.setStore(
                        wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0)
                );
            }

            return itemVo;
        }).collect(Collectors.toList());

        confirmVo.setItems(items);

        // 7.??????userId????????????
        UserEntity userEntity = umsClient.queryUserById(userId).getData();
        if (userEntity != null) {
            confirmVo.setBounds(userEntity.getIntegration());
        }

        // ??????????????????????????????id
        String orderToken = IdWorker.getIdStr();
        confirmVo.setOrderToken(orderToken);

        redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, orderToken, 25, TimeUnit.HOURS);

        return confirmVo;
    }

    public void submit(OrderSubmitVo submitVo) {
        String orderToken = submitVo.getOrderToken();
        if (StringUtils.isEmpty(orderToken)) {
            throw new RuntimeException("????????????!");
        }

        // 1. ??????
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

        Boolean flag = redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class),
                Arrays.asList(KEY_PREFIX + orderToken), orderToken
        );

        if (!flag) {
            throw new RuntimeException("?????????????????????!");
        }

        // 2. ?????????
        List<OrderItemVo> items = submitVo.getItems(); // ????????????
        if (CollectionUtils.isEmpty(items)) {
            throw new RuntimeException("???????????????!");
        }

        BigDecimal curPrice = items.stream().map(item -> {
            Long skuId = item.getSkuId();
            SkuEntity skuEntity = pmsClient.querySkuById(skuId).getData();
            if (skuEntity == null) {
                return new BigDecimal(0);
            }
            BigDecimal price = skuEntity.getPrice().multiply(item.getCount());
            return price;
        }).reduce((a, b) -> a.add(b)).get();

        if (curPrice.compareTo(submitVo.getTotalPrice()) != 0) {
            throw new RuntimeException("???????????????????????????????????????");
        }

        // 3. ?????????????????????
        // ?????????????????????skuId?????????
        List<SkuLockVo> skuLockVos = items.stream().map(item -> {
            SkuLockVo skuLockVo = new SkuLockVo();
            skuLockVo.setSkuId(item.getSkuId());
            skuLockVo.setCount(item.getCount().intValue());
            return skuLockVo;
        }).collect(Collectors.toList());

        // ??????????????????
        List<SkuLockVo> resultLockVo = wmsClient.checkLock(skuLockVos, orderToken).getData();

        // ????????????????????????
        if (CollectionUtils.isNotEmpty(resultLockVo)) {
            throw new RuntimeException(JSON.toJSONString(resultLockVo));
        }

        // 4. ????????????
        Long userId = submitVo.getAddress().getUserId();

        try {
            // ??????????????????
            omsClient.saveOrder(submitVo, userId);
            // ??????oms???????????????????????????
            rabbitTemplate.convertAndSend("ORDER_MSG_EXCHANGE", "order.ttl", orderToken);
        } catch (Exception e) {
            // ????????????????????????????????????oms????????????????????? ??? wms????????????
            rabbitTemplate.convertAndSend("ORDER_MSG_EXCHANGE", "order.failure", orderToken);
            throw new RuntimeException("???????????????!");
        }

        // 5. ???????????????????????????
        List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());

        Map<String, Object> msg = new HashMap<>();

        msg.put("userId", userId);
        msg.put("skuIds", JSON.toJSONString(skuIds));
        rabbitTemplate.convertAndSend("ORDER_MSG_EXCHANGE", "cart.delete", msg);
    }
}
