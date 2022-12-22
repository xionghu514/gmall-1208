package com.atguigu.gmall.item;

import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.SkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallItemApplicationTests {

	@Autowired
	private GmallPmsClient pmsClient;
	/*
	1.根据skuId查询sku
	2.根据三级分类的id查询一二三级分类
	3.根据品牌id查询品牌
	4.根据spuId查询spu
	5.根据skuId查询sku的图片列表
	6.根据skuId查询营销信息
	7.根据skuId查询库存
	8.根据spuId查询spu下所有sku的销售属性列表
	9.根据skuId查询当前sku的销售属性
	10.根据spuId查询spu下所有销售属性组合与skuId的映射关系
	11.根据spuId查询spu的描述信息
	12.查询规格参数分组及组下的规格参数和值
	*/

	@Test
	void contextLoads() {
//		1.根据skuId查询sku
		SkuEntity skuEntity = pmsClient.querySkuById(1l).getData();
		System.out.println(skuEntity);
	}

}
