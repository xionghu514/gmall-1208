package com.atguigu.gmall.wms.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/29 15:34
 * @Email: 1796235969@qq.com
 */
@Component
public class StockListener {
    @Autowired
    private WareSkuMapper wareSkuMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_FIX = "stock:info:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "STOCK_MINUS_QUEUE"),
            exchange = @Exchange(value = "ORDER_MSG_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"stock.minus"}
    ))
    public void minus(String orderToken, Channel channel, Message message) throws IOException {
        // 1.对数据判空
        if (StringUtils.isBlank(orderToken)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        // 2.获取订单Json数据
        String skuLockVosJson = redisTemplate.opsForValue().get(KEY_FIX + orderToken);

        if (StringUtils.isBlank(skuLockVosJson)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        // 3.获取订单Json数据
        List<SkuLockVo> skuLockVos = JSON.parseArray(skuLockVosJson, SkuLockVo.class);


        if (CollectionUtils.isEmpty(skuLockVos)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        // 4.遍历解锁库存
        skuLockVos.forEach(skuLockVo -> {
            wareSkuMapper.minus(skuLockVo.getWareSkuId(), skuLockVo.getCount());
        });

        // 5.将redis的锁定的缓存删除
        redisTemplate.delete(KEY_FIX + orderToken);

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "STOCK_UNLOCK_QUEUE"),
            exchange = @Exchange(value = "ORDER_MSG_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"order.failure", "stock.unlock"}
    ))
    public void unlock(String orderToken, Channel channel, Message message) throws IOException {
        // 1.对数据判空
        if (StringUtils.isBlank(orderToken)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        // 2.获取订单Json数据
        String skuLockVosJson = redisTemplate.opsForValue().get(KEY_FIX + orderToken);

        if (StringUtils.isBlank(skuLockVosJson)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        // 3.获取订单Json数据
        List<SkuLockVo> skuLockVos = JSON.parseArray(skuLockVosJson, SkuLockVo.class);


        if (CollectionUtils.isEmpty(skuLockVos)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        // 4.遍历解锁库存
        skuLockVos.forEach(skuLockVo -> {
            wareSkuMapper.unlock(skuLockVo.getWareSkuId(), skuLockVo.getCount());
        });

        // 5.将redis的锁定的缓存删除
        redisTemplate.delete(KEY_FIX + orderToken);

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
