package com.atguigu.gmall.cart.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/26 22:02
 * @Email: 1796235969@qq.com
 */
@Component
public class CartListener {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CartMapper cartMapper;

    private static final String PRICE_PREFIX = "CART:PRICE:";

    private static final String KEY_PREFIX = "CART:INFO:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "CART_DELETE_QUEUE"),
            exchange = @Exchange(value = "ORDER_MSG_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"cart.delete"}
    ))
    public void orderListener(Map<String, Object> msg, Channel channel, Message message) throws IOException {
        if (CollectionUtils.isEmpty(msg)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        try {
            String userId = msg.get("userId").toString();
            String skuIdsJson = msg.get("skuIds").toString();

            if (StringUtils.isBlank(userId) || StringUtils.isBlank(skuIdsJson)) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);

            // 反序列化为skuIds
            List<String> skuIds = JSON.parseArray(skuIdsJson, String.class);

            // redis删除数据
            hashOps.delete(skuIds.toArray());

            cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id", userId).in("sku_id", skuIds));

            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            if (message.getMessageProperties().getRedelivered()) {
                // 拒绝消息  不重新入队
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            }else {
                // 不确认消息: 游标, 是否批量确认, 重新入队
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
            e.printStackTrace();
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "CART_PRICE_QUEUE"),
            exchange = @Exchange(value = "PMS_SPU_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.update"}
    ))
    public void cartListener(Long spuId, Channel channel, Message message) throws IOException {
        if (spuId == null) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        List<SkuEntity> skuEntities = pmsClient.querySkusBySpuId(spuId).getData();

        if (CollectionUtils.isEmpty(skuEntities)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 遍历商品同步价格
        skuEntities.forEach(skuEntity -> {
            redisTemplate.opsForValue().setIfPresent(PRICE_PREFIX + skuEntity.getId(), skuEntity.getPrice().toString());
        });

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

    }
}
