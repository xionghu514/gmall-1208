package com.atguigu.gmall.item;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GmallItemApplicationTests {

	@Autowired
	private GmallPmsClient pmsClient;

	@Autowired
	private GmallSmsClient smsClient;

	@Autowired
	private GmallWmsClient wmsClient;
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

	@Test
	void test1() {
		// 2.根据三级分类的id查询一二三级分类
		ResponseVo<List<CategoryEntity>> responseVo = pmsClient.queryLvl123CategoriesByCid3(225l);
		List<CategoryEntity> categoryEntityList = responseVo.getData();
		System.out.println(categoryEntityList);
	}

	@Test
	void test2() {
		// 3.根据品牌id查询品牌
		System.out.println(pmsClient.queryBrandById(3l));
	}

	@Test
	void test3() {
		// 4.根据spuId查询spu
		System.out.println(pmsClient.querySpuById(7l));
	}

	@Test
	void test4() {
		// 5.根据skuId查询sku的图片列表
		System.out.println(pmsClient.querySkuImagesBySkuId(1l));
	}

	@Test
	void test5() {
		// 6.根据skuId查询营销信息
		ResponseVo<List<ItemSaleVo>> responseVo = smsClient.querySalesBySkuId(7l);
		List<ItemSaleVo> itemSaleVoList = responseVo.getData();
		System.out.println(itemSaleVoList);
	}

	@Test
	void test6() {
		// 7.根据skuId查询库存
		List<WareSkuEntity> data = wmsClient.queryWareSkuEntities(2l).getData();
		System.out.println(data);
	}

	@Test
	void test7() {
		// 8.根据spuId查询spu下所有sku的销售属性列表
		List<SaleAttrValueVo> data = pmsClient.querySaleAttrValuesBySpuId(7l).getData();
		data.forEach(System.out::println);
	}

	@Test
	void test8() {
		// 9.根据skuId查询当前sku的销售属性
		System.out.println(pmsClient.querySaleAttrValueBySkuId(1l).getData());
	}

	@Test
	void test9() {
		// 10.根据spuId查询spu下所有销售属性组合与skuId的映射关系
		String data = pmsClient.queryMappingBySpuId(7l).getData();
		System.out.println(data);
	}

	@Test
	void test10() {
		//11.根据spuId查询spu的描述信息
		System.out.println(pmsClient.querySpuDescById(7l));
	}

	@Test
	void testt11() {
		// 12.查询规格参数分组及组下的规格参数和值
		System.out.println(pmsClient.queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(225l, 7l, 1l).getData());
	}
}
