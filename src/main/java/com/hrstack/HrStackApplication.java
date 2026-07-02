package com.hrstack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class HrStackApplication {

	public static void main(String[] args) {
		SpringApplication.run(HrStackApplication.class, args);
	}

}
