package com.atguigu.gmall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/15 19:48
 * @Email: 1796235969@qq.com
 */
@Component
@Slf4j
public class RabbitConfig {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        // 确认消息是否到达交换机，不管是否到达交换机都执行
        rabbitTemplate.setConfirmCallback((@Nullable CorrelationData correlationData, boolean ack, @Nullable String cause) -> {
            if (ack) {
                log.info("消息到达交换机");
                System.out.println(correlationData);
            } else {
                log.error("消息没有到达交换机，原因：{}", cause);
            }
        });

        // 确认消息是否到达队列，只有消息没到队列才执行
        rabbitTemplate.setReturnCallback((Message message, int replyCode, String replyText, String exchange, String routingKey) -> {
            log.error("消息没到达队列，交换机：{}，路由键：{}，消息内容：{}，状态码：{}，状态文本：{}", exchange, routingKey,
                    new String(message.getBody()), replyCode, replyText);
        });
    }

    /**
     *  业务交换机: ORDER_MSG_EXCHANGE
     */

    /**
     *   延时队列： ORDER_TTL_QUEUE
     */
    @Bean
    public Queue ttlQueue() {
        return QueueBuilder.durable("ORDER_TTL_QUEUE").ttl(30000000)
                .deadLetterExchange("ORDER_MSG_EXCHANGE")
                .deadLetterRoutingKey("order.dead").build();
    }

    /**
     *   将延时队列绑定到业务交换机: order.ttl
     */
    @Bean
    public Binding ttlBinding() {
        return new Binding("ORDER_TTL_QUEUE", Binding.DestinationType.QUEUE,
                "ORDER_MSG_EXCHANGE", "order.ttl", null);
    }

    /**
     *   死信交换机：ORDER_MSG_EXCHANGE
     */

    /**
     *   死信队列：ORDER_DEAD_QUEUE
     */
    @Bean
    public Queue deadQueue() {
        return QueueBuilder.durable("ORDER_DEAD_QUEUE").build();
    }

    /**
     *   将死信队列绑定到死信交换机: order.dead
     */
    @Bean
    public Binding deadExchange() {
        return new Binding("ORDER_DEAD_QUEUE", Binding.DestinationType.QUEUE,
                "ORDER_MSG_EXCHANGE", "order.dead", null);
    }


}
