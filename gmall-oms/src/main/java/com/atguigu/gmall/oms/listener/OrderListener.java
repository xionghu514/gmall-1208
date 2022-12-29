package com.atguigu.gmall.oms.listener;

import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/29 15:10
 * @Email: 1796235969@qq.com
 */
@Component
public class OrderListener {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private OrderMapper orderMapper;

    @RabbitListener(queues = {"ORDER_DEAD_QUEUE"})
    public void closeOrder(String orderToken, Channel channel, Message message) throws IOException {
        if (StringUtils.isBlank(orderToken)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 关闭订单
        if (orderMapper.updateStatus(orderToken, 0, 4) == 1) {
            // 如果关单成功就发送消息给wms，解锁库存
            rabbitTemplate.convertAndSend("ORDER_MSG_EXCHANGE", "stock.unlock", orderToken);
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("OMS_FAIL_QUEUE"),
            exchange = @Exchange(name = "ORDER_MSG_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"order.failure"}
    ))
    public void failOrder(String orderToken, Channel channel, Message message) throws IOException {
        if (StringUtils.isBlank(orderToken)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 标记为无效订单
        orderMapper.updateStatus(orderToken, 0, 5);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

    }
}
