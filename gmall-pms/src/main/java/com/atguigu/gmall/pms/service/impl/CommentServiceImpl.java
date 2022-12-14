package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.CommentEntity;
import com.atguigu.gmall.pms.mapper.CommentMapper;
import com.atguigu.gmall.pms.service.CommentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service("commentService")
public class CommentServiceImpl extends ServiceImpl<CommentMapper, CommentEntity> implements CommentService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CommentEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CommentEntity>()
        );


        return new PageResultVo(page);
    }

}