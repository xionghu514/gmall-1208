package com.atguigu.gmall.index;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallIndexApplicationTests {
	@Autowired
	private RedissonClient redissonClient;

	@Test
	void test1() {
		BloomFilter<CharSequence> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), 20, 0.3);
		bloomFilter.put("1");
		bloomFilter.put("2");
		bloomFilter.put("3");
		bloomFilter.put("4");
		bloomFilter.put("5");
		bloomFilter.put("6");

		System.out.println(bloomFilter.mightContain("1"));
		System.out.println(bloomFilter.mightContain("3"));
		System.out.println(bloomFilter.mightContain("5"));
		System.out.println(bloomFilter.mightContain("7"));
		System.out.println(bloomFilter.mightContain("9"));
		System.out.println(bloomFilter.mightContain("11"));
		System.out.println(bloomFilter.mightContain("13"));
		System.out.println(bloomFilter.mightContain("15"));
		System.out.println(bloomFilter.mightContain("17"));
		System.out.println(bloomFilter.mightContain("19"));
	}

	@Test
	void contextLoads() {
		RBloomFilter<String> bf = redissonClient.getBloomFilter("bf");
		bf.tryInit(30, 0.3);
		bf.add("1");
		bf.add("2");
		bf.add("3");
		bf.add("4");
		bf.add("5");
		bf.add("6");

		System.out.println(bf.contains("1"));
		System.out.println(bf.contains("3"));
		System.out.println(bf.contains("5"));
		System.out.println(bf.contains("7"));
		System.out.println(bf.contains("9"));
		System.out.println(bf.contains("11"));
		System.out.println(bf.contains("13"));
		System.out.println(bf.contains("15"));
		System.out.println(bf.contains("17"));
		System.out.println(bf.contains("19"));

	}

}
