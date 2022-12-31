package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.payment.feign.GmallOmsClient;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.vo.PayVo;
import com.atguigu.gmall.payment.vo.PaymentInfoEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description:
 * @Author: xionghu514
 * @Date: 2022/12/30 12:14
 * @Email: 1796235969@qq.com
 */
@Service
public class PaymentService {
    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private GmallOmsClient omsClient;

    public OrderEntity queryOrderByToken(String orderToken) {
        OrderEntity orderentity = omsClient.queryOrderByToken(orderToken).getData();
        return orderentity;
    }

    public Long savePaymentInfo(PayVo payVo) {
        PaymentInfoEntity payEntity = new PaymentInfoEntity();
        payEntity.setOutTradeNo(payVo.getOut_trade_no());
        payEntity.setPaymentType(1);
        payEntity.setTotalAmount(new BigDecimal(payVo.getTotal_amount()));
        payEntity.setSubject(payVo.getSubject());
        payEntity.setPaymentStatus(0);
        payEntity.setCreateTime(new Date());

        paymentInfoMapper.insert(payEntity);

        System.out.println("id: " + payEntity.getId());
        return payEntity.getId();
    }

    public PaymentInfoEntity queryPaymentInfoById(String payId) {

        return paymentInfoMapper.selectById(payId);
    }

    public int updataPaymentInfo(PaymentInfoEntity paymentInfoEntity) {

        return paymentInfoMapper.updateById(paymentInfoEntity);
    }
}
