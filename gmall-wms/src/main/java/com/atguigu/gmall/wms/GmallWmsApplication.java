package com.atguigu.gmall.wms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableFeignClients
//@EnableDiscoveryClient // 将服务注册到服务中心, 默认会注册 可以省略
@EnableSwagger2
@MapperScan("com.atguigu.gmall.wms.mapper")
public class GmallWmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallWmsApplication.class, args);
	}

}
