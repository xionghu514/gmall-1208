package com.atguigu.gmall.pms.controller;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 属性分组
 *
 * @author fengge
 * @email fengge@atguigu.com
 * @date 2022-12-09 09:47:55
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {

    @Autowired
    private AttrGroupService attrGroupService;

    @GetMapping("with/attr/value/{cid}")
    public ResponseVo<List<ItemGroupVo>> queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(
            @PathVariable("cid") Long cid,
            @RequestParam("spuId") Long spuId,
            @RequestParam("skuId") Long skuId
    ) {
        List<ItemGroupVo> groupVos = attrGroupService.queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(cid, spuId, skuId);

        return ResponseVo.ok(groupVos);
    }

    @GetMapping("/withattrs/{catId}")
    @ApiOperation("根据 分类Id 查询 分组以及组下的基本属性规格参数")
    public ResponseVo<List<AttrGroupEntity>> queryAttrGroupAndAttr(@PathVariable("catId") Long catId) {
        List<AttrGroupEntity> attrGroupEntities = attrGroupService.queryAttrGroupAndAttr(catId);

        return ResponseVo.ok(attrGroupEntities);
    }

    @GetMapping("/category/{cid}")
    @ApiOperation("根据分类id查询分组")
    public ResponseVo<List<AttrGroupEntity>> queryAttrGroupsByCid(@PathVariable("cid") Long cid) {
        List<AttrGroupEntity> attrGroupEntityList = attrGroupService.list(
                new LambdaQueryWrapper<AttrGroupEntity>()
                        .eq(AttrGroupEntity::getCategoryId, cid)
        );

        return ResponseVo.ok(attrGroupEntityList);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryAttrGroupByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = attrGroupService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<AttrGroupEntity> queryAttrGroupById(@PathVariable("id") Long id){
		AttrGroupEntity attrGroup = attrGroupService.getById(id);

        return ResponseVo.ok(attrGroup);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		attrGroupService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
