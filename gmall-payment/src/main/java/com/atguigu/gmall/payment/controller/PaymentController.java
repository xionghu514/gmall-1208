package com.atguigu.gmall.payment.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.payment.config.AlipayTemplate;
import com.atguigu.gmall.payment.interceptor.LoginInterceptor;
import com.atguigu.gmall.payment.service.PaymentService;
import com.atguigu.gmall.payment.vo.PayAsyncVo;
import com.atguigu.gmall.payment.vo.PayVo;
import com.atguigu.gmall.payment.vo.PaymentInfoEntity;
import com.atguigu.gmall.payment.vo.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/30 12:03
 * @Email: 1796235969@qq.com
 */
@Controller
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 同步回调
    @GetMapping("/pay/succ")
    public String paySucc() {
        System.out.println("同步回调");

        return "paysuccess";
    }

    @PostMapping("/pay/ok")
    @ResponseBody
    public Object payOk(PayAsyncVo asyncVo) {
        System.out.println("异步回调");

        // 1.验签 失败返回 failure
        Boolean flag = alipayTemplate.checkSignature(asyncVo);
        if (!flag) {
            return "failure";
        }

        // 2. 校验业务参数 app_id out_trade_no total_amount
        String app_id = asyncVo.getApp_id();
        String out_trade_no = asyncVo.getOut_trade_no();
        String total_amount = asyncVo.getTotal_amount();

        String payId = asyncVo.getPassback_params();
        // 根据id查询对账信息
        PaymentInfoEntity paymentInfoEntity = paymentService.queryPaymentInfoById(payId);
        if (!StringUtils.equals(app_id, alipayTemplate.getApp_id()) ||
                !StringUtils.equals(out_trade_no, paymentInfoEntity.getOutTradeNo()) ||
                new BigDecimal(total_amount).compareTo(paymentInfoEntity.getTotalAmount()) != 0
        ) {
            return "failure";
        }

        // 3. 校验支付状态 TRADE_SUCCESS
        if (!StringUtils.equals(asyncVo.getTrade_status(), "TRADE_SUCCESS")) {
            return "failure";
        }

        // 4. 修改对账记录，改为已支付状态
        paymentInfoEntity.setTradeNo(asyncVo.getTrade_no());
        paymentInfoEntity.setCallbackTime(new Date());
        paymentInfoEntity.setCallbackContent(JSON.toJSONString(asyncVo));
        paymentInfoEntity.setPaymentStatus(1);

        if (paymentService.updataPaymentInfo(paymentInfoEntity) == 1) {
            // 5. 发消息给mq，发给oms修改订单状态
            rabbitTemplate.convertAndSend("ORDER_MSG_EXCHANGE", "order.pay", out_trade_no);
        }

        // 6. 返回 success
        return "success";
    }


    @GetMapping("alipay.html")
    @ResponseBody
    public Object alipay(@RequestParam("orderToken") String orderToken, Model model) {
        OrderEntity orderEntity = paymentService.queryOrderByToken(orderToken);

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();

        if (orderEntity == null) {
            throw new RuntimeException("您的订单不存在!");
        }

        if (userId != orderEntity.getUserId()) {
            throw new RuntimeException("该订单不属于您!");
        }

        if (orderEntity.getStatus() != 0) {
            throw new RuntimeException("该订单不能支付");
        }

        try {
            // 调用支付宝接口打开支付页面
            PayVo payVo = new PayVo();
            payVo.setOut_trade_no(orderToken);
            payVo.setTotal_amount("0.01");
            payVo.setSubject("谷粒商城支付平台");

            // 将支付数据保存到数据库
            Long payId = paymentService.savePaymentInfo(payVo);
            payVo.setPassback_params(payId.toString());

            return alipayTemplate.pay(payVo);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("打开支付成功页面失败");
    }

    @GetMapping("pay.html")
    public String pay(@RequestParam("orderToken") String orderToken, Model model) {
        OrderEntity orderEntity = paymentService.queryOrderByToken(orderToken);

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();

        if (orderEntity == null) {
            throw new RuntimeException("您的订单不存在!");
        }

        if (userId != orderEntity.getUserId()) {
            throw new RuntimeException("该订单不属于您!");
        }

        if (orderEntity.getStatus() != 0) {
            throw new RuntimeException("该订单不能支付");
        }

        model.addAttribute("orderEntity", orderEntity);
        return "pay";
    }
}
