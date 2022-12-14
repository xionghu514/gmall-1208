package com.atguigu.gmall.search;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.search.feign.GmallPmsClice;
import com.atguigu.gmall.search.feign.GmallSmsClice;
import com.atguigu.gmall.search.feign.GmallWmsClice;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValueVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {
	@Autowired
	private GmallPmsClice pmsClice;

	@Autowired
	private GmallSmsClice smsClice;

	@Autowired
	private GmallWmsClice wmsClice;

	@Autowired
	private ElasticsearchRestTemplate restTemplate;

	@Test
	void contextLoads() {
		IndexOperations indexOperations = restTemplate.indexOps(Goods.class);
		indexOperations.create();
		indexOperations.putMapping(indexOperations.createMapping());
	}

	@Test
	void test1() {
//		ResponseVo<List<SpuEntity>> listResponseVo = pmsClice.querySpuByPageJson(new PageParamVo(1, 10, null));
//		List<SpuEntity> spuEntityList = listResponseVo.getData();
//		spuEntityList.forEach(System.out::println);
//		List<SpuAttrValueEntity> data = pmsClice.querySpuAttrValueEntitiesBySpuIdAndCid(225l, 7l).getData();
		List<SkuAttrValueEntity> data = pmsClice.querySkuAttrValueEntitiesBySkuIdAndCid(225l, 7l).getData();
		data.forEach(System.out::println);
	}

	@Test
	void test2() {
		Integer pageNum = 1;
		Integer pageSize = 50;
		IndexOperations indexOps = restTemplate.indexOps(Goods.class);
		if (!indexOps.exists()) {
			indexOps.create();
			indexOps.putMapping(indexOps.createMapping());
		}


		do {
			// 分页查询spu
			List<SpuEntity> spuEntities = pmsClice.querySpuByPageJson(new PageParamVo(pageNum, pageSize, null)).getData();
			// 如果spu为空，就直接返回
			if (CollectionUtils.isEmpty(spuEntities)) {
				return;
			}
			// 遍历spu集合
			spuEntities.forEach(spuEntity -> {
				// 获取spuId
				Long spuId = spuEntity.getId();
				// 获取品牌Id
				Long brandId = spuEntity.getBrandId();
				// 获取分类Id
				Long categoryId = spuEntity.getCategoryId();
				// 获取 商品上架时间
				Date createTime = spuEntity.getCreateTime();

				// 获取基本属性集合
				List<SpuAttrValueEntity> spuAttrValueEntities = pmsClice.querySpuAttrValueEntitiesBySpuIdAndCid(categoryId, spuId).getData();

				// 根据spuId 获取sku集合
				List<SkuEntity> skuEntities = pmsClice.querySkusBySpuId(spuId).getData();
				// 判空， 如果sku集合为空直接返回
				if (CollectionUtils.isEmpty(skuEntities)) {
					return;
				}

				// 遍历sku集合
				List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
					// 获取skuId
					Long skuId = skuEntity.getId();
					Goods goods = new Goods();
					// 设置 基础字段
					goods.setSkuId(skuId);
					goods.setTitle(skuEntity.getTitle());
					goods.setSubtitle(skuEntity.getSubtitle());
					goods.setDefaultImage(skuEntity.getDefaultImage());
					goods.setPrice(skuEntity.getPrice().doubleValue());

					// 设置创建时间
					goods.setCreateTime(createTime);

					// 获取商品库存信息
					List<WareSkuEntity> wareSkuEntities = wmsClice.queryWareSkuEntities(skuId).getData();
					// 判断库存信息是否为空，为空就不设置
					if (CollectionUtils.isNotEmpty(wareSkuEntities)) {
						// 获取该商品总销量
						Long sales = wareSkuEntities.stream().map(WareSkuEntity::getSales)
								.reduce((a, b) -> a + b).get();
						// 设置销量
						goods.setSales(sales);

						// 设置是否有货
						goods.setStore(
								wareSkuEntities.stream().
										anyMatch(wareSkuEntity ->
												wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0
										)
						);
					}

					// 获取品牌信息
					BrandEntity brandEntity = pmsClice.queryBrandById(brandId).getData();
					// 判空， 品牌信息不为空就不设置
					if (brandEntity != null) {
						// 设置品牌id
						goods.setBrandId(brandId);
						// 设置 品牌名称
						goods.setBrandName(brandEntity.getName());
						// 设置品牌logo
						goods.setLogo(brandEntity.getLogo());
					}

					// 获取分类信息
					CategoryEntity categoryEntity = pmsClice.queryCategoryById(categoryId).getData();
					// 判空， 如果分类信息为空就不设置
					if (categoryEntity != null) {
						goods.setCategoryId(categoryId);
						goods.setCategoryName(categoryEntity.getName());
					}

					// 判空 如果基本属性集合为空 就不用设置
					if (CollectionUtils.isNotEmpty(spuAttrValueEntities)) {
						List<SearchAttrValueVo> searchAttrValueVoList = spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
							SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
							// 设置searchAttrValeVo属性
							BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValueVo);
							return searchAttrValueVo;
						}).collect(Collectors.toList());
						goods.setSearchAttrs(searchAttrValueVoList);
					}

					// 获取销售属性集合
					List<SkuAttrValueEntity> skuAttrValueEntities = pmsClice.querySkuAttrValueEntitiesBySkuIdAndCid(categoryId, skuId).getData();
					// 判空 如果销售属性集合为空则不设置
					if (CollectionUtils.isNotEmpty(skuAttrValueEntities)) {
						List<SearchAttrValueVo> searchAttrValueVoList = skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
							SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
							// 设置searchAttrValeVo属性
							BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValueVo);

							return searchAttrValueVo;
						}).collect(Collectors.toList());

						goods.setSearchAttrs(searchAttrValueVoList);
					}


					return goods;
				}).collect(Collectors.toList());

				restTemplate.save(goodsList);
			});

			pageSize = spuEntities.size();
			pageNum++;
		}while (pageSize == 50);

	}


}
