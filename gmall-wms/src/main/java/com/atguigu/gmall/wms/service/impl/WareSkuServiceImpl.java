package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.service.WareSkuService;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuMapper wareSkuMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String LOCK_FIX = "stock:lock:";
    private static final String KEY_FIX = "stock:info:";

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareSkuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    @Transactional
    public List<SkuLockVo> checkLock(List<SkuLockVo> skuLockVos, String orderToken) {
        // 判空
        if (CollectionUtils.isEmpty(skuLockVos)) {
            throw new RuntimeException("请选择您要购买的商品");
        }

        // 遍历商品，进行验库存锁库存
        skuLockVos.forEach(skuLockVo -> {
            checkAndLock(skuLockVo);
        });

        // 如果有商品锁库存失败，则将所有锁库存成功的商品解锁库存
        if (!skuLockVos.stream().anyMatch(SkuLockVo::getLock)) {
            // 遍历解锁
            skuLockVos.stream().filter(SkuLockVo::getLock).collect(Collectors.toList()).forEach(skuLockVo -> {
                wareSkuMapper.unlock(skuLockVo.getWareSkuId(), skuLockVo.getCount());
            });

            // 如果锁定失败就放回订单数据
            return skuLockVos;
        }

        // 锁定成功则将商品的数据保存到数据库，为以后取消订单做准备
        redisTemplate.opsForValue().set(KEY_FIX + orderToken, JSON.toJSONString(skuLockVos), 25, TimeUnit.HOURS);

        // 发送消息到延时队列，到时间解锁库存
        rabbitTemplate.convertAndSend("ORDER_MSG_EXCHANGE", "stock.ttl", orderToken);

        // 如果验库存锁库存成功就返回null
        return null;
    }

    public void checkAndLock(SkuLockVo skuLockVo) {
        RLock lock = redissonClient.getLock(LOCK_FIX + skuLockVo.getSkuId());

        // 上锁
        lock.lock();

        try {
            // 1. 验库存
            List<WareSkuEntity> wares = wareSkuMapper.check(skuLockVo.getSkuId(), skuLockVo.getCount());

            if (CollectionUtils.isEmpty(wares)) {
                skuLockVo.setLock(false);
                return;
            }

            // 2. 锁库存
            WareSkuEntity wareSkuEntity = wares.get(0);

            int i = wareSkuMapper.lock(wareSkuEntity.getId(), skuLockVo.getCount());

            if (i == 1) {
                skuLockVo.setLock(true);
                skuLockVo.setWareSkuId(wareSkuEntity.getId());
            }
        } finally {
            lock.unlock();
        }

    }

}