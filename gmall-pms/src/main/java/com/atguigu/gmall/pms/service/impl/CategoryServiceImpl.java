package com.atguigu.gmall.pms.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<CategoryEntity> queryCategoryByPid(Long pid) {
        // pid = -1 查询所有 否则根据 pid 进行查询
        LambdaQueryWrapper<CategoryEntity> wrapper = new LambdaQueryWrapper<>();
        if (pid == -1) {
            return list(wrapper);
        }
        List<CategoryEntity> categoryEntities = list(wrapper.eq(CategoryEntity::getParentId, pid));

        return categoryEntities;
    }

    @Override
    public List<CategoryEntity> queryLevel23CategoriesByPid(Long pid) {
        List<CategoryEntity> categoryEntities = categoryMapper.queryCategoriesByPid(pid);
        if (CollectionUtils.isEmpty(categoryEntities)) {
            return null;
        }

        return categoryEntities;
    }

    @Override
    public List<CategoryEntity> queryLvl123CategoriesByCid3(Long cid3) {
        CategoryEntity categoryEntity3 = getById(cid3);
        if (categoryEntity3 == null) {
            return null;
        }
        CategoryEntity categoryEntity2 = getById(categoryEntity3.getParentId());
        if (categoryEntity2 == null) {
            return null;
        }
        CategoryEntity categoryEntity = getById(categoryEntity2.getParentId());

        return Arrays.asList(categoryEntity,categoryEntity2,categoryEntity3);
    }

}