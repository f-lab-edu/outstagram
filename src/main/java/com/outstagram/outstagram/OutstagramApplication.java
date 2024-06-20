package com.outstagram.outstagram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableCaching
@EnableScheduling
@SpringBootApplication
public class OutstagramApplication {

	public static void main(String[] args) {
		SpringApplication.run(OutstagramApplication.class, args);
	}

}
