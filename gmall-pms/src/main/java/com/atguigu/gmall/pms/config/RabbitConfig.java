package com.atguigu.gmall.pms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
}
