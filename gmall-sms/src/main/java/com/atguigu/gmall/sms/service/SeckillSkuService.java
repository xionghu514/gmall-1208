package com.atguigu.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.sms.entity.SeckillSkuEntity;

import java.util.Map;

/**
 * 秒杀活动商品关联
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2022-12-09 09:54:51
 */
public interface SeckillSkuService extends IService<SeckillSkuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

