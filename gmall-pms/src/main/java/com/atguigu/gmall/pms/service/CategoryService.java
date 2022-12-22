package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品三级分类
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2022-12-09 09:47:55
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<CategoryEntity> queryCategoryByPid(Long pid);

    List<CategoryEntity> queryLevel23CategoriesByPid(Long pid);

    List<CategoryEntity> queryLvl123CategoriesByCid3(Long cid3);
}

