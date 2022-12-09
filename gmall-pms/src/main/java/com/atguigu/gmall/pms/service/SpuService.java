package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * spu信息
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2022-12-09 09:47:54
 */
public interface SpuService extends IService<SpuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    PageResultVo querySpusByCidAndPage(Long cid, PageParamVo paramVo);
}

