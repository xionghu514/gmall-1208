package com.atguigu.gmall.search;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.search.feign.GmallPmsClice;
import com.atguigu.gmall.search.feign.GmallSmsClice;
import com.atguigu.gmall.search.pojo.Goods;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;

import java.util.List;

@SpringBootTest
class GmallSearchApplicationTests {
	@Autowired
	private GmallPmsClice pmsClice;

	@Autowired
	private GmallSmsClice smsClice;

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
		ResponseVo<List<SpuEntity>> listResponseVo = pmsClice.querySpuByPageJson(new PageParamVo(1, 10, null));
		List<SpuEntity> spuEntityList = listResponseVo.getData();
		spuEntityList.forEach(System.out::println);
	}


}
